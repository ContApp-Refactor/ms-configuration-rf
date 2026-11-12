package co.unicauca.edu.co.contables.accounting.backup.debtpayments;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.backup.support.AccountingBackupKeys;
import co.unicauca.edu.co.contables.accounting.backup.support.GlobalUniques;
import co.unicauca.edu.co.contables.accounting.backup.support.KeysetPager;
import co.unicauca.edu.co.contables.accounting.backup.support.ReferenceChecks;
import co.unicauca.edu.co.contables.accounting.backup.support.TxSupport;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.InvoiceStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.ReceiptType;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.WriteOffStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.ReceiptStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.PortfolioWriteOffEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.ReceiptDetailEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.ReceiptEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.WriteOffDetailEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.replicas.InvoiceReplicaEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.repository.IPortfolioWriteOffRepository;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.repository.IReceiptRepository;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.repository.replicas.IInvoiceRepository;
import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * @brief Participante {@code accounting.debtPayments} (order 100): cartera del módulo
 * {@code debt_payments}: réplicas de factura, recibos de caja (con sus detalles) y castigos de cartera
 * (con sus detalles).
 *
 * Entradas (orden de restore): {@code accounting/debt_payments/invoices.ndjson},
 * {@code .../receipts.ndjson}, {@code .../write_offs.ndjson}. Los detalles viajan anidados en la fila de
 * su cabecera. Namespaces registrados: {@code DP_INVOICE}, {@code DP_RECEIPT}, {@code DP_WRITE_OFF},
 * {@code RECEIPT_CODE} y {@code FACT_CODE}. Consume {@code THIRD}, {@code COST_CENTER}, {@code ACCOUNT},
 * {@code PAYMENT_METHOD}.
 *
 * Códigos de PUC ({@code accountingAccount}, {@code paymentMethodAccount}, {@code ledgerAccountId}) son el
 * CÓDIGO de la cuenta y el PUC se restaura con los mismos códigos: no se remapean (se verifican en
 * {@link #verifyIntegrity}). {@code receipts.receipt_code} es UNIQUE global: se conserva si está libre y si
 * no se regenera con el mismo formato {@code RC-<millis>}.
 *
 * NO se incluye {@code message_processing_errors}: es un registro técnico de errores de mensajería sin
 * dato de negocio de la empresa.
 */
@Component
@RequiredArgsConstructor
public class DebtPaymentsBackupParticipant implements BackupParticipant {

    public static final int SCHEMA_VERSION = 1;
    public static final String ENTRY_INVOICES = "accounting/debt_payments/invoices.ndjson";
    public static final String ENTRY_RECEIPTS = "accounting/debt_payments/receipts.ndjson";
    public static final String ENTRY_WRITE_OFFS = "accounting/debt_payments/write_offs.ndjson";

    private static final int BATCH = 500;

    private static final String INVOICE = InvoiceReplicaEntity.class.getName();
    private static final String RECEIPT = ReceiptEntity.class.getName();
    private static final String RECEIPT_DETAIL = ReceiptDetailEntity.class.getName();
    private static final String WRITE_OFF = PortfolioWriteOffEntity.class.getName();
    private static final String WRITE_OFF_DETAIL = WriteOffDetailEntity.class.getName();

    private final IInvoiceRepository invoiceRepository;
    private final IReceiptRepository receiptRepository;
    private final IPortfolioWriteOffRepository writeOffRepository;
    private final EntityManager entityManager;
    private final GlobalUniques uniques;
    private final ReferenceChecks checks;

    /** {@code thirdId} es el id VIEJO del tercero; {@code accountingAccount} es el código del PUC. */
    public record InvoiceRow(Long id, Long factCode, Long thirdId, Long totalValue, Long totalPay,
            Long pendingValue, LocalDate creationDate, LocalDate expirationDate, boolean active,
            Long accountingAccount, InvoiceStatus status) {
    }

    /** {@code invoiceId} es el id VIEJO de la réplica de cartera. */
    public record ReceiptDetailRow(Long invoiceId, String invoiceCode, Long accountingAccount, Long amountPaid) {
    }

    public record ReceiptRow(Long id, String receiptCode, Long thirdPartyId, Long paymentMethodId,
            Long paymentMethodAccount, ReceiptType receiptType, ReceiptStatus status, LocalDate issueDate,
            BigDecimal totalAmount, String observations, String voidReasonDescription,
            LocalDateTime voidDate, Long ledgerAccountId, Long centerCostId, List<ReceiptDetailRow> details) {
    }

    public record WriteOffDetailRow(Long invoiceId, Long amountWrittenOff, Long accountingAccount) {
    }

    public record WriteOffRow(Long id, String justification, String code, Long totalAmount,
            LocalDate writeOffDate, Long debitAuxiliaryAccount, Long debitAuxiliaryAccountId, Long costCenterId,
            Long thirdId, WriteOffStatus status, List<WriteOffDetailRow> details) {
    }

    @Override
    public String moduleKey() {
        return AccountingBackupKeys.MODULE_DEBT_PAYMENTS;
    }

    @Override
    public int order() {
        return AccountingBackupKeys.ORDER_DEBT_PAYMENTS;
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public Set<String> requires() {
        return Set.of(BackupKeys.MODULE_ENTERPRISE, BackupKeys.MODULE_CONFIGURATION, BackupKeys.MODULE_THIRDS,
                BackupKeys.MODULE_PUC, AccountingBackupKeys.MODULE_BANKING);
    }

    /* ------------------------------------------------------------------ */
    /* Export                                                             */
    /* ------------------------------------------------------------------ */

    @Override
    public void export(UUID enterpriseId, BackupWriter writer) {
        String ent = enterpriseId.toString();
        writer.writeNdjson(ENTRY_INVOICES, invoices(ent));
        writer.writeNdjson(ENTRY_RECEIPTS, receipts(ent));
        writer.writeNdjson(ENTRY_WRITE_OFFS, writeOffs(ent));
    }

    private Iterator<InvoiceRow> invoices(String ent) {
        return KeysetPager.<InvoiceReplicaEntity, InvoiceRow>of(BATCH,
                (after, size) -> entityManager.createQuery(
                        "SELECT i FROM " + INVOICE + " i WHERE i.entId = :e AND i.id > :a ORDER BY i.id",
                        InvoiceReplicaEntity.class).setParameter("e", ent).setParameter("a", after)
                        .setMaxResults(size).getResultList(),
                InvoiceReplicaEntity::getId,
                page -> {
                    List<InvoiceRow> rows = new ArrayList<>(page.size());
                    for (InvoiceReplicaEntity i : page) {
                        rows.add(new InvoiceRow(i.getId(), i.getFactCode(), i.getThirdId(), i.getTotalValue(),
                                i.getTotalPay(), i.getPendingValue(), i.getCreationDate(), i.getExpirationDate(),
                                i.isActive(), i.getAccountingAccount(), i.getStatus()));
                    }
                    entityManager.clear();
                    return rows;
                });
    }

    private Iterator<ReceiptRow> receipts(String ent) {
        return KeysetPager.<ReceiptEntity, ReceiptRow>of(BATCH,
                (after, size) -> entityManager.createQuery(
                        "SELECT r FROM " + RECEIPT + " r WHERE r.enterpriseId = :e AND r.id > :a ORDER BY r.id",
                        ReceiptEntity.class).setParameter("e", ent).setParameter("a", after)
                        .setMaxResults(size).getResultList(),
                ReceiptEntity::getId,
                page -> {
                    List<ReceiptRow> rows = new ArrayList<>(page.size());
                    for (ReceiptEntity r : page) {
                        List<ReceiptDetailRow> details = new ArrayList<>();
                        for (ReceiptDetailEntity d : r.getDetails()) {
                            details.add(new ReceiptDetailRow(d.getInvoiceId(), d.getInvoiceCode(),
                                    d.getAccountingAccount(), d.getAmountPaid()));
                        }
                        rows.add(new ReceiptRow(r.getId(), r.getReceiptCode(), r.getThirdPartyId(),
                                r.getPaymentMethodId(), r.getPaymentMethodAccount(), r.getReceiptType(),
                                r.getStatus(), r.getIssueDate(), r.getTotalAmount(), r.getObservations(),
                                r.getVoidReasonDescription(), r.getVoidDate(), r.getLedgerAccountId(),
                                r.getCenterCostId(), details));
                    }
                    entityManager.clear();
                    return rows;
                });
    }

    private Iterator<WriteOffRow> writeOffs(String ent) {
        return KeysetPager.<PortfolioWriteOffEntity, WriteOffRow>of(BATCH,
                (after, size) -> entityManager.createQuery(
                        "SELECT w FROM " + WRITE_OFF + " w WHERE w.enterpriseId = :e AND w.id > :a ORDER BY w.id",
                        PortfolioWriteOffEntity.class).setParameter("e", ent).setParameter("a", after)
                        .setMaxResults(size).getResultList(),
                PortfolioWriteOffEntity::getId,
                page -> {
                    List<WriteOffRow> rows = new ArrayList<>(page.size());
                    for (PortfolioWriteOffEntity w : page) {
                        List<WriteOffDetailRow> details = new ArrayList<>();
                        if (w.getDetails() != null) {
                            for (WriteOffDetailEntity d : w.getDetails()) {
                                details.add(new WriteOffDetailRow(d.getInvoiceId(), d.getAmountWrittenOff(),
                                        d.getAccountingAccount()));
                            }
                        }
                        rows.add(new WriteOffRow(w.getId(), w.getJustification(), w.getCode(), w.getTotalAmount(),
                                w.getWriteOffDate(), w.getDebitAuxiliaryAccount(), w.getDebitAuxiliaryAccountId(),
                                w.getCostCenterId(), w.getThirdId(), w.getStatus(), details));
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
        Set<String> usedReceiptCodes = new HashSet<>();
        AtomicLong codeSeq = new AtomicLong();

        if (reader.has(ENTRY_INVOICES)) {
            reader.forEachBatch(ENTRY_INVOICES, InvoiceRow.class, BATCH, rows -> {
                List<InvoiceReplicaEntity> entities = new ArrayList<>(rows.size());
                for (InvoiceRow r : rows) {
                    InvoiceReplicaEntity e = new InvoiceReplicaEntity();
                    e.setFactCode(uniques.factCode(idMap, r.factCode()));
                    e.setEntId(ent);
                    e.setThirdId(idMap.requireLong(BackupKeys.THIRD, r.thirdId()));
                    e.setTotalValue(r.totalValue());
                    e.setTotalPay(r.totalPay());
                    e.setPendingValue(r.pendingValue());
                    e.setCreationDate(r.creationDate());
                    e.setExpirationDate(r.expirationDate());
                    e.setActive(r.active());
                    e.setAccountingAccount(r.accountingAccount());
                    e.setStatus(r.status());
                    entities.add(e);
                }
                invoiceRepository.saveAll(entities);
                for (int i = 0; i < rows.size(); i++) {
                    idMap.put(AccountingBackupKeys.DP_INVOICE, rows.get(i).id(), entities.get(i).getId());
                }
                flushAndClear();
            });
        }

        if (reader.has(ENTRY_RECEIPTS)) {
            reader.forEachBatch(ENTRY_RECEIPTS, ReceiptRow.class, BATCH, rows -> {
                List<ReceiptEntity> entities = new ArrayList<>(rows.size());
                for (ReceiptRow r : rows) {
                    ReceiptEntity e = new ReceiptEntity();
                    String code = uniques.freeCode("receipts", "receipt_code", r.receiptCode(),
                            () -> "RC-" + (System.currentTimeMillis() + codeSeq.incrementAndGet()), usedReceiptCodes);
                    idMap.put(AccountingBackupKeys.RECEIPT_CODE, r.receiptCode(), code);
                    e.setReceiptCode(code);
                    e.setThirdPartyId(idMap.requireLong(BackupKeys.THIRD, r.thirdPartyId()));
                    e.setEnterpriseId(ent);
                    e.setPaymentMethodId(idMap.requireLong(AccountingBackupKeys.PAYMENT_METHOD, r.paymentMethodId()));
                    e.setPaymentMethodAccount(r.paymentMethodAccount());
                    e.setReceiptType(r.receiptType());
                    e.setStatus(r.status());
                    e.setIssueDate(r.issueDate());
                    e.setTotalAmount(r.totalAmount());
                    e.setObservations(r.observations());
                    e.setVoidReasonDescription(r.voidReasonDescription());
                    e.setVoidDate(r.voidDate());
                    e.setLedgerAccountId(r.ledgerAccountId());
                    e.setCenterCostId(idMap.requireNullable(BackupKeys.COST_CENTER, r.centerCostId()));
                    if (r.details() != null) {
                        for (ReceiptDetailRow d : r.details()) {
                            ReceiptDetailEntity de = new ReceiptDetailEntity();
                            de.setInvoiceId(idMap.requireLong(AccountingBackupKeys.DP_INVOICE, d.invoiceId()));
                            de.setInvoiceCode(d.invoiceCode());
                            de.setAccountingAccount(d.accountingAccount());
                            de.setAmountPaid(d.amountPaid());
                            de.setReceipt(e);
                            e.getDetails().add(de);
                        }
                    }
                    entities.add(e);
                }
                receiptRepository.saveAll(entities);
                for (int i = 0; i < rows.size(); i++) {
                    idMap.put(AccountingBackupKeys.DP_RECEIPT, rows.get(i).id(), entities.get(i).getId());
                }
                flushAndClear();
            });
        }

        if (reader.has(ENTRY_WRITE_OFFS)) {
            reader.forEachBatch(ENTRY_WRITE_OFFS, WriteOffRow.class, BATCH, rows -> {
                List<PortfolioWriteOffEntity> entities = new ArrayList<>(rows.size());
                for (WriteOffRow r : rows) {
                    PortfolioWriteOffEntity e = new PortfolioWriteOffEntity();
                    e.setJustification(r.justification());
                    e.setCode(r.code());
                    e.setTotalAmount(r.totalAmount());
                    e.setWriteOffDate(r.writeOffDate());
                    e.setDebitAuxiliaryAccount(r.debitAuxiliaryAccount());
                    e.setDebitAuxiliaryAccountId(idMap.requireLong(BackupKeys.ACCOUNT, r.debitAuxiliaryAccountId()));
                    e.setCostCenterId(idMap.requireNullable(BackupKeys.COST_CENTER, r.costCenterId()));
                    e.setThirdId(idMap.requireLong(BackupKeys.THIRD, r.thirdId()));
                    e.setStatus(r.status());
                    e.setEnterpriseId(ent);
                    List<WriteOffDetailEntity> details = new ArrayList<>();
                    if (r.details() != null) {
                        for (WriteOffDetailRow d : r.details()) {
                            WriteOffDetailEntity de = new WriteOffDetailEntity();
                            de.setInvoiceId(idMap.requireLong(AccountingBackupKeys.DP_INVOICE, d.invoiceId()));
                            de.setAmountWrittenOff(d.amountWrittenOff());
                            de.setAccountingAccount(d.accountingAccount());
                            de.setPortfolioWriteOff(e);
                            details.add(de);
                        }
                    }
                    e.setDetails(details);
                    entities.add(e);
                }
                writeOffRepository.saveAll(entities);
                for (int i = 0; i < rows.size(); i++) {
                    idMap.put(AccountingBackupKeys.DP_WRITE_OFF, rows.get(i).id(), entities.get(i).getId());
                }
                flushAndClear();
            });
        }
    }

    /**
     * Restore en el lugar: detalles de castigos y recibos, castigos, recibos y réplicas de factura de la
     * empresa (hijos antes que padres). SQL nativo directo, sin servicios ni auditoría.
     */
    @Override
    public void purge(UUID enterpriseId) {
        String ent = enterpriseId.toString();
        entityManager.flush();
        TxSupport.purge(entityManager, "DELETE FROM write_off_details WHERE write_off_id IN ("
                + "SELECT id FROM portfolio_write_offs WHERE enterprise_id = :e)", ent);
        TxSupport.purge(entityManager, "DELETE FROM portfolio_write_offs WHERE enterprise_id = :e", ent);
        TxSupport.purge(entityManager, "DELETE FROM receipt_details WHERE receipt_id IN ("
                + "SELECT id FROM receipts WHERE enterprise_id = :e)", ent);
        TxSupport.purge(entityManager, "DELETE FROM receipts WHERE enterprise_id = :e", ent);
        TxSupport.purge(entityManager, "DELETE FROM invoice_replica WHERE ent_id = :e", ent);
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
                ENTRY_INVOICES, count("SELECT COUNT(i) FROM " + INVOICE + " i WHERE i.entId = :e", ent),
                ENTRY_RECEIPTS, count("SELECT COUNT(r) FROM " + RECEIPT + " r WHERE r.enterpriseId = :e", ent),
                ENTRY_WRITE_OFFS, count("SELECT COUNT(w) FROM " + WRITE_OFF + " w WHERE w.enterpriseId = :e", ent));
    }

    @Override
    public List<String> verifyIntegrity(UUID enterpriseId) {
        String ent = enterpriseId.toString();
        List<String> problems = new ArrayList<>();

        // detalles -> réplica de factura de la misma empresa
        add(problems, count("SELECT COUNT(d) FROM " + RECEIPT_DETAIL + " d WHERE d.receipt.enterpriseId = :e AND NOT EXISTS ("
                + "SELECT i.id FROM " + INVOICE + " i WHERE i.id = d.invoiceId AND i.entId = :e)", ent),
                "detalles de recibo de cartera referencian una factura que no es de la empresa destino");
        add(problems, count("SELECT COUNT(d) FROM " + WRITE_OFF_DETAIL + " d WHERE d.portfolioWriteOff.enterpriseId = :e AND NOT EXISTS ("
                + "SELECT i.id FROM " + INVOICE + " i WHERE i.id = d.invoiceId AND i.entId = :e)", ent),
                "detalles de castigo de cartera referencian una factura que no es de la empresa destino");

        // terceros, centros de costo y cuentas (ids) de la empresa destino
        String inv = "invoice_replica x";
        String invW = "x.ent_id = :e AND x.tenant_id = :t";
        add(problems, checks.orphanThirds(inv, invW, "x.third_id", ent), "réplicas de factura referencian un tercero que no es de la empresa destino");
        add(problems, checks.orphanAccountCodes(inv, invW, "x.accounting_account", ent), "réplicas de factura referencian un código de cuenta inexistente en el PUC destino");

        String rec = "receipts x";
        String recW = "x.enterprise_id = :e AND x.tenant_id = :t";
        add(problems, checks.orphanThirds(rec, recW, "x.third_party_id", ent), "recibos de cartera referencian un tercero que no es de la empresa destino");
        add(problems, checks.orphanCostCenters(rec, recW, "x.center_cost", ent), "recibos de cartera referencian un centro de costo que no es de la empresa destino");
        add(problems, checks.orphanAccountCodes(rec, recW, "x.payment_method_account", ent), "recibos de cartera referencian un código de cuenta (medio de pago) inexistente en el PUC destino");
        add(problems, checks.orphanAccountCodes(rec, recW, "x.ledger_account", ent), "recibos de cartera referencian un código de cuenta (ingreso directo) inexistente en el PUC destino");
        add(problems, checks.orphans(rec, recW, "x.payment_method_id", "payment_methods", "id", "id_enterprise", ent), "recibos de cartera referencian un medio de pago que no es de la empresa destino");
        add(problems, checks.orphanAccountCodes("receipt_details x JOIN receipts h ON h.id = x.receipt_id",
                "h.enterprise_id = :e AND h.tenant_id = :t", "x.accounting_account", ent),
                "detalles de recibo de cartera referencian un código de cuenta inexistente en el PUC destino");

        String wo = "portfolio_write_offs x";
        String woW = "x.enterprise_id = :e AND x.tenant_id = :t";
        add(problems, checks.orphanThirds(wo, woW, "x.third_id", ent), "castigos de cartera referencian un tercero que no es de la empresa destino");
        add(problems, checks.orphanCostCenters(wo, woW, "x.cost_center_id", ent), "castigos de cartera referencian un centro de costo que no es de la empresa destino");
        add(problems, checks.orphanAccountIds(wo, woW, "x.debit_auxiliary_account_id", ent), "castigos de cartera referencian una cuenta del PUC que no es de la empresa destino");
        add(problems, checks.orphanAccountCodes(wo, woW, "x.debit_auxiliary_account", ent), "castigos de cartera referencian un código de cuenta inexistente en el PUC destino");
        add(problems, checks.orphanAccountCodes("write_off_details x JOIN portfolio_write_offs h ON h.id = x.write_off_id",
                "h.enterprise_id = :e AND h.tenant_id = :t", "x.accounting_account", ent),
                "detalles de castigo de cartera referencian un código de cuenta inexistente en el PUC destino");
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
