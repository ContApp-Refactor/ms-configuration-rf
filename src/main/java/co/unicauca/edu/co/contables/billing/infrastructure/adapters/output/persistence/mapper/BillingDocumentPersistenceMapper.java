package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.mapper;

import co.unicauca.edu.co.contables.billing.application.ports.input.view.BillingDocumentSummary;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocument;
import co.unicauca.edu.co.contables.billing.domain.model.BillingDocumentState;
import co.unicauca.edu.co.contables.billing.domain.model.CounterpartyRole;
import co.unicauca.edu.co.contables.billing.domain.model.CounterpartySnapshot;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentLine;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentNumber;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentStatus;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentTotals;
import co.unicauca.edu.co.contables.billing.domain.model.DocumentType;
import co.unicauca.edu.co.contables.billing.domain.model.IssuerSnapshot;
import co.unicauca.edu.co.contables.billing.domain.model.Money;
import co.unicauca.edu.co.contables.billing.domain.model.OriginalDocumentRef;
import co.unicauca.edu.co.contables.billing.domain.model.Quantity;
import co.unicauca.edu.co.contables.billing.domain.model.TaxRate;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity.BillingDocumentEntity;
import co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity.BillingDocumentLineEntity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Conversion manual dominio <-> entidad. Reglas:
 * <ul>
 *   <li>Los enums viajan como String plano (D10).</li>
 *   <li>Los totales de cabecera salen de {@code document.totals()} (derivados de las lineas).</li>
 *   <li>Las lineas se FUSIONAN por {@code lineNo} (D9): las existentes se actualizan en sitio, las
 *       nuevas se agregan y las que ya no estan se retiran (orphanRemoval). Un reemplazo total
 *       violaria {@code uk_billing_line_no} porque Hibernate ejecuta los inserts antes que los deletes.</li>
 *   <li>Ni el tenant ni la version se copian al entity: los administra Hibernate.</li>
 *   <li>Al reconstruir, el tercero y el emisor quedan {@code active=true}: la tabla guarda el
 *       snapshot congelado y la emision siempre vuelve a leer el tercero fresco, asi que el flag solo
 *       importa en el momento de leer el catalogo, no despues de persistir.</li>
 * </ul>
 */
@Component("billingDocumentPersistenceMapper")
public class BillingDocumentPersistenceMapper {

    // ---------- dominio -> entidad ----------

    public BillingDocumentEntity newEntity(BillingDocument document) {
        BillingDocumentEntity entity = new BillingDocumentEntity();
        apply(document, entity);
        return entity;
    }

    /** Copia el estado del dominio sobre la entidad (nueva o ya gestionada). */
    public void apply(BillingDocument document, BillingDocumentEntity entity) {
        entity.setId(document.id());
        entity.setEnterpriseId(document.enterpriseId());
        entity.setDocumentType(document.type().name());
        entity.setStatus(document.status().name());

        DocumentNumber number = document.number();
        entity.setNumberPrefix(number == null ? null : number.prefix());
        entity.setNumberValue(number == null ? null : number.value());
        entity.setNumberDisplay(number == null ? null : number.display());

        entity.setIssueDate(document.issueDate());

        CounterpartySnapshot counterparty = document.counterparty();
        entity.setCounterpartyId(counterparty == null ? null : counterparty.thirdId());
        entity.setCounterpartyRole(counterparty == null ? null : counterparty.role().name());
        entity.setCounterpartyName(counterparty == null ? null : counterparty.name());
        entity.setCounterpartyIdType(counterparty == null ? null : counterparty.idType());
        entity.setCounterpartyIdNumber(counterparty == null ? null : counterparty.idNumber());
        entity.setCounterpartyVerification(counterparty == null ? null : counterparty.verificationNumber());
        entity.setCounterpartyAddress(counterparty == null ? null : counterparty.address());
        entity.setCounterpartyEmail(counterparty == null ? null : counterparty.email());
        entity.setCounterpartyPhone(counterparty == null ? null : counterparty.phone());

        entity.setSupplierInvoiceNumber(document.supplierInvoiceNumber());

        IssuerSnapshot issuer = document.issuer();
        entity.setIssuerName(issuer == null ? null : issuer.name());
        entity.setIssuerNit(issuer == null ? null : issuer.nit());
        entity.setIssuerDv(issuer == null ? null : issuer.dv());

        OriginalDocumentRef original = document.original();
        entity.setOriginalDocumentId(original == null ? null : original.id());
        entity.setOriginalDocumentType(original == null ? null : original.type().name());
        entity.setOriginalNumberDisplay(original == null || original.number() == null
                ? null : original.number().display());
        entity.setOriginalIssueDate(original == null ? null : original.issueDate());

        entity.setReturnReason(document.returnReason());
        entity.setCurrency(document.currency());

        DocumentTotals totals = document.totals();
        entity.setSubtotal(totals.subtotal().value());
        entity.setTaxTotal(totals.tax().value());
        entity.setTotal(totals.total().value());

        entity.setCreatedAt(document.createdAt());
        entity.setCreatedBy(document.createdBy());
        entity.setUpdatedAt(document.updatedAt());
        entity.setUpdatedBy(document.updatedBy());
        entity.setEmittedAt(document.emittedAt());
        entity.setEmittedBy(document.emittedBy());
        entity.setAnnulledAt(document.annulledAt());
        entity.setAnnulledBy(document.annulledBy());
        entity.setAnnulReason(document.annulReason());

        mergeLines(document.lines(), entity);
    }

