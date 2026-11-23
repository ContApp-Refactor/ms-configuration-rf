package co.unicauca.edu.co.contables.accounting.backup.treasury;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.backup.support.AccountingBackupKeys;
import co.unicauca.edu.co.contables.accounting.backup.support.KeysetPager;
import co.unicauca.edu.co.contables.accounting.backup.support.ReferenceChecks;
import co.unicauca.edu.co.contables.accounting.backup.support.TxSupport;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentScheduleStatus;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentVoucherStatus;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.WriteOffStatus;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.jpa.entity.DueDateHistoryEntity;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.jpa.entity.PayableWriteOffDetailEntity;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.jpa.entity.PayableWriteOffEntity;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.jpa.entity.PaymentScheduleDetailEntity;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.jpa.entity.PaymentScheduleEntity;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.jpa.entity.PaymentVoucherDetailEntity;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.jpa.entity.PaymentVoucherEntity;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.jpa.entity.SupplierInvoiceReplicaEntity;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.jpa.repository.IDueDateHistoryRepository;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.jpa.repository.IPayableWriteOffRepository;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.jpa.repository.IPaymentScheduleRepository;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.jpa.repository.IPaymentVoucherRepository;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.jpa.repository.ISupplierInvoiceRepository;
import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * @brief Participante {@code accounting.treasury} (order 120): tesorería de la empresa. Réplicas de
 * factura de proveedor, comprobantes de egreso, programaciones de pago, castigos de cuentas por pagar e
 * historial de cambios de vencimiento.
 *
 * Entradas (orden de restore): {@code accounting/treasury/supplier_invoices.ndjson},
 * {@code .../payment_vouchers.ndjson}, {@code .../payment_schedules.ndjson},
 * {@code .../payable_write_offs.ndjson}, {@code .../due_date_history.ndjson}. Los detalles viajan anidados.
 * Namespaces registrados: {@code SUPPLIER_INVOICE}, {@code PAYMENT_VOUCHER}. Consume {@code THIRD},
 * {@code ACCOUNT}, {@code PAYMENT_METHOD}, {@code BANK_ACCOUNT} y {@code ACC_ENTRY} (los comprobantes y
 * castigos apuntan al asiento que generaron).
 *
 * Se conservan los números de comprobante ({@code voucher_number}, único por tenant+empresa, así que en la
 * empresa nueva no chocan). Se anulan las {@code idempotency_key} (pertenecen a reintentos de la operación
 * original) y {@code last_event_id} de las réplicas de proveedor. {@code source_invoice_id} es el id de la
 * factura de compra en el microservicio de facturación: es una etiqueta externa y se copia tal cual.
 *
 * NO se incluyen {@code outbox_events} ni {@code processed_events}: son infraestructura de mensajería
 * (eventos pendientes de publicar y deduplicación), no datos de la empresa; copiarlos reenviaría eventos.
 * Los {@code createdAt/updatedAt} de los documentos los fija {@code @PrePersist} a la fecha del restore.
 */
@Component
@RequiredArgsConstructor
public class TreasuryBackupParticipant implements BackupParticipant {

    public static final int SCHEMA_VERSION = 1;
    public static final String ENTRY_SUPPLIER_INVOICES = "accounting/treasury/supplier_invoices.ndjson";
    public static final String ENTRY_VOUCHERS = "accounting/treasury/payment_vouchers.ndjson";
    public static final String ENTRY_SCHEDULES = "accounting/treasury/payment_schedules.ndjson";
    public static final String ENTRY_WRITE_OFFS = "accounting/treasury/payable_write_offs.ndjson";
    public static final String ENTRY_DUE_DATE_HISTORY = "accounting/treasury/due_date_history.ndjson";

    private static final int BATCH = 500;

    private static final String SUPPLIER_INVOICE = SupplierInvoiceReplicaEntity.class.getName();
    private static final String VOUCHER = PaymentVoucherEntity.class.getName();
    private static final String SCHEDULE = PaymentScheduleEntity.class.getName();
    private static final String WRITE_OFF = PayableWriteOffEntity.class.getName();
    private static final String HISTORY = DueDateHistoryEntity.class.getName();

