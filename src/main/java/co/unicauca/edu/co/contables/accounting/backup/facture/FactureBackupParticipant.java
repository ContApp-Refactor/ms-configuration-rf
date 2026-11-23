package co.unicauca.edu.co.contables.accounting.backup.facture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.backup.support.AccountingBackupKeys;
import co.unicauca.edu.co.contables.accounting.backup.support.GlobalUniques;
import co.unicauca.edu.co.contables.accounting.backup.support.KeysetPager;
import co.unicauca.edu.co.contables.accounting.backup.support.ReferenceChecks;
import co.unicauca.edu.co.contables.accounting.backup.support.TxSupport;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.PurchaseInvoiceStatus;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFacture;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFactureType;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonProduct;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonReturn;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.repository.SkeletonFactureRepository;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.repository.SkeletonReturnRepository;
import co.unicauca.edu.co.contables.accounting.facture.domain.model.eFactureType;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.persistence.entity.FactureEntity;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.persistence.entity.ProductEntity;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.persistence.repository.FactureRepository;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.persistence.repository.ProductRepository;
import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * @brief Participante {@code accounting.facture} (order 90): facturas del módulo {@code accounting/facture}.
 * Cubre las dos generaciones de tablas: las facturas heredadas ({@code FACTURES} +
 * {@code FACTURES_AND_PRODUCTS} + {@code PRODUCTS}) y el esqueleto actual ({@code skeleton_factures} +
 * {@code skeleton_products} + {@code skeleton_returns}).
 *
 * Entradas (orden de restore): {@code accounting/facture/legacy_products.ndjson},
 * {@code .../legacy_factures.ndjson}, {@code .../skeleton_factures.ndjson},
 * {@code .../skeleton_returns.ndjson}. Las líneas de {@code skeleton_products} viajan anidadas en su
 * factura y los ids de producto de la relación {@code FACTURES_AND_PRODUCTS} viajan dentro de la factura
 * heredada. Consume {@code THIRD} y {@code PRODUCT}; registra {@code FACT_CODE}.
 *
 * Notas de modelo (heredadas, no introducidas por la copia):
 * <ul>
 * <li>{@code PRODUCTS.prod_id} NO es autogenerado: es el id del producto de inventario, por lo que en el
 * destino pasa a ser el id NUEVO del producto ({@code PRODUCT}). La tabla no tiene tenant ni empresa: se
 * exportan solo los productos referenciados por facturas de la empresa.</li>
 * <li>{@code FACTURES.fact_code} es UNIQUE global y la búsqueda por número no filtra por empresa: se
 * conserva el número si está libre y si no se asigna uno nuevo ({@link GlobalUniques#factCode}).</li>
 * <li>{@code skeleton_returns} no tiene tenant ni empresa: se exportan las devoluciones cuyo
 * {@code originalFactCode} pertenece a una factura skeleton de la empresa. Es una limitación del esquema
 * (dos empresas del mismo tenant con el mismo número serían indistinguibles), mitigada porque el servicio
 * exige números únicos por tenant.</li>
 * </ul>
 *
 * NO se incluye {@code purchase_invoice_outbox}: es la bandeja de salida de eventos hacia otros servicios
 * (infraestructura de mensajería), copiarla reenviaría eventos antiguos. La {@code returnDate} de las
 * devoluciones se conserva (el {@code @PrePersist} la pisa y se corrige tras insertar).
 */
@Component
@RequiredArgsConstructor
public class FactureBackupParticipant implements BackupParticipant {

    public static final int SCHEMA_VERSION = 1;
    public static final String ENTRY_LEGACY_PRODUCTS = "accounting/facture/legacy_products.ndjson";
    public static final String ENTRY_LEGACY_FACTURES = "accounting/facture/legacy_factures.ndjson";
    public static final String ENTRY_SKELETON_FACTURES = "accounting/facture/skeleton_factures.ndjson";
    public static final String ENTRY_SKELETON_RETURNS = "accounting/facture/skeleton_returns.ndjson";

    private static final int BATCH = 500;

    private static final String LEGACY_FACTURE = FactureEntity.class.getName();
    private static final String LEGACY_PRODUCT = ProductEntity.class.getName();
    private static final String SKELETON = SkeletonFacture.class.getName();
    private static final String RETURN = SkeletonReturn.class.getName();

    private final FactureRepository factureRepository;
    private final ProductRepository legacyProductRepository;
    private final SkeletonFactureRepository skeletonRepository;
    private final SkeletonReturnRepository returnRepository;
    private final EntityManager entityManager;
    private final GlobalUniques uniques;
    private final ReferenceChecks checks;

    /** {@code productId} es el id VIEJO del producto de inventario (también PK de la fila). */
    public record LegacyProductRow(long productId, String code, double descount, double amount, String description,
            double vat, double unitPrice, double subtotal) {
    }

    /** {@code thId} y {@code productIds} son ids VIEJOS. */
    public record LegacyFactureRow(Long factId, Long thId, Long factCode, String factObservations,
            eFactureType factureType, List<Long> productIds, Double factSubtotals, Double facSalesTax,
            Double facWithholdingSource, Double descounts) {
    }

    public record SkeletonProductRow(Long productId, Integer amount, String description, Double discount,
            Double unitPrice, Double subtotal, List<Integer> taxPercentage) {
    }

    public record SkeletonFactureRow(Long id, Long factCode, Long thId, String totalValue, String totalPay,
            String pendingValue, LocalDate issueDate, LocalDate expirationDate, Long accountingAccount,
            SkeletonFactureType factureType, PurchaseInvoiceStatus purchaseStatus, LocalDateTime voidedAt,
            List<SkeletonProductRow> products) {
    }

    public record SkeletonReturnRow(Long id, Long originalFactCode, Long productId, Integer returnedQuantity,
            LocalDateTime returnDate, SkeletonReturn.ReturnType returnType, String details) {
    }

    @Override
    public String moduleKey() {
        return AccountingBackupKeys.MODULE_FACTURE;
    }

    @Override
    public int order() {
        return AccountingBackupKeys.ORDER_FACTURE;
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public Set<String> requires() {
        return Set.of(BackupKeys.MODULE_ENTERPRISE, BackupKeys.MODULE_THIRDS, BackupKeys.MODULE_PRODUCTS);
    }

    /* ------------------------------------------------------------------ */
    /* Export                                                             */
    /* ------------------------------------------------------------------ */

    @Override
    public void export(UUID enterpriseId, BackupWriter writer) {
        String ent = enterpriseId.toString();
        writer.writeNdjson(ENTRY_LEGACY_PRODUCTS, legacyProducts(ent));
        writer.writeNdjson(ENTRY_LEGACY_FACTURES, legacyFactures(ent));
        writer.writeNdjson(ENTRY_SKELETON_FACTURES, skeletons(ent));
        writer.writeNdjson(ENTRY_SKELETON_RETURNS, returns(ent));
    }

    private Iterator<LegacyProductRow> legacyProducts(String ent) {
        // SQL nativo: el hashCode de Lombok (@Data) de FactureEntity/ProductEntity recorre las colecciones
        // bidireccionales y revienta al hidratarlas; leer filas planas evita cargar el grafo de entidades.
        return KeysetPager.<Object[], LegacyProductRow>of(BATCH,
                (after, size) -> {
                    @SuppressWarnings("unchecked")
                    List<Object[]> page = entityManager.createNativeQuery(
                            "SELECT DISTINCT p.prod_id, p.prod_code, p.prod_descount, p.prod_amount, p.prod_description, "
                                    + "p.prod_vat, p.prod_unit_price, p.prod_subtotal FROM PRODUCTS p "
                                    + "JOIN FACTURES_AND_PRODUCTS fp ON fp.prod_id = p.prod_id "
                                    + "JOIN FACTURES f ON f.fact_id = fp.fact_id "
                                    + "WHERE f.ent_id = :e AND p.prod_id > :a ORDER BY p.prod_id")
                            .setParameter("e", ent).setParameter("a", after).setMaxResults(size).getResultList();
                    return page;
                },
                r -> ((Number) r[0]).longValue(),
                page -> {
                    List<LegacyProductRow> rows = new ArrayList<>(page.size());
                    for (Object[] r : page) {
                        rows.add(new LegacyProductRow(((Number) r[0]).longValue(), (String) r[1],
                                ((Number) r[2]).doubleValue(), ((Number) r[3]).doubleValue(), (String) r[4],
                                ((Number) r[5]).doubleValue(), ((Number) r[6]).doubleValue(),
                                ((Number) r[7]).doubleValue()));
                    }
                    return rows;
                });
    }

    private static Double dbl(Object o) {
        return o == null ? null : ((Number) o).doubleValue();
    }

    private Iterator<LegacyFactureRow> legacyFactures(String ent) {
        return KeysetPager.<Object[], LegacyFactureRow>of(BATCH,
                (after, size) -> {
                    @SuppressWarnings("unchecked")
                    List<Object[]> page = entityManager.createNativeQuery(
                            "SELECT f.fact_id, f.th_id, f.fact_code, f.fact_observations, f.fact_type, f.fact_subtotals, "
                                    + "f.fact_sales_tax, f.fact_withholding_source, f.descounts FROM FACTURES f "
                                    + "WHERE f.ent_id = :e AND f.fact_id > :a ORDER BY f.fact_id")
                            .setParameter("e", ent).setParameter("a", after).setMaxResults(size).getResultList();
                    return page;
                },
                r -> ((Number) r[0]).longValue(),
                page -> {
                    Map<Long, List<Long>> productsByFacture = new HashMap<>();
                    List<Long> ids = new ArrayList<>(page.size());
                    for (Object[] r : page) {
                        ids.add(((Number) r[0]).longValue());
                    }
                    @SuppressWarnings("unchecked")
                    List<Object[]> links = entityManager.createNativeQuery(
                            "SELECT fp.fact_id, fp.prod_id FROM FACTURES_AND_PRODUCTS fp WHERE fp.fact_id IN (:ids)")
                            .setParameter("ids", ids).getResultList();
                    for (Object[] l : links) {
                        productsByFacture.computeIfAbsent(((Number) l[0]).longValue(), k -> new ArrayList<>())
                                .add(((Number) l[1]).longValue());
                    }
                    List<LegacyFactureRow> rows = new ArrayList<>(page.size());
                    for (Object[] r : page) {
                        long id = ((Number) r[0]).longValue();
                        List<Long> productIds = productsByFacture.getOrDefault(id, new ArrayList<>());
                        productIds.sort(Long::compare);
                        rows.add(new LegacyFactureRow(id, r[1] == null ? null : ((Number) r[1]).longValue(),
                                r[2] == null ? null : ((Number) r[2]).longValue(), (String) r[3],
                                r[4] == null ? null : eFactureType.valueOf((String) r[4]), productIds,
                                dbl(r[5]), dbl(r[6]), dbl(r[7]), dbl(r[8])));
                    }
                    return rows;
                });
    }

    private Iterator<SkeletonFactureRow> skeletons(String ent) {
        return KeysetPager.<SkeletonFacture, SkeletonFactureRow>of(BATCH,
                (after, size) -> entityManager.createQuery(
                        "SELECT f FROM " + SKELETON + " f WHERE f.entId = :e AND f.id > :a ORDER BY f.id",
                        SkeletonFacture.class).setParameter("e", ent).setParameter("a", after).setMaxResults(size)
                        .getResultList(),
                SkeletonFacture::getId,
                page -> {
                    List<SkeletonFactureRow> rows = new ArrayList<>(page.size());
                    for (SkeletonFacture f : page) {
                        List<SkeletonProductRow> products = new ArrayList<>();
                        f.getProducts().stream().sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                                .forEach(p -> products.add(new SkeletonProductRow(p.getProductId(), p.getAmount(),
                                        p.getDescription(), p.getDiscount(), p.getUnitPrice(), p.getSubtotal(),
                                        p.getTaxPercentage() == null ? null : new ArrayList<>(p.getTaxPercentage()))));
                        rows.add(new SkeletonFactureRow(f.getId(), f.getFactCode(), f.getThId(), f.getTotalValue(),
                                f.getTotalPay(), f.getPendingValue(), f.getIssueDate(), f.getExpirationDate(),
                                f.getAccountingAccount(), f.getFactureType(), f.getPurchaseStatus(),
                                f.getVoidedAt(), products));
                    }
                    entityManager.clear();
                    return rows;
                });
    }

    private Iterator<SkeletonReturnRow> returns(String ent) {
        return KeysetPager.<SkeletonReturn, SkeletonReturnRow>of(BATCH,
                (after, size) -> entityManager.createQuery(
                        "SELECT r FROM " + RETURN + " r WHERE r.originalFactCode IN ("
                                + "SELECT f.factCode FROM " + SKELETON + " f WHERE f.entId = :e) AND r.id > :a ORDER BY r.id",
                        SkeletonReturn.class).setParameter("e", ent).setParameter("a", after).setMaxResults(size)
                        .getResultList(),
                SkeletonReturn::getId,
                page -> {
                    List<SkeletonReturnRow> rows = new ArrayList<>(page.size());
                    for (SkeletonReturn r : page) {
                        rows.add(new SkeletonReturnRow(r.getId(), r.getOriginalFactCode(), r.getProductId(),
                                r.getReturnedQuantity(), r.getReturnDate(), r.getReturnType(), r.getDetails()));
                    }
                    entityManager.clear();
                    return rows;
                });
    }

    /* ------------------------------------------------------------------ */
    /* Restore                                                            */
    /* ------------------------------------------------------------------ */

    @Override
    public void restore(BackupReader reader, IdMap idMap, RestoreContext ctx) {
        String ent = ctx.targetEnterpriseIdAsString();

        if (reader.has(ENTRY_LEGACY_PRODUCTS)) {
            reader.forEachBatch(ENTRY_LEGACY_PRODUCTS, LegacyProductRow.class, BATCH, rows -> {
                List<ProductEntity> entities = new ArrayList<>(rows.size());
                for (LegacyProductRow r : rows) {
                    entities.add(ProductEntity.builder()
                            .productId(idMap.requireLong(BackupKeys.PRODUCT, r.productId()))
                            .code(r.code())
                            .descount(r.descount())
                            .amount(r.amount())
                            .description(r.description())
                            .vat(r.vat())
                            .unitPrice(r.unitPrice())
                            .subtotal(r.subtotal())
                            .build());
                }
                legacyProductRepository.saveAll(entities);
                flushAndClear();
            });
        }

        if (reader.has(ENTRY_LEGACY_FACTURES)) {
            reader.forEachBatch(ENTRY_LEGACY_FACTURES, LegacyFactureRow.class, BATCH, rows -> {
                List<FactureEntity> entities = new ArrayList<>(rows.size());
                for (LegacyFactureRow r : rows) {
                    Set<ProductEntity> products = new HashSet<>();
                    if (r.productIds() != null) {
                        for (Long oldProductId : r.productIds()) {
                            products.add(entityManager.getReference(ProductEntity.class,
                                    idMap.requireLong(BackupKeys.PRODUCT, oldProductId)));
                        }
                    }
                    entities.add(FactureEntity.builder()
                            .entId(ent)
                            .thId(idMap.requireLong(BackupKeys.THIRD, r.thId()))
                            .factCode(uniques.factCode(idMap, r.factCode()))
                            .factObservations(r.factObservations())
                            .factureType(r.factureType())
                            .factProducts(products)
                            .factSubtotals(r.factSubtotals())
                            .facSalesTax(r.facSalesTax())
                            .facWithholdingSource(r.facWithholdingSource())
                            .descounts(r.descounts())
                            .build());
                }
                factureRepository.saveAll(entities);
                flushAndClear();
            });
        }

        if (reader.has(ENTRY_SKELETON_FACTURES)) {
            reader.forEachBatch(ENTRY_SKELETON_FACTURES, SkeletonFactureRow.class, BATCH, rows -> {
                List<SkeletonFacture> entities = new ArrayList<>(rows.size());
                for (SkeletonFactureRow r : rows) {
                    SkeletonFacture f = SkeletonFacture.builder()
                            .factCode(uniques.factCode(idMap, r.factCode()))
                            .entId(ent)
                            .thId(idMap.requireLong(BackupKeys.THIRD, r.thId()))
                            .totalValue(r.totalValue())
                            .totalPay(r.totalPay())
                            .pendingValue(r.pendingValue())
                            .issueDate(r.issueDate())
                            .expirationDate(r.expirationDate())
                            .accountingAccount(r.accountingAccount())
                            .factureType(r.factureType())
                            .purchaseStatus(r.purchaseStatus() == null ? PurchaseInvoiceStatus.ACTIVE : r.purchaseStatus())
                            .voidedAt(r.voidedAt())
                            .build();
                    if (r.products() != null) {
                        for (SkeletonProductRow p : r.products()) {
                            f.addProduct(SkeletonProduct.builder()
                                    .productId(idMap.requireLong(BackupKeys.PRODUCT, p.productId()))
                                    .amount(p.amount())
                                    .description(p.description())
                                    .discount(p.discount())
                                    .unitPrice(p.unitPrice())
                                    .subtotal(p.subtotal())
                                    .taxPercentage(p.taxPercentage())
                                    .build());
                        }
                    }
                    entities.add(f);
                }
                skeletonRepository.saveAll(entities);
                flushAndClear();
            });
        }

        if (reader.has(ENTRY_SKELETON_RETURNS)) {
            reader.forEachBatch(ENTRY_SKELETON_RETURNS, SkeletonReturnRow.class, BATCH, rows -> {
                List<SkeletonReturn> entities = new ArrayList<>(rows.size());
                for (SkeletonReturnRow r : rows) {
                    entities.add(SkeletonReturn.builder()
                            .originalFactCode(idMap.requireLong(AccountingBackupKeys.FACT_CODE, r.originalFactCode()))
                            .productId(idMap.requireLong(BackupKeys.PRODUCT, r.productId()))
                            .returnedQuantity(r.returnedQuantity())
                            .returnType(r.returnType())
                            .details(r.details())
                            .build());
                }
                returnRepository.saveAll(entities);
                entityManager.flush();
                // @PrePersist fija returnDate = ahora; se restituye la fecha original
                for (int i = 0; i < rows.size(); i++) {
                    if (rows.get(i).returnDate() != null) {
                        entityManager.createQuery("UPDATE " + RETURN + " r SET r.returnDate = :d WHERE r.id = :id")
                                .setParameter("d", rows.get(i).returnDate())
                                .setParameter("id", entities.get(i).getId()).executeUpdate();
                    }
                }
                flushAndClear();
            });
        }
    }

    /**
     * Restore en el lugar: borra devoluciones (por código de factura skeleton de la empresa), líneas y
     * facturas skeleton, y las facturas heredadas con sus vínculos y productos. SQL nativo directo (el
     * {@code hashCode} de Lombok de FactureEntity/ProductEntity revienta al cargarlas).
     */
    @Override
    public void purge(UUID enterpriseId) {
        String ent = enterpriseId.toString();
        entityManager.flush();
        // skeleton: devoluciones (sin empresa; se acotan por el código de las facturas de la empresa)
        TxSupport.purge(entityManager, "DELETE FROM skeleton_returns WHERE original_fact_code IN ("
                + "SELECT fact_code FROM skeleton_factures WHERE ent_id = :e)", ent);
        TxSupport.purge(entityManager, "DELETE FROM skeleton_products WHERE facture_id IN ("
                + "SELECT id FROM skeleton_factures WHERE ent_id = :e)", ent);
        TxSupport.purge(entityManager, "DELETE FROM skeleton_factures WHERE ent_id = :e", ent);

        // heredadas: PRODUCTS sin tenant ni empresa; se borran los referenciados SOLO por facturas de la empresa
        @SuppressWarnings("unchecked")
        List<Number> productIds = entityManager.createNativeQuery(
                "SELECT DISTINCT fp.prod_id FROM FACTURES_AND_PRODUCTS fp "
                        + "JOIN FACTURES f ON f.fact_id = fp.fact_id WHERE f.ent_id = :e")
                .setParameter("e", ent).getResultList();
        TxSupport.purge(entityManager, "DELETE FROM FACTURES_AND_PRODUCTS WHERE fact_id IN ("
                + "SELECT fact_id FROM FACTURES WHERE ent_id = :e)", ent);
        TxSupport.purge(entityManager, "DELETE FROM FACTURES WHERE ent_id = :e", ent);
        for (int i = 0; i < productIds.size(); i += BATCH) {
            List<Long> chunk = new ArrayList<>();
            for (Number n : productIds.subList(i, Math.min(i + BATCH, productIds.size()))) {
                chunk.add(n.longValue());
            }
            entityManager.createNativeQuery("DELETE FROM PRODUCTS WHERE prod_id IN (:ids) AND NOT EXISTS ("
                    + "SELECT 1 FROM FACTURES_AND_PRODUCTS fp WHERE fp.prod_id = PRODUCTS.prod_id)")
                    .setParameter("ids", chunk).executeUpdate();
        }
        TxSupport.flushAndClear(entityManager);
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
                ENTRY_LEGACY_PRODUCTS, count("SELECT COUNT(DISTINCT p.productId) FROM " + LEGACY_PRODUCT + " p JOIN p.factures f WHERE f.entId = :e", ent),
                ENTRY_LEGACY_FACTURES, count("SELECT COUNT(f) FROM " + LEGACY_FACTURE + " f WHERE f.entId = :e", ent),
                ENTRY_SKELETON_FACTURES, count("SELECT COUNT(f) FROM " + SKELETON + " f WHERE f.entId = :e", ent),
                ENTRY_SKELETON_RETURNS, count("SELECT COUNT(r) FROM " + RETURN + " r WHERE r.originalFactCode IN ("
                        + "SELECT f.factCode FROM " + SKELETON + " f WHERE f.entId = :e)", ent));
    }

    @Override
    public List<String> verifyIntegrity(UUID enterpriseId) {
        String ent = enterpriseId.toString();
        List<String> problems = new ArrayList<>();

        String lf = "FACTURES x";
        String lfW = "x.ent_id = :e";
        add(problems, checks.orphanThirds(lf, lfW, "x.th_id", ent),
                "facturas heredadas referencian un tercero que no es de la empresa destino");
        add(problems, checks.orphans("FACTURES_AND_PRODUCTS x JOIN FACTURES h ON h.fact_id = x.fact_id",
                "h.ent_id = :e", "x.prod_id", "product", "id", "enterprise_id", ent),
                "facturas heredadas referencian un producto que no es de la empresa destino");

        String sf = "skeleton_factures x";
        String sfW = "x.ent_id = :e AND x.tenant_id = :t";
        add(problems, checks.orphanThirds(sf, sfW, "x.th_id", ent), "facturas skeleton referencian un tercero que no es de la empresa destino");
        add(problems, checks.orphans("skeleton_products x JOIN skeleton_factures h ON h.id = x.facture_id",
                "h.ent_id = :e AND h.tenant_id = :t", "x.product_id", "product", "id", "enterprise_id", ent),
                "líneas de factura skeleton referencian un producto que no es de la empresa destino");
        add(problems, checks.orphans("skeleton_returns x",
                "x.original_fact_code IN (SELECT f.fact_code FROM skeleton_factures f WHERE f.ent_id = :e AND f.tenant_id = :t)",
                "x.product_id", "product", "id", "enterprise_id", ent),
                "devoluciones referencian un producto que no es de la empresa destino");
        return problems;
    }

    private static void add(List<String> problems, long n, String message) {
        if (n > 0) {
            problems.add(n + " " + message);
        }
    }

    private long count(String jpql, String ent) {
        return entityManager.createQuery(jpql, Long.class).setParameter("e", ent).getSingleResult();
    }
}