    private void mergeLines(List<DocumentLine> lines, BillingDocumentEntity entity) {
        Map<Integer, BillingDocumentLineEntity> existing = new HashMap<>();
        for (BillingDocumentLineEntity row : entity.getLines()) {
            existing.put(row.getLineNo(), row);
        }
        Set<Integer> kept = new HashSet<>();
        for (DocumentLine line : lines) {
            BillingDocumentLineEntity row = existing.get(line.lineNo());
            if (row == null) {
                row = new BillingDocumentLineEntity();
                row.setDocument(entity);
                entity.getLines().add(row);
            }
            copyLine(line, row);
            kept.add(line.lineNo());
        }
        entity.getLines().removeIf(row -> !kept.contains(row.getLineNo()));
    }

    private void copyLine(DocumentLine line, BillingDocumentLineEntity row) {
        row.setLineNo(line.lineNo());
        row.setSourceLineNo(line.sourceLineNo());
        row.setProductId(line.productId());
        row.setProductCode(line.productCode());
        row.setProductName(line.productName());
        row.setUnitOfMeasureId(line.unitOfMeasureId());
        row.setUnitOfMeasureName(line.unitOfMeasureName());
        row.setQuantity(line.quantity().value());
        row.setCatalogUnitPrice(line.catalogUnitPrice().value());
        row.setCatalogTaxRate(line.catalogTaxRate().value());
        row.setUnitPriceOverride(line.unitPriceOverride() == null ? null : line.unitPriceOverride().value());
        row.setTaxRateOverride(line.taxRateOverride() == null ? null : line.taxRateOverride().value());
        row.setUnitPrice(line.unitPrice().value());
        row.setTaxRate(line.taxRate().value());
        row.setSubtotal(line.subtotal().value());
        row.setTaxAmount(line.taxAmount().value());
        row.setTotal(line.total().value());
    }

    // ---------- entidad -> dominio ----------

    /** Reconstruye el agregado; requiere sesion abierta si las lineas aun son perezosas. */
    public BillingDocument toDomain(BillingDocumentEntity entity) {
        List<BillingDocumentLineEntity> rows = new ArrayList<>(entity.getLines());
        rows.sort(Comparator.comparingInt(BillingDocumentLineEntity::getLineNo));
        List<DocumentLine> lines = new ArrayList<>(rows.size());
        for (BillingDocumentLineEntity row : rows) {
            lines.add(toLine(row));
        }
        return BillingDocument.restore(new BillingDocumentState(
                entity.getId(),
                entity.getEnterpriseId(),
                DocumentType.valueOf(entity.getDocumentType()),
                DocumentStatus.valueOf(entity.getStatus()),
                number(entity.getNumberPrefix(), entity.getNumberValue()),
                entity.getIssueDate(),
                counterparty(entity),
                entity.getSupplierInvoiceNumber(),
                issuer(entity),
                original(entity),
                entity.getReturnReason(),
                lines,
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy(),
                entity.getEmittedAt(),
                entity.getEmittedBy(),
                entity.getAnnulledAt(),
                entity.getAnnulledBy(),
                entity.getAnnulReason(),
                entity.getVersion()));
    }