    private final ISupplierInvoiceRepository supplierInvoiceRepository;
    private final IPaymentVoucherRepository voucherRepository;
    private final IPaymentScheduleRepository scheduleRepository;
    private final IPayableWriteOffRepository writeOffRepository;
    private final IDueDateHistoryRepository historyRepository;
    private final EntityManager entityManager;
    private final ReferenceChecks checks;

    public record SupplierInvoiceRow(Long id, Long sourceInvoiceId, String reference, Long supplierId,
            BigDecimal originalAmount, BigDecimal paidAmount, BigDecimal pendingAmount, BigDecimal reservedAmount,
            LocalDate issueDate, LocalDate originalDueDate, LocalDate dueDate, boolean dueDateOverridden,
            Long payableAccountId, String payableAccountCode, boolean active) {
    }

    /** {@code invoiceId} / {@code payableAccountId} / {@code supplierId} son ids VIEJOS. */
    public record VoucherDetailRow(Long supplierId, Long invoiceId, String invoiceReference,
            Long payableAccountId, String payableAccountCode, BigDecimal previousBalance, BigDecimal amountPaid,
            BigDecimal remainingBalance) {
    }

    public record VoucherRow(Long id, String voucherNumber, LocalDate issueDate, PaymentVoucherStatus status,
            Long paymentMethodId, Long bankAccountId, BigDecimal total, String observations,
            Long accountingEntryId, String failureReason, String voidReason, List<VoucherDetailRow> details) {
    }

    public record ScheduleDetailRow(Long supplierId, Long invoiceId, BigDecimal amount, boolean canceled,
            String cancellationReason) {
    }

    public record ScheduleRow(Long id, LocalDate executionDate, Long paymentMethodId, Long bankAccountId,
            String observations, PaymentScheduleStatus status, Long voucherId, int retryCount,
            String failureReason, List<ScheduleDetailRow> details) {
    }

    public record WriteOffDetailRow(Long supplierId, Long invoiceId, Long payableAccountId,
            String payableAccountCode, BigDecimal amount) {
    }

    public record WriteOffRow(Long id, String reason, Long counterpartAccountId, String counterpartAccountCode,
            BigDecimal total, WriteOffStatus status, Long accountingEntryId, List<WriteOffDetailRow> details) {
    }

    public record DueDateHistoryRow(Long id, Long invoiceId, LocalDate previousDate, LocalDate newDate,
            String reason, Instant changedAt, String changedBy) {
    }

    @Override
    public String moduleKey() {
        return AccountingBackupKeys.MODULE_TREASURY;
    }

