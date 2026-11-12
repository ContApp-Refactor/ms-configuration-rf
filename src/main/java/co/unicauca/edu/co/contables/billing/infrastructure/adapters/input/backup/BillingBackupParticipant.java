package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.backup;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.unicauca.edu.co.contables.billing.application.ports.output.VerificationCodeGenerator;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity.BillingDocumentEntity;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity.BillingDocumentLineEntity;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity.BillingDocumentSequenceEntity;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.repository.BillingDocumentJpaRepository;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.repository.BillingDocumentSequenceJpaRepository;
import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * @brief Participante {@code billing} (order 85): facturación de la empresa. Facturas de venta y de compra,
 * devoluciones/notas (venta y compra), sus líneas, las retenciones (JSON {@code withholdings} y
 * {@code withholding_total}), el {@code verification_code} y la numeración por tipo de documento.
 *
 * Entradas (orden de restore): {@code billing/invoices.ndjson} (documentos SIN documento original: facturas
 * en cualquier estado), {@code billing/returns.ndjson} (documentos CON {@code original_document_id}:
 * devoluciones, que apuntan a una factura ya restaurada) y {@code billing/sequences.ndjson} (último número
 * por tipo). Las líneas viajan anidadas dentro de su documento. Namespace registrado:
 * {@code BILLING_DOCUMENT} (UUID viejo → UUID nuevo). Consume {@code THIRD} (contraparte), {@code PRODUCT},
 * {@code UOM}, {@code TAX} (dentro del JSON de retenciones) y {@code PAYMENT_METHOD}.
 *
 * <ul>
 * <li><b>Empresa</b>: billing guarda {@code enterprise_id} como texto con el UUID de la empresa (igual que
 * inventario o tesorería), así que la empresa destino es {@code ctx.targetEnterpriseIdAsString()}; el tenant
 * lo llena {@code @TenantId}.</li>
 * <li><b>Ids</b>: los UUID de documento se regeneran; los ids de línea son identidad.</li>
 * <li><b>Numeración</b>: se conservan {@code number_prefix/number_value/number_display} originales (la unicidad es
 * por tenant+empresa+tipo, y la empresa destino es nueva) y la secuencia de la empresa destino queda en
 * {@code max(last_number exportado, mayor número restaurado)} por tipo, de modo que el próximo número emitido
 * sea mayor que cualquier restaurado.</li>
 * <li><b>verification_code</b> (QR público, se busca sin empresa): se conserva solo si ninguna fila de ningún
 * tenant lo tiene ya ni se usó antes en este restore; si choca se regenera con {@link VerificationCodeGenerator}.</li>
 * <li><b>Snapshots</b> (emisor, contraparte, nombre del medio de pago, nombres de producto/unidad): son
 * congelados al emitir y se copian tal cual; solo se remapean los ids de catálogo.</li>
 * <li><b>Retenciones</b>: cada elemento del JSON {@code withholdings} guarda {@code taxId}; se remapea con
 * {@code TAX}.</li>
 * <li><b>Referencias sueltas</b>: billing no tiene FKs de BD hacia otros módulos, así que una copia puede traer ids
 * que ya no existen (la empresa fue recreada con ids nuevos). Cada referencia a otro módulo se resuelve en 3
 * pasos: (1) {@link IdMap}; (2) si no está, re-vinculación por CLAVE NATURAL contra los datos ya restaurados de
 * la empresa destino (tercero por número de documento, producto por código o nombre, unidad y medio de pago por
 * nombre, impuesto por código/descripción y tarifa); (3) si tampoco, se TOLERA: {@code null} en las columnas
 * opcionales (la factura conserva sus snapshots), id viejo en {@code product_id} (NOT NULL; un id borrado nunca se
 * reutiliza) y {@code taxId} nulo en la retención. Solo la referencia interna ({@code original_document_id}) sigue
 * siendo estricta.</li>
 * </ul>
 *
 * NO se incluyen {@code billing_outbox_events} (cola de publicación a Rabbit: copiarla reenviaría eventos de
 * hechos ya publicados) ni {@code billing_idempotency_keys} (claves de reintento HTTP con TTL, ligadas a los
 * UUID de documento viejos, sin sentido en la empresa nueva). En {@link #purge} sí se borran las claves de
 * idempotencia de la empresa para que no queden apuntando a documentos borrados.
 */
@Component
@RequiredArgsConstructor
public class BillingBackupParticipant implements BackupParticipant {

    /**
     * v2: se agregan los datos del emisor y la ciudad de la contraparte congelados al emitir
     * ({@code counterparty_city}, {@code issuer_address/city/department/phone/email/economic_activity/logo}).
     * Un ZIP v1 sigue restaurando: los campos ausentes quedan nulos (igual que en documentos anteriores).
     */
    public static final int SCHEMA_VERSION = 2;
    public static final String ENTRY_INVOICES = "billing/invoices.ndjson";
    public static final String ENTRY_RETURNS = "billing/returns.ndjson";
    public static final String ENTRY_SEQUENCES = "billing/sequences.ndjson";

    private static final int BATCH = 200;
    private static final String DOCUMENT = "BillingDocumentEntity";
    private static final String SEQUENCE = "BillingDocumentSequenceEntity";
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(BillingBackupParticipant.class);

    private final BillingDocumentJpaRepository documentRepository;
    private final BillingDocumentSequenceJpaRepository sequenceRepository;
    private final VerificationCodeGenerator verificationCodes;
    private final EntityManager entityManager;

    public record LineRow(int lineNo, Integer sourceLineNo, long productId, String productCode, String productName,
            Long unitOfMeasureId, String unitOfMeasureName, BigDecimal quantity, BigDecimal catalogUnitPrice,
            BigDecimal catalogTaxRate, BigDecimal unitPriceOverride, BigDecimal taxRateOverride,
            BigDecimal unitPrice, BigDecimal taxRate, BigDecimal subtotal, BigDecimal taxAmount, BigDecimal total,
            String taxBreakdown) {
    }

    /** {@code id}, {@code originalDocumentId}, {@code counterpartyId}, productos, medio de pago: ids VIEJOS. */
    public record DocumentRow(UUID id, String documentType, String status, String numberPrefix, Long numberValue,
            String numberDisplay, LocalDate issueDate, Long counterpartyId, String counterpartyRole,
            String counterpartyName, String counterpartyIdType, String counterpartyIdNumber,
            String counterpartyVerification, String counterpartyAddress, String counterpartyEmail,
            String counterpartyPhone, String counterpartyCity, String supplierInvoiceNumber, String issuerName,
            String issuerNit, String issuerDv, String issuerAddress, String issuerCity, String issuerDepartment,
            String issuerPhone, String issuerEmail, String issuerEconomicActivity, String issuerLogo,
            Long paymentMethodId, String paymentMethodName, UUID originalDocumentId,
            String originalDocumentType, String originalNumberDisplay, LocalDate originalIssueDate,
            String returnReason, String currency, BigDecimal subtotal, BigDecimal taxTotal, BigDecimal total,
            BigDecimal withholdingTotal, String withholdings, String verificationCode, Instant createdAt,
            String createdBy, Instant updatedAt, String updatedBy, Instant emittedAt, String emittedBy,
            Instant annulledAt, String annulledBy, String annulReason, List<LineRow> lines) {
    }

    public record SequenceRow(String documentType, String prefix, long lastNumber, Instant updatedAt) {
    }

    @Override
    public String moduleKey() {
        return BackupKeys.MODULE_BILLING;
    }

    @Override
    public int order() {
        return BackupKeys.ORDER_BILLING;
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public Set<String> requires() {
        return Set.of(BackupKeys.MODULE_ENTERPRISE, BackupKeys.MODULE_THIRDS, BackupKeys.MODULE_PUC,
                BackupKeys.MODULE_TAXES, BackupKeys.MODULE_PRODUCTS, BackupKeys.MODULE_ACCOUNTING_BANKING);
    }

    /*
     * preflight: se deja el default (no valida). Las únicas unicidades de billing son
     * (tenant, empresa, tipo, número) -empresa destino nueva- y verification_code, que se regenera si
     * choca; ninguna puede hacer fallar el restore, así que no hay nada que rechazar antes de la
     * transacción (tampoco hay filas "propias" que ignorar en IN_PLACE: el purge corre antes).
     */

    /* ------------------------------------------------------------------ */
    /* Export                                                             */
    /* ------------------------------------------------------------------ */

    @Override
    public void export(UUID enterpriseId, BackupWriter writer) {
        String ent = enterpriseId.toString();
        writer.writeNdjson(ENTRY_INVOICES, new DocumentPager(ent, false));
        writer.writeNdjson(ENTRY_RETURNS, new DocumentPager(ent, true));
        writer.writeNdjson(ENTRY_SEQUENCES, sequences(ent).iterator());
    }

    private List<SequenceRow> sequences(String ent) {
        List<BillingDocumentSequenceEntity> list = entityManager.createQuery(
                "SELECT s FROM " + SEQUENCE + " s WHERE s.enterpriseId = :e ORDER BY s.documentType",
                BillingDocumentSequenceEntity.class).setParameter("e", ent).getResultList();
        List<SequenceRow> rows = new ArrayList<>(list.size());
        for (BillingDocumentSequenceEntity s : list) {
            rows.add(new SequenceRow(s.getDocumentType(), s.getPrefix(), s.getLastNumber(), s.getUpdatedAt()));
        }
        entityManager.clear();
        return rows;
    }

    /** Pagina por id (UUID) ascendente; la primera página no filtra por id para no depender del orden del UUID. */
    private final class DocumentPager implements Iterator<DocumentRow> {
        private final String ent;
        private final boolean returns;
        private UUID lastId;
        private Iterator<DocumentRow> current = List.<DocumentRow>of().iterator();
        private boolean exhausted;

        DocumentPager(String ent, boolean returns) {
            this.ent = ent;
            this.returns = returns;
        }

        @Override
        public boolean hasNext() {
            while (!current.hasNext()) {
                if (exhausted) {
                    return false;
                }
                String jpql = "SELECT d FROM " + DOCUMENT + " d WHERE d.enterpriseId = :e AND d.originalDocumentId IS "
                        + (returns ? "NOT NULL" : "NULL") + (lastId == null ? "" : " AND d.id > :a") + " ORDER BY d.id";
                var q = entityManager.createQuery(jpql, BillingDocumentEntity.class).setParameter("e", ent)
                        .setMaxResults(BATCH);
                if (lastId != null) {
                    q.setParameter("a", lastId);
                }
                List<BillingDocumentEntity> page = q.getResultList();
                if (page.size() < BATCH) {
                    exhausted = true;
                }
                if (page.isEmpty()) {
                    return false;
                }
                lastId = page.get(page.size() - 1).getId();
                List<DocumentRow> rows = new ArrayList<>(page.size());
                for (BillingDocumentEntity d : page) {
                    rows.add(toRow(d));
                }
                entityManager.clear();
                current = rows.iterator();
            }
            return true;
        }

        @Override
        public DocumentRow next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return current.next();
        }
    }

    private static DocumentRow toRow(BillingDocumentEntity d) {
        List<LineRow> lines = new ArrayList<>(d.getLines().size());
        for (BillingDocumentLineEntity l : d.getLines()) {
            lines.add(new LineRow(l.getLineNo(), l.getSourceLineNo(), l.getProductId(), l.getProductCode(),
                    l.getProductName(), l.getUnitOfMeasureId(), l.getUnitOfMeasureName(), l.getQuantity(),
                    l.getCatalogUnitPrice(), l.getCatalogTaxRate(), l.getUnitPriceOverride(),
                    l.getTaxRateOverride(), l.getUnitPrice(), l.getTaxRate(), l.getSubtotal(), l.getTaxAmount(),
                    l.getTotal(), l.getTaxBreakdown()));
        }
        return new DocumentRow(d.getId(), d.getDocumentType(), d.getStatus(), d.getNumberPrefix(),
                d.getNumberValue(), d.getNumberDisplay(), d.getIssueDate(), d.getCounterpartyId(),
                d.getCounterpartyRole(), d.getCounterpartyName(), d.getCounterpartyIdType(),
                d.getCounterpartyIdNumber(), d.getCounterpartyVerification(), d.getCounterpartyAddress(),
                d.getCounterpartyEmail(), d.getCounterpartyPhone(), d.getCounterpartyCity(),
                d.getSupplierInvoiceNumber(), d.getIssuerName(), d.getIssuerNit(), d.getIssuerDv(),
                d.getIssuerAddress(), d.getIssuerCity(), d.getIssuerDepartment(), d.getIssuerPhone(),
                d.getIssuerEmail(), d.getIssuerEconomicActivity(), d.getIssuerLogo(), d.getPaymentMethodId(),
                d.getPaymentMethodName(), d.getOriginalDocumentId(), d.getOriginalDocumentType(),
                d.getOriginalNumberDisplay(), d.getOriginalIssueDate(), d.getReturnReason(), d.getCurrency(),
                d.getSubtotal(), d.getTaxTotal(), d.getTotal(), d.getWithholdingTotal(), d.getWithholdings(),
                d.getVerificationCode(), d.getCreatedAt(), d.getCreatedBy(), d.getUpdatedAt(), d.getUpdatedBy(),
                d.getEmittedAt(), d.getEmittedBy(), d.getAnnulledAt(), d.getAnnulledBy(), d.getAnnulReason(), lines);
    }

    /* ------------------------------------------------------------------ */
    /* Restore                                                            */
    /* ------------------------------------------------------------------ */

    @Override
    public void restore(BackupReader reader, IdMap idMap, RestoreContext ctx) {
        String ent = ctx.targetEnterpriseIdAsString();
        Map<String, Long> maxNumber = new HashMap<>();
        Set<String> usedCodes = new HashSet<>();
        Refs refs = new Refs(idMap, ent);

        // Primero las facturas y luego las devoluciones, que apuntan a una factura ya restaurada.
        for (String entry : List.of(ENTRY_INVOICES, ENTRY_RETURNS)) {
            if (!reader.has(entry)) {
                continue;
            }
            reader.forEachBatch(entry, DocumentRow.class, BATCH, rows -> {
                List<BillingDocumentEntity> entities = new ArrayList<>(rows.size());
                for (DocumentRow r : rows) {
                    entities.add(toEntity(r, ent, idMap, refs, maxNumber, usedCodes));
                }
                documentRepository.saveAll(entities);
                flushAndClear();
            });
        }

        restoreSequences(reader, ent, maxNumber);
        refs.logSummary();
    }

    private BillingDocumentEntity toEntity(DocumentRow r, String ent, IdMap idMap, Refs refs,
            Map<String, Long> maxNumber, Set<String> usedCodes) {
        BillingDocumentEntity e = new BillingDocumentEntity();
        UUID newId = UUID.randomUUID();
        e.setId(newId);
        idMap.put(BackupKeys.BILLING_DOCUMENT, r.id(), newId);
        e.setEnterpriseId(ent);
        e.setDocumentType(r.documentType());
        e.setStatus(r.status());
        e.setNumberPrefix(r.numberPrefix());
        e.setNumberValue(r.numberValue());
        e.setNumberDisplay(r.numberDisplay());
        if (r.numberValue() != null) {
            maxNumber.merge(r.documentType(), r.numberValue(), Math::max);
        }
        e.setIssueDate(r.issueDate());
        e.setCounterpartyId(refs.third(r.counterpartyId(), r.counterpartyIdNumber(), r.counterpartyIdType()));
        e.setCounterpartyRole(r.counterpartyRole());
        e.setCounterpartyName(r.counterpartyName());
        e.setCounterpartyIdType(r.counterpartyIdType());
        e.setCounterpartyIdNumber(r.counterpartyIdNumber());
        e.setCounterpartyVerification(r.counterpartyVerification());
        e.setCounterpartyAddress(r.counterpartyAddress());
        e.setCounterpartyEmail(r.counterpartyEmail());
        e.setCounterpartyPhone(r.counterpartyPhone());
        e.setCounterpartyCity(r.counterpartyCity());
        e.setSupplierInvoiceNumber(r.supplierInvoiceNumber());
        e.setIssuerName(r.issuerName());
        e.setIssuerNit(r.issuerNit());
        e.setIssuerDv(r.issuerDv());
        e.setIssuerAddress(r.issuerAddress());
        e.setIssuerCity(r.issuerCity());
        e.setIssuerDepartment(r.issuerDepartment());
        e.setIssuerPhone(r.issuerPhone());
        e.setIssuerEmail(r.issuerEmail());
        e.setIssuerEconomicActivity(r.issuerEconomicActivity());
        e.setIssuerLogo(r.issuerLogo());
        e.setPaymentMethodId(refs.paymentMethod(r.paymentMethodId(), r.paymentMethodName()));
        e.setPaymentMethodName(r.paymentMethodName());
        e.setOriginalDocumentId(r.originalDocumentId() == null ? null
                : idMap.requireUuid(BackupKeys.BILLING_DOCUMENT, r.originalDocumentId()));
        e.setOriginalDocumentType(r.originalDocumentType());
        e.setOriginalNumberDisplay(r.originalNumberDisplay());
        e.setOriginalIssueDate(r.originalIssueDate());
        e.setReturnReason(r.returnReason());
        e.setCurrency(r.currency());
        e.setSubtotal(r.subtotal());
        e.setTaxTotal(r.taxTotal());
        e.setTotal(r.total());
        e.setWithholdingTotal(r.withholdingTotal());
        e.setWithholdings(remapWithholdings(r.withholdings(), refs));
        e.setVerificationCode(freeVerificationCode(r.verificationCode(), usedCodes));
        e.setCreatedAt(r.createdAt());
        e.setCreatedBy(r.createdBy());
        e.setUpdatedAt(r.updatedAt());
        e.setUpdatedBy(r.updatedBy());
        e.setEmittedAt(r.emittedAt());
        e.setEmittedBy(r.emittedBy());
        e.setAnnulledAt(r.annulledAt());
        e.setAnnulledBy(r.annulledBy());
        e.setAnnulReason(r.annulReason());
        if (r.lines() != null) {
            for (LineRow l : r.lines()) {
                BillingDocumentLineEntity le = new BillingDocumentLineEntity();
                le.setDocument(e);
                le.setLineNo(l.lineNo());
                le.setSourceLineNo(l.sourceLineNo());
                le.setProductId(refs.product(l.productId(), l.productCode(), l.productName()));
                le.setProductCode(l.productCode());
                le.setProductName(l.productName());
                le.setUnitOfMeasureId(refs.unitOfMeasure(l.unitOfMeasureId(), l.unitOfMeasureName()));
                le.setUnitOfMeasureName(l.unitOfMeasureName());
                le.setQuantity(l.quantity());
                le.setCatalogUnitPrice(l.catalogUnitPrice());
                le.setCatalogTaxRate(l.catalogTaxRate());
                le.setUnitPriceOverride(l.unitPriceOverride());
                le.setTaxRateOverride(l.taxRateOverride());
                le.setUnitPrice(l.unitPrice());
                le.setTaxRate(l.taxRate());
                le.setSubtotal(l.subtotal());
                le.setTaxAmount(l.taxAmount());
                le.setTotal(l.total());
                le.setTaxBreakdown(l.taxBreakdown());
                e.getLines().add(le);
            }
        }
        return e;
    }

    /**
     * Resuelve {@code taxId} de cada retención (namespace {@code TAX}, luego clave natural, luego {@code null});
     * el resto del JSON (nombre y tarifa congelados) queda intacto.
     */
    private static String remapWithholdings(String json, Refs refs) {
        if (json == null || json.isBlank()) {
            return json;
        }
        try {
            JsonNode root = JSON.readTree(json);
            if (!root.isArray()) {
                return json;
            }
            for (JsonNode item : root) {
                if (item instanceof ObjectNode obj && obj.hasNonNull("taxId")) {
                    Long taxId = refs.tax(obj.get("taxId").asLong(), text(obj, "taxName"), decimal(obj, "ratePct"));
                    if (taxId == null) {
                        obj.putNull("taxId");
                    } else {
                        obj.put("taxId", taxId.longValue());
                    }
                }
            }
            return JSON.writeValueAsString(root);
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalStateException("JSON de retenciones inválido en la copia de seguridad", ex);
        }
    }

    private static String text(JsonNode obj, String field) {
        return obj.hasNonNull(field) ? obj.get(field).asText() : null;
    }

    private static BigDecimal decimal(JsonNode obj, String field) {
        return obj.hasNonNull(field) && obj.get(field).isNumber() ? obj.get(field).decimalValue() : null;
    }

    /**
     * Resolución de referencias a otros módulos durante UN restore: IdMap, clave natural contra la empresa destino
     * (un solo query por clave distinta, con caché) o tolerancia. Cuenta, por tipo, cuántas referencias se
     * re-vincularon por clave natural y cuántas quedaron sueltas, para un único {@code log.warn} al final.
     */
    private final class Refs {
        private final IdMap idMap;
        private final String ent;
        private final String tenant = TenantContext.getTenantId();
        private final Map<String, Optional<Long>> cache = new HashMap<>();
        private final Map<String, long[]> stats = new LinkedHashMap<>();

        Refs(IdMap idMap, String ent) {
            this.idMap = idMap;
            this.ent = ent;
        }

        Long third(Long oldId, String idNumber, String idType) {
            return resolve("tercero", BackupKeys.THIRD, oldId, "T|" + idNumber + "|" + idType,
                    () -> thirdByNaturalKey(idNumber, idType));
        }

        Long paymentMethod(Long oldId, String name) {
            return resolve("medio de pago", BackupKeys.PAYMENT_METHOD, oldId, "M|" + name,
                    () -> firstId("SELECT id FROM payment_methods WHERE id_enterprise = :e AND tenant_id = :t "
                            + "AND LOWER(TRIM(name)) = LOWER(TRIM(:n)) ORDER BY id", name));
        }

        /** {@code product_id} es NOT NULL: sin coincidencia se conserva el id viejo. */
        long product(long oldId, String code, String name) {
            Long id = resolve("producto", BackupKeys.PRODUCT, oldId, "P|" + code + "|" + name, () -> {
                Optional<Long> byCode = firstId("SELECT id FROM product WHERE enterprise_id = :e AND tenant_id = :t "
                        + "AND LOWER(TRIM(code)) = LOWER(TRIM(:n)) ORDER BY id", code);
                return byCode.isPresent() ? byCode : firstId("SELECT id FROM product WHERE enterprise_id = :e "
                        + "AND tenant_id = :t AND LOWER(TRIM(name)) = LOWER(TRIM(:n)) ORDER BY id", name);
            });
            return id == null ? oldId : id;
        }

        Long unitOfMeasure(Long oldId, String name) {
            // unit_of_measure no tiene columna tenant_id: solo se acota por empresa.
            return resolve("unidad de medida", BackupKeys.UOM, oldId, "U|" + name,
                    () -> firstId("SELECT id FROM unit_of_measure WHERE enterprise_id = :e "
                            + "AND LOWER(TRIM(name)) = LOWER(TRIM(:n)) ORDER BY id", name));
        }

        Long tax(long oldId, String name, BigDecimal ratePct) {
            return resolve("impuesto", BackupKeys.TAX, oldId, "X|" + name + "|" + ratePct,
                    () -> taxByNaturalKey(name, ratePct));
        }

        private Long resolve(String kind, String namespace, Long oldId, String key,
                java.util.function.Supplier<Optional<Long>> natural) {
            if (oldId == null) {
                return null;
            }
            Optional<Object> mapped = idMap.find(namespace, oldId);
            if (mapped.isPresent()) {
                return ((Number) mapped.get()).longValue();
            }
            Optional<Long> found = cache.computeIfAbsent(kind + "|" + key, k -> natural.get());
            stats.computeIfAbsent(kind, k -> new long[2])[found.isPresent() ? 0 : 1]++;
            return found.orElse(null);
        }

        private Optional<Long> firstId(String sql, String name) {
            if (name == null || name.isBlank()) {
                return Optional.empty();
            }
            var q = entityManager.createNativeQuery(sql).setParameter("e", ent).setParameter("n", name);
            if (sql.contains(":t")) {
                q.setParameter("t", tenant);
            }
            @SuppressWarnings("unchecked")
            List<Number> ids = q.setMaxResults(1).getResultList();
            return ids.isEmpty() ? Optional.empty() : Optional.of(ids.get(0).longValue());
        }

        /** Número de documento del snapshot; el tipo de identificación solo desambigua si hay varias coincidencias. */
        @SuppressWarnings("unchecked")
        private Optional<Long> thirdByNaturalKey(String idNumber, String idType) {
            if (idNumber == null || idNumber.isBlank()) {
                return Optional.empty();
            }
            long number;
            try {
                number = Long.parseLong(idNumber.trim());
            } catch (NumberFormatException ex) {
                return Optional.empty();
            }
            List<Object[]> rows = entityManager.createNativeQuery(
                    "SELECT t.th_id, ti.ti_code, ti.ti_name FROM thirds t LEFT JOIN type_id ti ON ti.ti_id = t.type_id_fk "
                            + "WHERE t.ent_id = :e AND t.tenant_id = :t AND t.th_id_number = :n ORDER BY t.th_id")
                    .setParameter("e", ent).setParameter("t", tenant).setParameter("n", number).getResultList();
            if (rows.isEmpty()) {
                return Optional.empty();
            }
            if (rows.size() > 1 && idType != null && !idType.isBlank()) {
                for (Object[] row : rows) {
                    if (idType.equalsIgnoreCase((String) row[1]) || idType.equalsIgnoreCase((String) row[2])) {
                        return Optional.of(((Number) row[0]).longValue());
                    }
                }
            }
            return Optional.of(((Number) rows.get(0)[0]).longValue());
        }

        /** Código o descripción = nombre congelado; si hay varios, prefiere la misma tarifa y luego el menor id. */
        @SuppressWarnings("unchecked")
        private Optional<Long> taxByNaturalKey(String name, BigDecimal ratePct) {
            if (name == null || name.isBlank()) {
                return Optional.empty();
            }
            List<Object[]> rows = entityManager.createNativeQuery(
                    "SELECT id, interest FROM tax WHERE id_enterprise = :e AND tenant_id = :t "
                            + "AND (LOWER(TRIM(code)) = LOWER(TRIM(:n)) OR LOWER(TRIM(description)) = LOWER(TRIM(:n))) "
                            + "ORDER BY id")
                    .setParameter("e", ent).setParameter("t", tenant).setParameter("n", name).getResultList();
            if (rows.isEmpty()) {
                return Optional.empty();
            }
            if (ratePct != null) {
                for (Object[] row : rows) {
                    if (row[1] != null
                            && ratePct.compareTo(BigDecimal.valueOf(((Number) row[1]).doubleValue())) == 0) {
                        return Optional.of(((Number) row[0]).longValue());
                    }
                }
            }
            return Optional.of(((Number) rows.get(0)[0]).longValue());
        }

        void logSummary() {
            if (stats.isEmpty()) {
                return;
            }
            StringBuilder sb = new StringBuilder();
            stats.forEach((kind, c) -> sb.append(sb.length() == 0 ? "" : "; ").append(kind).append(": ")
                    .append(c[0]).append(" re-vinculadas por clave natural, ").append(c[1]).append(" sueltas"));
            log.warn("billing: la copia traía referencias a otros módulos que no están en la copia (datos de origen "
                    + "con ids viejos); se resolvieron por clave natural o quedaron sueltas con su snapshot: {}", sb);
        }
    }

    /**
     * El código de verificación se busca sin empresa (QR público): se conserva si NINGUNA fila de ningún
     * tenant lo tiene (consulta nativa, el filtro de tenant ocultaría las que chocan) y no se usó antes en
     * este restore; si no, se regenera hasta encontrar uno libre. Nulo sigue nulo (borradores).
     */
    private String freeVerificationCode(String original, Set<String> used) {
        if (original == null || original.isBlank()) {
            return original;
        }
        String candidate = original;
        int guard = 0;
        while (used.contains(candidate) || verificationCodeTaken(candidate)) {
            candidate = verificationCodes.next();
            if (++guard > 1000) {
                throw new IllegalStateException("No se pudo generar un código de verificación libre");
            }
        }
        used.add(candidate);
        return candidate;
    }

    private boolean verificationCodeTaken(String code) {
        entityManager.flush();
        Number n = (Number) entityManager
                .createNativeQuery("SELECT COUNT(*) FROM billing_documents WHERE verification_code = :v")
                .setParameter("v", code).getSingleResult();
        return n.longValue() > 0;
    }

    /**
     * Secuencia destino por tipo = max(último número exportado, mayor número restaurado). Si la fila ya
     * existe (no debería: empresa nueva o purgada) solo se sube, nunca se baja.
     */
    private void restoreSequences(BackupReader reader, String ent, Map<String, Long> maxNumber) {
        Map<String, SequenceRow> exported = new LinkedHashMap<>();
        if (reader.has(ENTRY_SEQUENCES)) {
            reader.forEach(ENTRY_SEQUENCES, SequenceRow.class, s -> exported.put(s.documentType(), s));
        }
        Set<String> types = new HashSet<>(exported.keySet());
        types.addAll(maxNumber.keySet());
        for (String type : types) {
            SequenceRow src = exported.get(type);
            long last = Math.max(src == null ? 0L : src.lastNumber(), maxNumber.getOrDefault(type, 0L));
            BillingDocumentSequenceEntity row = sequenceRepository.findForUpdate(ent, type).orElse(null);
            if (row == null) {
                row = new BillingDocumentSequenceEntity();
                row.setEnterpriseId(ent);
                row.setDocumentType(type);
                row.setPrefix(src != null && src.prefix() != null ? src.prefix() : prefixOf(type));
                row.setLastNumber(last);
            } else {
                row.setLastNumber(Math.max(row.getLastNumber(), last));
            }
            row.setUpdatedAt(src != null && src.updatedAt() != null ? src.updatedAt() : Instant.now());
            sequenceRepository.save(row);
        }
        flushAndClear();
    }

    private static String prefixOf(String type) {
        return DocumentType.valueOf(type).prefix();
    }

    /**
     * Restore en el lugar: líneas, documentos, secuencia de numeración y claves de idempotencia de la
     * empresa (apuntan a documentos que dejan de existir). SQL nativo, sin eventos ni auditoría.
     */
    @Override
    public void purge(UUID enterpriseId) {
        String ent = enterpriseId.toString();
        entityManager.flush();
        delete("DELETE FROM billing_document_lines WHERE document_id IN ("
                + "SELECT id FROM billing_documents WHERE enterprise_id = :e AND tenant_id = :t)", ent);
        delete("DELETE FROM billing_documents WHERE enterprise_id = :e AND tenant_id = :t", ent);
        delete("DELETE FROM billing_document_sequences WHERE enterprise_id = :e AND tenant_id = :t", ent);
        delete("DELETE FROM billing_idempotency_keys WHERE enterprise_id = :e AND tenant_id = :t", ent);
        flushAndClear();
    }

    private void delete(String sql, String ent) {
        entityManager.createNativeQuery(sql).setParameter("e", ent)
                .setParameter("t", TenantContext.getTenantId()).executeUpdate();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    /* ------------------------------------------------------------------ */
    /* Verificación                                                       */
    /* ------------------------------------------------------------------ */

    @Override
    public Map<String, Long> count(UUID enterpriseId) {
        String ent = enterpriseId.toString();
        return Map.of(
                ENTRY_INVOICES, count("SELECT COUNT(d) FROM " + DOCUMENT
                        + " d WHERE d.enterpriseId = :e AND d.originalDocumentId IS NULL", ent),
                ENTRY_RETURNS, count("SELECT COUNT(d) FROM " + DOCUMENT
                        + " d WHERE d.enterpriseId = :e AND d.originalDocumentId IS NOT NULL", ent),
                ENTRY_SEQUENCES, count("SELECT COUNT(s) FROM " + SEQUENCE + " s WHERE s.enterpriseId = :e", ent));
    }

    private long count(String jpql, String ent) {
        return entityManager.createQuery(jpql, Long.class).setParameter("e", ent).getSingleResult();
    }

    @Override
    public List<String> verifyIntegrity(UUID enterpriseId) {
        String ent = enterpriseId.toString();
        List<String> problems = new ArrayList<>();

        String d = "billing_documents x";
        String dW = "x.enterprise_id = :e AND x.tenant_id = :t";
        add(problems, orphans(d, dW, "x.counterparty_id", "THIRDS", "th_id", "ent_id", ent),
                "documentos de facturación referencian un tercero que no es de la empresa destino");
        add(problems, orphans(d, dW, "x.payment_method_id", "payment_methods", "id", "id_enterprise", ent),
                "documentos de facturación referencian un medio de pago que no es de la empresa destino");
        add(problems, orphans(d, dW, "x.original_document_id", "billing_documents", "id", "enterprise_id", ent),
                "devoluciones referencian un documento original que no es de la empresa destino");

        String l = "billing_document_lines x JOIN billing_documents h ON h.id = x.document_id";
        String lW = "h.enterprise_id = :e AND h.tenant_id = :t";
        // product_id (NOT NULL) no se valida: sin coincidencia por clave natural el restore conserva a propósito el id
        // viejo (un producto borrado nunca reutiliza su id) y la línea queda con su snapshot de código/nombre.
        // unit_of_measure no tiene columna tenant_id (su entidad no usa @TenantId): solo se acota por empresa.
        add(problems, orphans(l, lW, "x.unit_of_measure_id", "unit_of_measure", "id", "enterprise_id", ent, false),
                "líneas de facturación referencian una unidad de medida que no es de la empresa destino");

        long badTaxes = orphanWithholdingTaxes(ent);
        add(problems, badTaxes, "retenciones de facturas referencian un impuesto que no es de la empresa destino");
        return problems;
    }

    /** Huérfanos por SQL nativo (el filtro {@code @TenantId} no aplica): la FK debe existir en la empresa y el tenant. */
    private long orphans(String from, String where, String columnExpr, String refTable, String refKey,
            String refEnterpriseColumn, String ent) {
        return orphans(from, where, columnExpr, refTable, refKey, refEnterpriseColumn, ent, true);
    }

    private long orphans(String from, String where, String columnExpr, String refTable, String refKey,
            String refEnterpriseColumn, String ent, boolean refHasTenant) {
        entityManager.flush();
        String sql = "SELECT COUNT(*) FROM " + from + " WHERE " + where + " AND " + columnExpr
                + " IS NOT NULL AND NOT EXISTS (SELECT 1 FROM " + refTable + " r WHERE r." + refKey + " = "
                + columnExpr + " AND r." + refEnterpriseColumn + " = :e"
                + (refHasTenant ? " AND r.tenant_id = :t" : "") + ")";
        var q = entityManager.createNativeQuery(sql).setParameter("e", ent);
        // El tenant del documento ya se acota en `where` (:t); se enlaza siempre porque ahí aparece.
        q.setParameter("t", TenantContext.getTenantId());
        return ((Number) q.getSingleResult()).longValue();
    }

    /** Documentos cuyo JSON de retenciones tiene un {@code taxId} que no es un impuesto de la empresa. */
    @SuppressWarnings("unchecked")
    private long orphanWithholdingTaxes(String ent) {
        entityManager.flush();
        Set<Long> taxIds = new HashSet<>();
        List<Number> ids = entityManager.createNativeQuery(
                "SELECT id FROM tax WHERE id_enterprise = :e AND tenant_id = :t")
                .setParameter("e", ent).setParameter("t", TenantContext.getTenantId()).getResultList();
        ids.forEach(n -> taxIds.add(n.longValue()));
        long bad = 0;
        UUID last = null;
        while (true) {
            var q = entityManager.createQuery("SELECT d.id, d.withholdings FROM " + DOCUMENT
                    + " d WHERE d.enterpriseId = :e AND d.withholdings IS NOT NULL"
                    + (last == null ? "" : " AND d.id > :a") + " ORDER BY d.id", Object[].class)
                    .setParameter("e", ent).setMaxResults(BATCH);
            if (last != null) {
                q.setParameter("a", last);
            }
            List<Object[]> page = q.getResultList();
            for (Object[] row : page) {
                last = (UUID) row[0];
                if (hasUnknownTax((String) row[1], taxIds)) {
                    bad++;
                }
            }
            if (page.size() < BATCH) {
                return bad;
            }
        }
    }

    private static boolean hasUnknownTax(String json, Set<Long> taxIds) {
        try {
            JsonNode root = JSON.readTree(json);
            if (root.isArray()) {
                for (JsonNode item : root) {
                    if (item.hasNonNull("taxId") && !taxIds.contains(item.get("taxId").asLong())) {
                        return true;
                    }
                }
            }
            return false;
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            return true;
        }
    }

    private static void add(List<String> problems, long n, String message) {
        if (n > 0) {
            problems.add(n + " " + message);
        }
    }
}