    /** Resumen armado solo con las columnas de cabecera (no toca las lineas). */
    public BillingDocumentSummary toSummary(BillingDocumentEntity entity) {
        OriginalDocumentRef original = original(entity);
        return new BillingDocumentSummary(
                entity.getId(),
                DocumentType.valueOf(entity.getDocumentType()),
                DocumentStatus.valueOf(entity.getStatus()),
                number(entity.getNumberPrefix(), entity.getNumberValue()),
                entity.getIssueDate(),
                entity.getCounterpartyId(),
                entity.getCounterpartyName(),
                entity.getSupplierInvoiceNumber(),
                entity.getOriginalDocumentId(),
                original == null ? null : original.number(),
                new DocumentTotals(Money.of(entity.getSubtotal()), Money.of(entity.getTaxTotal()),
                        Money.of(entity.getTotal())),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getEmittedAt(),
                entity.getAnnulledAt(),
                entity.getVersion());
    }

    private DocumentLine toLine(BillingDocumentLineEntity row) {
        return DocumentLine.restore(
                row.getLineNo(),
                row.getSourceLineNo(),
                row.getProductId(),
                row.getProductCode(),
                row.getProductName(),
                row.getUnitOfMeasureId(),
                row.getUnitOfMeasureName(),
                Quantity.of(row.getQuantity()),
                Money.of(row.getCatalogUnitPrice()),
                TaxRate.of(row.getCatalogTaxRate()),
                row.getUnitPriceOverride() == null ? null : Money.of(row.getUnitPriceOverride()),
                row.getTaxRateOverride() == null ? null : TaxRate.of(row.getTaxRateOverride()));
    }

    private static DocumentNumber number(String prefix, Long value) {
        return value == null ? null : new DocumentNumber(prefix, value);
    }

    private static CounterpartySnapshot counterparty(BillingDocumentEntity entity) {
        if (entity.getCounterpartyId() == null) {
            return null;
        }
        return new CounterpartySnapshot(
                entity.getCounterpartyId(),
                CounterpartyRole.valueOf(entity.getCounterpartyRole()),
                entity.getCounterpartyName(),
                entity.getCounterpartyIdType(),
                entity.getCounterpartyIdNumber(),
                entity.getCounterpartyVerification(),
                entity.getCounterpartyAddress(),
                entity.getCounterpartyEmail(),
                entity.getCounterpartyPhone(),
                true);
    }

    private static IssuerSnapshot issuer(BillingDocumentEntity entity) {
        if (entity.getIssuerName() == null && entity.getIssuerNit() == null && entity.getIssuerDv() == null) {
            return null;
        }
        return new IssuerSnapshot(entity.getEnterpriseId(), entity.getIssuerName(), entity.getIssuerNit(),
                entity.getIssuerDv(), true);
    }

    private static OriginalDocumentRef original(BillingDocumentEntity entity) {
        if (entity.getOriginalDocumentId() == null) {
            return null;
        }
        return new OriginalDocumentRef(
                entity.getOriginalDocumentId(),
                DocumentType.valueOf(entity.getOriginalDocumentType()),
                parseDisplay(entity.getOriginalNumberDisplay()),
                entity.getOriginalIssueDate());
    }

    /** Inversa de {@link DocumentNumber#display()}: "FV-000123" -> (FV, 123). */
    private static DocumentNumber parseDisplay(String display) {
        if (display == null || display.isBlank()) {
            return null;
        }
        int separator = display.lastIndexOf('-');
        if (separator <= 0 || separator == display.length() - 1) {
            throw new IllegalStateException("Numero de documento persistido invalido: " + display);
        }
        return new DocumentNumber(display.substring(0, separator),
                Long.parseLong(display.substring(separator + 1)));
    }
}