    @Override
    public int order() {
        return AccountingBackupKeys.ORDER_TREASURY;
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public Set<String> requires() {
        return Set.of(BackupKeys.MODULE_ENTERPRISE, BackupKeys.MODULE_THIRDS, BackupKeys.MODULE_PUC,
                AccountingBackupKeys.MODULE_BANKING, AccountingBackupKeys.MODULE_LEDGER);
    }

    /* ------------------------------------------------------------------ */
    /* Export                                                             */
    /* ------------------------------------------------------------------ */

    @Override
    public void export(UUID enterpriseId, BackupWriter writer) {
        String ent = enterpriseId.toString();
        writer.writeNdjson(ENTRY_SUPPLIER_INVOICES, supplierInvoices(ent));
        writer.writeNdjson(ENTRY_VOUCHERS, vouchers(ent));
        writer.writeNdjson(ENTRY_SCHEDULES, schedules(ent));
        writer.writeNdjson(ENTRY_WRITE_OFFS, writeOffs(ent));
        writer.writeNdjson(ENTRY_DUE_DATE_HISTORY, history(ent));
    }

    private <T> List<T> page(Class<T> type, String entity, String alias, String where, String ent, long after, int size) {
        return entityManager.createQuery("SELECT " + alias + " FROM " + entity + " " + alias + " WHERE " + where
                + " AND " + alias + ".id > :a ORDER BY " + alias + ".id", type)
                .setParameter("e", ent).setParameter("a", after).setMaxResults(size).getResultList();
    }

    private Iterator<SupplierInvoiceRow> supplierInvoices(String ent) {
        return KeysetPager.<SupplierInvoiceReplicaEntity, SupplierInvoiceRow>of(BATCH,
                (after, size) -> page(SupplierInvoiceReplicaEntity.class, SUPPLIER_INVOICE, "s", "s.enterpriseId = :e", ent, after, size),
                SupplierInvoiceReplicaEntity::getId,
                list -> {
                    List<SupplierInvoiceRow> rows = new ArrayList<>(list.size());
                    for (SupplierInvoiceReplicaEntity s : list) {
                        rows.add(new SupplierInvoiceRow(s.getId(), s.getSourceInvoiceId(), s.getReference(),
                                s.getSupplierId(), s.getOriginalAmount(), s.getPaidAmount(), s.getPendingAmount(),
                                s.getReservedAmount(), s.getIssueDate(), s.getOriginalDueDate(), s.getDueDate(),
                                s.isDueDateOverridden(), s.getPayableAccountId(), s.getPayableAccountCode(),
                                s.isActive()));
                    }
                    entityManager.clear();
                    return rows;
                });
    }

    private Iterator<VoucherRow> vouchers(String ent) {
        return KeysetPager.<PaymentVoucherEntity, VoucherRow>of(BATCH,
                (after, size) -> page(PaymentVoucherEntity.class, VOUCHER, "v", "v.enterpriseId = :e", ent, after, size),
                PaymentVoucherEntity::getId,
                list -> {
                    List<VoucherRow> rows = new ArrayList<>(list.size());
                    for (PaymentVoucherEntity v : list) {
                        List<VoucherDetailRow> details = new ArrayList<>();
                        for (PaymentVoucherDetailEntity d : v.getDetails()) {
                            details.add(new VoucherDetailRow(d.getSupplierId(), d.getInvoiceId(),
                                    d.getInvoiceReference(), d.getPayableAccountId(), d.getPayableAccountCode(),
                                    d.getPreviousBalance(), d.getAmountPaid(), d.getRemainingBalance()));
                        }
                        rows.add(new VoucherRow(v.getId(), v.getVoucherNumber(), v.getIssueDate(), v.getStatus(),
                                v.getPaymentMethodId(), v.getBankAccountId(), v.getTotal(), v.getObservations(),
                                v.getAccountingEntryId(), v.getFailureReason(), v.getVoidReason(), details));
                    }
                    entityManager.clear();
                    return rows;
                });
    }

    private Iterator<ScheduleRow> schedules(String ent) {
        return KeysetPager.<PaymentScheduleEntity, ScheduleRow>of(BATCH,
                (after, size) -> page(PaymentScheduleEntity.class, SCHEDULE, "p", "p.enterpriseId = :e", ent, after, size),
                PaymentScheduleEntity::getId,
                list -> {
                    List<ScheduleRow> rows = new ArrayList<>(list.size());
                    for (PaymentScheduleEntity p : list) {
                        List<ScheduleDetailRow> details = new ArrayList<>();
                        for (PaymentScheduleDetailEntity d : p.getDetails()) {
                            details.add(new ScheduleDetailRow(d.getSupplierId(), d.getInvoiceId(), d.getAmount(),
                                    d.isCanceled(), d.getCancellationReason()));
                        }
                        rows.add(new ScheduleRow(p.getId(), p.getExecutionDate(), p.getPaymentMethodId(),
                                p.getBankAccountId(), p.getObservations(), p.getStatus(), p.getVoucherId(),
                                p.getRetryCount(), p.getFailureReason(), details));
                    }
                    entityManager.clear();
                    return rows;
                });
    }

    private Iterator<WriteOffRow> writeOffs(String ent) {
        return KeysetPager.<PayableWriteOffEntity, WriteOffRow>of(BATCH,
                (after, size) -> page(PayableWriteOffEntity.class, WRITE_OFF, "w", "w.enterpriseId = :e", ent, after, size),
                PayableWriteOffEntity::getId,
                list -> {
                    List<WriteOffRow> rows = new ArrayList<>(list.size());
                    for (PayableWriteOffEntity w : list) {
                        List<WriteOffDetailRow> details = new ArrayList<>();
                        for (PayableWriteOffDetailEntity d : w.getDetails()) {
                            details.add(new WriteOffDetailRow(d.getSupplierId(), d.getInvoiceId(),
                                    d.getPayableAccountId(), d.getPayableAccountCode(), d.getAmount()));
                        }
                        rows.add(new WriteOffRow(w.getId(), w.getReason(), w.getCounterpartAccountId(),
                                w.getCounterpartAccountCode(), w.getTotal(), w.getStatus(),
                                w.getAccountingEntryId(), details));
                    }
                    entityManager.clear();
                    return rows;
                });
    }

    private Iterator<DueDateHistoryRow> history(String ent) {
        String where = "h.invoiceId IN (SELECT s.id FROM " + SUPPLIER_INVOICE + " s WHERE s.enterpriseId = :e)";
        return KeysetPager.<DueDateHistoryEntity, DueDateHistoryRow>of(BATCH,
                (after, size) -> page(DueDateHistoryEntity.class, HISTORY, "h", where, ent, after, size),
                DueDateHistoryEntity::getId,
                list -> {
                    List<DueDateHistoryRow> rows = new ArrayList<>(list.size());
                    for (DueDateHistoryEntity h : list) {
                        rows.add(new DueDateHistoryRow(h.getId(), h.getInvoiceId(), h.getPreviousDate(),
                                h.getNewDate(), h.getReason(), h.getChangedAt(), h.getChangedBy()));
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

        if (reader.has(ENTRY_SUPPLIER_INVOICES)) {
            reader.forEachBatch(ENTRY_SUPPLIER_INVOICES, SupplierInvoiceRow.class, BATCH, rows -> {
                List<SupplierInvoiceReplicaEntity> entities = new ArrayList<>(rows.size());
                for (SupplierInvoiceRow r : rows) {
                    SupplierInvoiceReplicaEntity e = new SupplierInvoiceReplicaEntity();
                    e.setSourceInvoiceId(r.sourceInvoiceId());
                    e.setReference(r.reference());
                    e.setEnterpriseId(ent);
                    e.setSupplierId(idMap.requireLong(BackupKeys.THIRD, r.supplierId()));
                    e.setOriginalAmount(r.originalAmount());
                    e.setPaidAmount(r.paidAmount());
                    e.setPendingAmount(r.pendingAmount());
                    e.setReservedAmount(r.reservedAmount());
                    e.setIssueDate(r.issueDate());
                    e.setOriginalDueDate(r.originalDueDate());
                    e.setDueDate(r.dueDate());
                    e.setDueDateOverridden(r.dueDateOverridden());
                    e.setPayableAccountId(idMap.requireLong(BackupKeys.ACCOUNT, r.payableAccountId()));
                    e.setPayableAccountCode(r.payableAccountCode());
                    e.setActive(r.active());
                    entities.add(e);
                }
                supplierInvoiceRepository.saveAll(entities);
                for (int i = 0; i < rows.size(); i++) {
                    idMap.put(AccountingBackupKeys.SUPPLIER_INVOICE, rows.get(i).id(), entities.get(i).getId());
                }
                flushAndClear();
            });
        }

        if (reader.has(ENTRY_VOUCHERS)) {
            reader.forEachBatch(ENTRY_VOUCHERS, VoucherRow.class, BATCH, rows -> {
                List<PaymentVoucherEntity> entities = new ArrayList<>(rows.size());
                for (VoucherRow r : rows) {
                    PaymentVoucherEntity e = new PaymentVoucherEntity();
                    e.setVoucherNumber(r.voucherNumber());
                    e.setEnterpriseId(ent);
                    e.setIssueDate(r.issueDate());
                    e.setStatus(r.status());
                    e.setPaymentMethodId(idMap.requireLong(AccountingBackupKeys.PAYMENT_METHOD, r.paymentMethodId()));
                    e.setBankAccountId(idMap.requireNullable(AccountingBackupKeys.BANK_ACCOUNT, r.bankAccountId()));
                    e.setTotal(r.total());
                    e.setObservations(r.observations());
                    e.setIdempotencyKey(null);
                    e.setAccountingEntryId(idMap.requireNullable(AccountingBackupKeys.ACC_ENTRY, r.accountingEntryId()));
                    e.setFailureReason(r.failureReason());
                    e.setVoidReason(r.voidReason());
                    if (r.details() != null) {
                        for (VoucherDetailRow d : r.details()) {
                            PaymentVoucherDetailEntity de = new PaymentVoucherDetailEntity();
                            de.setSupplierId(idMap.requireLong(BackupKeys.THIRD, d.supplierId()));
                            de.setInvoiceId(idMap.requireLong(AccountingBackupKeys.SUPPLIER_INVOICE, d.invoiceId()));
                            de.setInvoiceReference(d.invoiceReference());
                            de.setPayableAccountId(idMap.requireLong(BackupKeys.ACCOUNT, d.payableAccountId()));
                            de.setPayableAccountCode(d.payableAccountCode());
                            de.setPreviousBalance(d.previousBalance());
                            de.setAmountPaid(d.amountPaid());
                            de.setRemainingBalance(d.remainingBalance());
                            de.setPaymentVoucher(e);
                            e.getDetails().add(de);
                        }
                    }
                    entities.add(e);
                }
                voucherRepository.saveAll(entities);
                for (int i = 0; i < rows.size(); i++) {
                    idMap.put(AccountingBackupKeys.PAYMENT_VOUCHER, rows.get(i).id(), entities.get(i).getId());
                }
                flushAndClear();
            });
        }

        if (reader.has(ENTRY_SCHEDULES)) {
            reader.forEachBatch(ENTRY_SCHEDULES, ScheduleRow.class, BATCH, rows -> {
                List<PaymentScheduleEntity> entities = new ArrayList<>(rows.size());
                for (ScheduleRow r : rows) {
                    PaymentScheduleEntity e = new PaymentScheduleEntity();
                    e.setEnterpriseId(ent);
                    e.setExecutionDate(r.executionDate());
                    e.setPaymentMethodId(idMap.requireLong(AccountingBackupKeys.PAYMENT_METHOD, r.paymentMethodId()));
                    e.setBankAccountId(idMap.requireNullable(AccountingBackupKeys.BANK_ACCOUNT, r.bankAccountId()));
                    e.setObservations(r.observations());
                    e.setStatus(r.status());
                    e.setVoucherId(idMap.requireNullable(AccountingBackupKeys.PAYMENT_VOUCHER, r.voucherId()));
                    e.setRetryCount(r.retryCount());
                    e.setFailureReason(r.failureReason());
                    if (r.details() != null) {
                        for (ScheduleDetailRow d : r.details()) {
                            PaymentScheduleDetailEntity de = new PaymentScheduleDetailEntity();
                            de.setSupplierId(idMap.requireLong(BackupKeys.THIRD, d.supplierId()));
                            de.setInvoiceId(idMap.requireLong(AccountingBackupKeys.SUPPLIER_INVOICE, d.invoiceId()));
                            de.setAmount(d.amount());
                            de.setCanceled(d.canceled());
                            de.setCancellationReason(d.cancellationReason());
                            de.setSchedule(e);
                            e.getDetails().add(de);
                        }
                    }
                    entities.add(e);
                }
                scheduleRepository.saveAll(entities);
                flushAndClear();
            });
        }

        if (reader.has(ENTRY_WRITE_OFFS)) {
            reader.forEachBatch(ENTRY_WRITE_OFFS, WriteOffRow.class, BATCH, rows -> {
                List<PayableWriteOffEntity> entities = new ArrayList<>(rows.size());
                for (WriteOffRow r : rows) {
                    PayableWriteOffEntity e = new PayableWriteOffEntity();
                    e.setEnterpriseId(ent);
                    e.setReason(r.reason());
                    e.setCounterpartAccountId(idMap.requireLong(BackupKeys.ACCOUNT, r.counterpartAccountId()));
                    e.setCounterpartAccountCode(r.counterpartAccountCode());
                    e.setTotal(r.total());
                    e.setStatus(r.status());
                    e.setAccountingEntryId(idMap.requireNullable(AccountingBackupKeys.ACC_ENTRY, r.accountingEntryId()));
                    if (r.details() != null) {
                        for (WriteOffDetailRow d : r.details()) {
                            PayableWriteOffDetailEntity de = new PayableWriteOffDetailEntity();
                            de.setSupplierId(idMap.requireLong(BackupKeys.THIRD, d.supplierId()));
                            de.setInvoiceId(idMap.requireLong(AccountingBackupKeys.SUPPLIER_INVOICE, d.invoiceId()));
                            de.setPayableAccountId(idMap.requireLong(BackupKeys.ACCOUNT, d.payableAccountId()));
                            de.setPayableAccountCode(d.payableAccountCode());
                            de.setAmount(d.amount());
                            de.setWriteOff(e);
                            e.getDetails().add(de);
                        }
                    }
                    entities.add(e);
                }
                writeOffRepository.saveAll(entities);
                flushAndClear();
            });
        }

        if (reader.has(ENTRY_DUE_DATE_HISTORY)) {
            reader.forEachBatch(ENTRY_DUE_DATE_HISTORY, DueDateHistoryRow.class, BATCH, rows -> {
                List<DueDateHistoryEntity> entities = new ArrayList<>(rows.size());
                for (DueDateHistoryRow r : rows) {
                    DueDateHistoryEntity e = new DueDateHistoryEntity();
                    e.setInvoiceId(idMap.requireLong(AccountingBackupKeys.SUPPLIER_INVOICE, r.invoiceId()));
                    e.setPreviousDate(r.previousDate());
                    e.setNewDate(r.newDate());
                    e.setReason(r.reason());
                    e.setChangedAt(r.changedAt() == null ? Instant.now() : r.changedAt());
                    e.setChangedBy(r.changedBy());
                    entities.add(e);
                }
                historyRepository.saveAll(entities);
                flushAndClear();
            });
        }
    }

    /**
     * Restore en el lugar: historial de vencimientos, castigos por pagar, programaciones, comprobantes
     * (con sus detalles) y por último las facturas de proveedor de la empresa. SQL nativo directo.
     */
    @Override
    public void purge(UUID enterpriseId) {
        String ent = enterpriseId.toString();
        entityManager.flush();
        TxSupport.purge(entityManager, "DELETE FROM due_date_history WHERE invoice_id IN ("
                + "SELECT id FROM supplier_invoice_replicas WHERE enterprise_id = :e)", ent);
        TxSupport.purge(entityManager, "DELETE FROM payable_write_off_details WHERE write_off_id IN ("
                + "SELECT id FROM payable_write_offs WHERE enterprise_id = :e)", ent);
        TxSupport.purge(entityManager, "DELETE FROM payable_write_offs WHERE enterprise_id = :e", ent);
        TxSupport.purge(entityManager, "DELETE FROM payment_schedule_details WHERE schedule_id IN ("
                + "SELECT id FROM payment_schedules WHERE enterprise_id = :e)", ent);
        TxSupport.purge(entityManager, "DELETE FROM payment_schedules WHERE enterprise_id = :e", ent);
        TxSupport.purge(entityManager, "DELETE FROM payment_voucher_details WHERE voucher_id IN ("
                + "SELECT id FROM payment_vouchers WHERE enterprise_id = :e)", ent);
        TxSupport.purge(entityManager, "DELETE FROM payment_vouchers WHERE enterprise_id = :e", ent);
        TxSupport.purge(entityManager, "DELETE FROM supplier_invoice_replicas WHERE enterprise_id = :e", ent);
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
                ENTRY_SUPPLIER_INVOICES, count("SELECT COUNT(s) FROM " + SUPPLIER_INVOICE + " s WHERE s.enterpriseId = :e", ent),
                ENTRY_VOUCHERS, count("SELECT COUNT(v) FROM " + VOUCHER + " v WHERE v.enterpriseId = :e", ent),
                ENTRY_SCHEDULES, count("SELECT COUNT(p) FROM " + SCHEDULE + " p WHERE p.enterpriseId = :e", ent),
                ENTRY_WRITE_OFFS, count("SELECT COUNT(w) FROM " + WRITE_OFF + " w WHERE w.enterpriseId = :e", ent),
                ENTRY_DUE_DATE_HISTORY, count("SELECT COUNT(h) FROM " + HISTORY + " h WHERE h.invoiceId IN ("
                        + "SELECT s.id FROM " + SUPPLIER_INVOICE + " s WHERE s.enterpriseId = :e)", ent));
    }

    @Override
    public List<String> verifyIntegrity(UUID enterpriseId) {
        String ent = enterpriseId.toString();
        List<String> problems = new ArrayList<>();

        String si = "supplier_invoice_replicas x";
        String siW = "x.enterprise_id = :e AND x.tenant_id = :t";
        add(problems, checks.orphanThirds(si, siW, "x.supplier_id", ent), "facturas de proveedor referencian un tercero que no es de la empresa destino");
        add(problems, checks.orphanAccountIds(si, siW, "x.payable_account_id", ent), "facturas de proveedor referencian una cuenta del PUC que no es de la empresa destino");

        String v = "payment_vouchers x";
        String vW = "x.enterprise_id = :e AND x.tenant_id = :t";
        add(problems, checks.orphans(v, vW, "x.payment_method_id", "payment_methods", "id", "id_enterprise", ent), "comprobantes de egreso referencian un medio de pago que no es de la empresa destino");
        add(problems, checks.orphans(v, vW, "x.bank_account_id", "bank_accounts", "id", "id_enterprise", ent), "comprobantes de egreso referencian una cuenta bancaria que no es de la empresa destino");
        add(problems, checks.orphans(v, vW, "x.accounting_entry_id", "accounting_entries", "id", "id_enterprise", ent), "comprobantes de egreso referencian un asiento que no es de la empresa destino");
        String vd = "payment_voucher_details x JOIN payment_vouchers h ON h.id = x.voucher_id";
        String vdW = "h.enterprise_id = :e AND h.tenant_id = :t";
        add(problems, checks.orphans(vd, vdW, "x.invoice_id", "supplier_invoice_replicas", "id", "enterprise_id", ent), "detalles de comprobante referencian una factura de proveedor que no es de la empresa destino");
        add(problems, checks.orphanThirds(vd, vdW, "x.supplier_id", ent), "detalles de comprobante referencian un tercero que no es de la empresa destino");
        add(problems, checks.orphanAccountIds(vd, vdW, "x.payable_account_id", ent), "detalles de comprobante referencian una cuenta del PUC que no es de la empresa destino");

        String ps = "payment_schedules x";
        String psW = "x.enterprise_id = :e AND x.tenant_id = :t";
        add(problems, checks.orphans(ps, psW, "x.payment_method_id", "payment_methods", "id", "id_enterprise", ent), "programaciones de pago referencian un medio de pago que no es de la empresa destino");
        add(problems, checks.orphans(ps, psW, "x.bank_account_id", "bank_accounts", "id", "id_enterprise", ent), "programaciones de pago referencian una cuenta bancaria que no es de la empresa destino");
        add(problems, checks.orphans(ps, psW, "x.voucher_id", "payment_vouchers", "id", "enterprise_id", ent), "programaciones de pago referencian un comprobante que no es de la empresa destino");
        String pd = "payment_schedule_details x JOIN payment_schedules h ON h.id = x.schedule_id";
        String pdW = "h.enterprise_id = :e AND h.tenant_id = :t";
        add(problems, checks.orphans(pd, pdW, "x.invoice_id", "supplier_invoice_replicas", "id", "enterprise_id", ent), "detalles de programación referencian una factura de proveedor que no es de la empresa destino");
        add(problems, checks.orphanThirds(pd, pdW, "x.supplier_id", ent), "detalles de programación referencian un tercero que no es de la empresa destino");

        String wo = "payable_write_offs x";
        String woW = "x.enterprise_id = :e AND x.tenant_id = :t";
        add(problems, checks.orphanAccountIds(wo, woW, "x.counterpart_account_id", ent), "castigos de cuentas por pagar referencian una cuenta del PUC que no es de la empresa destino");
        add(problems, checks.orphans(wo, woW, "x.accounting_entry_id", "accounting_entries", "id", "id_enterprise", ent), "castigos de cuentas por pagar referencian un asiento que no es de la empresa destino");
        String wd = "payable_write_off_details x JOIN payable_write_offs h ON h.id = x.write_off_id";
        String wdW = "h.enterprise_id = :e AND h.tenant_id = :t";
        add(problems, checks.orphans(wd, wdW, "x.invoice_id", "supplier_invoice_replicas", "id", "enterprise_id", ent), "detalles de castigo por pagar referencian una factura de proveedor que no es de la empresa destino");
        add(problems, checks.orphanThirds(wd, wdW, "x.supplier_id", ent), "detalles de castigo por pagar referencian un tercero que no es de la empresa destino");
        add(problems, checks.orphanAccountIds(wd, wdW, "x.payable_account_id", ent), "detalles de castigo por pagar referencian una cuenta del PUC que no es de la empresa destino");

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
