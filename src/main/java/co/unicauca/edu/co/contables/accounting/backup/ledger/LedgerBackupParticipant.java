package co.unicauca.edu.co.contables.accounting.backup.ledger;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.AccountingEntryStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.InvoiceStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.ProcessingStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.SourceDocumentType;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.AccountingEntryEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.AccountingMovementEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.InvoiceReplicaEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.ReceiptDetailEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.ReceiptEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.repository.IAccountingEntryRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.repository.IInvoiceRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.repository.IReceiptRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.commons.backup.BackupIntegrityException;
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
 * @brief Participante {@code accounting.ledger} (order 110): el libro contable de la empresa.
 * Réplicas contables de factura, recibos contables (con detalles) y asientos (con sus líneas de
 * movimiento).
 *
 * Entradas (orden de restore): {@code accounting/ledger/invoices.ndjson},
 * {@code accounting/ledger/receipts.ndjson}, {@code accounting/ledger/entries.ndjson}. Las líneas
 * ({@code accounting_movements}) y los detalles viajan anidados en la fila del asiento / recibo.
 * Namespaces registrados: {@code ACC_INVOICE}, {@code ACC_RECEIPT}, {@code ACC_ENTRY}. Consume
 * {@code ACCOUNT}, {@code THIRD}, {@code COST_CENTER}, {@code PAYMENT_METHOD}, {@code DP_RECEIPT} y
 * {@code DP_WRITE_OFF} (por eso corre después de {@code accounting.debtPayments}).
 *
 * <b>Decisión D5 (saldos del PUC)</b>. {@code Account.amount} es un acumulado que hoy mantiene el código
 * de dos formas: (a) {@code AccountBalanceUpdateService} suma cada línea de asiento ACTIVO a la cuenta y a
 * TODOS sus ancestros según la naturaleza de CADA cuenta (los asientos anulados se revierten, es decir,
 * netean 0), y (b) {@code InvoiceProcessService} suma el valor de cada factura de venta como débito a su
 * cuenta por cobrar (por CÓDIGO) y ancestros. El participante PUC restaura {@code amount = 0}. Al final de
 * este restore se RECALCULAN los saldos de TODAS las cuentas de la empresa destino aplicando exactamente
 * esa regla sobre los asientos y réplicas restaurados (no se confía en el saldo del origen, que podría
 * haber derivado). Así, por construcción, saldo == suma de asientos activos + facturas, y
 * {@link #verifyIntegrity} lo comprueba. Aproximación documentada: la contribución de cada factura se
 * toma como su {@code totalValue} (valor pendiente al crearse). Los {@code usageCount} de las cuentas los
 * decide el participante PUC.
 *
 * Unicidad global: {@code accounting_entries.code} es UNIQUE en toda la BD; se conserva el código si está
 * libre y si no se regenera con el mismo prefijo ({@code AE-}/{@code AC-} + 8 hex).
 * {@code accounting_receipts.original_receipt_id} (UNIQUE) se remapea al nuevo id de recibo de cartera.
 */
@Component
@RequiredArgsConstructor
public class LedgerBackupParticipant implements BackupParticipant {

    public static final int SCHEMA_VERSION = 1;
    public static final String ENTRY_INVOICES = "accounting/ledger/invoices.ndjson";
    public static final String ENTRY_RECEIPTS = "accounting/ledger/receipts.ndjson";
    public static final String ENTRY_ENTRIES = "accounting/ledger/entries.ndjson";

    private static final int BATCH = 500;

    private static final String INVOICE = InvoiceReplicaEntity.class.getName();
    private static final String RECEIPT = ReceiptEntity.class.getName();
    private static final String RECEIPT_DETAIL = ReceiptDetailEntity.class.getName();
    private static final String ENTRY = AccountingEntryEntity.class.getName();
    private static final String MOVEMENT = AccountingMovementEntity.class.getName();
    private static final String ACCOUNT = AccountCatalogueEntity.class.getName();

    private final IInvoiceRepository invoiceRepository;
    private final IReceiptRepository receiptRepository;
    private final IAccountingEntryRepository entryRepository;
    private final EntityManager entityManager;
    private final GlobalUniques uniques;
    private final ReferenceChecks checks;

    public record InvoiceRow(Long id, Long factCode, Long thirdId, BigDecimal totalValue, BigDecimal totalPay,
            BigDecimal pendingValue, LocalDate creationDate, LocalDate expirationDate, boolean active,
            Long accountingAccount, InvoiceStatus status) {
    }

    /** {@code originalInvoiceId} es el id VIEJO de la réplica contable. */
    public record ReceiptDetailRow(Long originalInvoiceId, String invoiceCode, Long accountingAccount,
            BigDecimal amountPaid) {
    }

    /** {@code originalReceiptId} es el id VIEJO del recibo de cartera. */
    public record ReceiptRow(Long id, Long originalReceiptId, String receiptCode, Long receiptTypeId,
            Long thirdPartyId, Long paymentMethodId, Long paymentMethodAccount, Long ledgerAccountId,
            String status, LocalDate issueDate, BigDecimal totalAmount, String observations,
            ProcessingStatus processingStatus, Long centerCostId, List<ReceiptDetailRow> details) {
    }

    /** {@code account} y {@code thirdPartyId} son ids VIEJOS. */
    public record MovementRow(Long account, Long thirdPartyId, String description, BigDecimal debit,
            BigDecimal credit) {
    }

    /** {@code sourceDocumentId} es id VIEJO: del recibo contable (RECEIPT) o del castigo de cartera (PORTFOLIO_WRITEOFF). */
    public record EntryRow(Long id, String code, LocalDate date, String description, AccountingEntryStatus status,
            Long sourceDocumentId, String type, Long centerCostId, List<MovementRow> movements) {
    }

    @Override
    public String moduleKey() {
        return AccountingBackupKeys.MODULE_LEDGER;
    }

    @Override
    public int order() {
        return AccountingBackupKeys.ORDER_LEDGER;
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public Set<String> requires() {
        return Set.of(BackupKeys.MODULE_ENTERPRISE, BackupKeys.MODULE_CONFIGURATION, BackupKeys.MODULE_THIRDS,
                BackupKeys.MODULE_PUC, AccountingBackupKeys.MODULE_BANKING,
                AccountingBackupKeys.MODULE_DEBT_PAYMENTS);
    }

    /* ------------------------------------------------------------------ */
    /* Export                                                             */
    /* ------------------------------------------------------------------ */

    @Override
    public void export(UUID enterpriseId, BackupWriter writer) {
        String ent = enterpriseId.toString();
        writer.writeNdjson(ENTRY_INVOICES, invoices(ent));
        writer.writeNdjson(ENTRY_RECEIPTS, receipts(ent));
        writer.writeNdjson(ENTRY_ENTRIES, entries(ent));
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
                        if (r.getDetails() != null) {
                            for (ReceiptDetailEntity d : r.getDetails()) {
                                details.add(new ReceiptDetailRow(d.getOriginalInvoiceId(), d.getInvoiceCode(),
                                        d.getAccountingAccount(), d.getAmountPaid()));
                            }
                        }
                        rows.add(new ReceiptRow(r.getId(), r.getOriginalReceiptId(), r.getReceiptCode(),
                                r.getReceiptTypeId(), r.getThirdPartyId(), r.getPaymentMethodId(),
                                r.getPaymentMethodAccount(), r.getLedgerAccountId(), r.getStatus(),
                                r.getIssueDate(), r.getTotalAmount(), r.getObservations(),
                                r.getProcessingStatus(), r.getCenterCostId(), details));
                    }
                    entityManager.clear();
                    return rows;
                });
    }

    private Iterator<EntryRow> entries(String ent) {
        return KeysetPager.<AccountingEntryEntity, EntryRow>of(BATCH,
                (after, size) -> entityManager.createQuery(
                        "SELECT e FROM " + ENTRY + " e WHERE e.idEnterprise = :e AND e.id > :a ORDER BY e.id",
                        AccountingEntryEntity.class).setParameter("e", ent).setParameter("a", after)
                        .setMaxResults(size).getResultList(),
                AccountingEntryEntity::getId,
                page -> {
                    List<EntryRow> rows = new ArrayList<>(page.size());
                    for (AccountingEntryEntity e : page) {
                        List<MovementRow> movements = new ArrayList<>();
                        if (e.getMovements() != null) {
                            e.getMovements().stream()
                                    .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                                    .forEach(m -> movements.add(new MovementRow(m.getAccount(), m.getThirdPartyId(),
                                            m.getDescription(), m.getDebit(), m.getCredit())));
                        }
                        rows.add(new EntryRow(e.getId(), e.getCode(), e.getDate(), e.getDescription(),
                                e.getStatus(), e.getSourceDocumentId(), e.getType(), e.getCenterCostId(), movements));
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
        Set<String> usedEntryCodes = new HashSet<>();

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
                    idMap.put(AccountingBackupKeys.ACC_INVOICE, rows.get(i).id(), entities.get(i).getId());
                }
                flushAndClear();
            });
        }

        if (reader.has(ENTRY_RECEIPTS)) {
            reader.forEachBatch(ENTRY_RECEIPTS, ReceiptRow.class, BATCH, rows -> {
                List<ReceiptEntity> entities = new ArrayList<>(rows.size());
                for (ReceiptRow r : rows) {
                    ReceiptEntity e = new ReceiptEntity();
                    e.setOriginalReceiptId(idMap.requireLong(AccountingBackupKeys.DP_RECEIPT, r.originalReceiptId()));
                    // El código es el mismo del recibo de cartera: si allí se regeneró, aquí también.
                    e.setReceiptCode(idMap.<String>find(AccountingBackupKeys.RECEIPT_CODE, r.receiptCode())
                            .orElse(r.receiptCode()));
                    e.setEnterpriseId(ent);
                    e.setReceiptTypeId(r.receiptTypeId());
                    e.setThirdPartyId(idMap.requireLong(BackupKeys.THIRD, r.thirdPartyId()));
                    e.setPaymentMethodId(idMap.requireLong(AccountingBackupKeys.PAYMENT_METHOD, r.paymentMethodId()));
                    e.setPaymentMethodAccount(r.paymentMethodAccount());
                    e.setLedgerAccountId(r.ledgerAccountId());
                    e.setStatus(r.status());
                    e.setIssueDate(r.issueDate());
                    e.setTotalAmount(r.totalAmount());
                    e.setObservations(r.observations());
                    e.setProcessingStatus(r.processingStatus());
                    e.setCenterCostId(idMap.requireNullable(BackupKeys.COST_CENTER, r.centerCostId()));
                    List<ReceiptDetailEntity> details = new ArrayList<>();
                    if (r.details() != null) {
                        for (ReceiptDetailRow d : r.details()) {
                            ReceiptDetailEntity de = new ReceiptDetailEntity();
                            de.setOriginalInvoiceId(
                                    idMap.requireLong(AccountingBackupKeys.ACC_INVOICE, d.originalInvoiceId()));
                            de.setInvoiceCode(d.invoiceCode());
                            de.setAccountingAccount(d.accountingAccount());
                            de.setAmountPaid(d.amountPaid());
                            de.setReceipt(e);
                            details.add(de);
                        }
                    }
                    e.setDetails(details);
                    entities.add(e);
                }
                receiptRepository.saveAll(entities);
                for (int i = 0; i < rows.size(); i++) {
                    idMap.put(AccountingBackupKeys.ACC_RECEIPT, rows.get(i).id(), entities.get(i).getId());
                }
                flushAndClear();
            });
        }

        if (reader.has(ENTRY_ENTRIES)) {
            reader.forEachBatch(ENTRY_ENTRIES, EntryRow.class, BATCH, rows -> {
                List<AccountingEntryEntity> entities = new ArrayList<>(rows.size());
                for (EntryRow r : rows) {
                    AccountingEntryEntity e = new AccountingEntryEntity();
                    e.setIdEnterprise(ent);
                    e.setCode(uniques.freeCode("accounting_entries", "code", r.code(),
                            () -> prefixOf(r.code()) + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                            usedEntryCodes));
                    e.setDate(r.date());
                    e.setDescription(r.description());
                    e.setStatus(r.status());
                    e.setSourceDocumentId(remapSource(idMap, r));
                    e.setType(r.type());
                    e.setCenterCostId(idMap.requireNullable(BackupKeys.COST_CENTER, r.centerCostId()));
                    List<AccountingMovementEntity> movements = new ArrayList<>();
                    if (r.movements() != null) {
                        for (MovementRow m : r.movements()) {
                            AccountingMovementEntity me = new AccountingMovementEntity();
                            me.setAccount(idMap.requireLong(BackupKeys.ACCOUNT, m.account()));
                            me.setThirdPartyId(idMap.requireLong(BackupKeys.THIRD, m.thirdPartyId()));
                            me.setDescription(m.description());
                            me.setDebit(m.debit());
                            me.setCredit(m.credit());
                            me.setAccountingEntry(e);
                            movements.add(me);
                        }
                    }
                    e.setMovements(movements);
                    entities.add(e);
                }
                entryRepository.saveAll(entities);
                for (int i = 0; i < rows.size(); i++) {
                    idMap.put(AccountingBackupKeys.ACC_ENTRY, rows.get(i).id(), entities.get(i).getId());
                }
                flushAndClear();
            });
        }

        recalculateBalances(ent);
    }

    /**
     * Restore en el lugar: líneas y asientos, detalles y recibos contables, y réplicas contables de factura
     * de la empresa (hijos antes que padres). NO recalcula saldos del PUC: el restore posterior lo hace.
     */
    @Override
    public void purge(UUID enterpriseId) {
        String ent = enterpriseId.toString();
        entityManager.flush();
        TxSupport.purge(entityManager, "DELETE FROM accounting_movements WHERE accounting_entry_id IN ("
                + "SELECT id FROM accounting_entries WHERE id_enterprise = :e)", ent);
        TxSupport.purge(entityManager, "DELETE FROM accounting_entries WHERE id_enterprise = :e", ent);
        TxSupport.purge(entityManager, "DELETE FROM accounting_receipt_details WHERE receipt_id IN ("
                + "SELECT id FROM accounting_receipts WHERE enterprise_id = :e)", ent);
        TxSupport.purge(entityManager, "DELETE FROM accounting_receipts WHERE enterprise_id = :e", ent);
        TxSupport.purge(entityManager, "DELETE FROM accounting_invoice_replica WHERE ent_id = :e", ent);
        TxSupport.flushAndClear(entityManager);
    }

    private static String prefixOf(String code) {
        int dash = code == null ? -1 : code.indexOf('-');
        return dash < 0 ? "AE-" : code.substring(0, dash + 1);
    }

    private Long remapSource(IdMap idMap, EntryRow r) {
        String type = r.type();
        if (SourceDocumentType.RECEIPT.name().equals(type)) {
            return idMap.requireLong(AccountingBackupKeys.ACC_RECEIPT, r.sourceDocumentId());
        }
        if (SourceDocumentType.PORTFOLIO_WRITEOFF.name().equals(type)) {
            return idMap.requireLong(AccountingBackupKeys.DP_WRITE_OFF, r.sourceDocumentId());
        }
        throw new BackupIntegrityException(AccountingBackupKeys.MODULE_LEDGER,
                "el asiento " + r.code() + " tiene un tipo de documento origen desconocido: " + type);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    /* ------------------------------------------------------------------ */
    /* D5: saldos del PUC                                                 */
    /* ------------------------------------------------------------------ */

    private record Acc(Long id, Long parentId, boolean credit, String code) {
    }

    /**
     * Saldo esperado por cuenta de la empresa: suma, para cada línea de asiento ACTIVO y cada factura de
     * venta, el efecto sobre la cuenta y todos sus ancestros según la naturaleza de CADA cuenta.
     */
    private Map<Long, BigDecimal> expectedBalances(String ent) {
        Map<Long, Acc> accounts = new HashMap<>();
        Map<String, Long> idByCode = new HashMap<>();
        List<Object[]> accRows = entityManager.createQuery(
                "SELECT a.id, a.parent.id, a.nature, a.code FROM " + ACCOUNT + " a WHERE a.idEnterprise = :e",
                Object[].class).setParameter("e", ent).getResultList();
        for (Object[] a : accRows) {
            Long id = (Long) a[0];
            boolean credit = a[2] instanceof Enum<?> en && "CREDIT".equals(en.name());
            accounts.put(id, new Acc(id, (Long) a[1], credit, (String) a[3]));
            if (a[3] != null) {
                idByCode.put((String) a[3], id);
            }
        }

        // [débito, crédito] acumulado por cuenta que recibe el movimiento directo
        Map<Long, BigDecimal[]> direct = new HashMap<>();
        List<Object[]> lines = entityManager.createQuery(
                "SELECT m.account, SUM(m.debit), SUM(m.credit) FROM " + MOVEMENT + " m "
                        + "WHERE m.accountingEntry.idEnterprise = :e AND m.accountingEntry.status = :st GROUP BY m.account",
                Object[].class).setParameter("e", ent).setParameter("st", AccountingEntryStatus.ACTIVE)
                .getResultList();
        for (Object[] l : lines) {
            addDirect(direct, (Long) l[0], (BigDecimal) l[1], (BigDecimal) l[2]);
        }
        List<Object[]> invoices = entityManager.createQuery(
                "SELECT i.accountingAccount, SUM(i.totalValue) FROM " + INVOICE + " i WHERE i.entId = :e GROUP BY i.accountingAccount",
                Object[].class).setParameter("e", ent).getResultList();
        for (Object[] i : invoices) {
            Long accountId = idByCode.get(String.valueOf(i[0]));
            if (accountId != null) {
                addDirect(direct, accountId, (BigDecimal) i[1], BigDecimal.ZERO);
            }
        }

        Map<Long, BigDecimal> balance = new HashMap<>();
        for (Map.Entry<Long, BigDecimal[]> d : direct.entrySet()) {
            BigDecimal debit = d.getValue()[0];
            BigDecimal credit = d.getValue()[1];
            Acc current = accounts.get(d.getKey());
            int guard = 0;
            while (current != null && guard++ < 100) {
                BigDecimal effect = current.credit() ? credit.subtract(debit) : debit.subtract(credit);
                balance.merge(current.id(), effect, BigDecimal::add);
                current = current.parentId() == null ? null : accounts.get(current.parentId());
            }
        }
        for (Long id : accounts.keySet()) {
            balance.putIfAbsent(id, BigDecimal.ZERO);
        }
        return balance;
    }

    private static void addDirect(Map<Long, BigDecimal[]> direct, Long accountId, BigDecimal debit, BigDecimal credit) {
        BigDecimal[] acc = direct.computeIfAbsent(accountId, k -> new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO });
        acc[0] = acc[0].add(debit == null ? BigDecimal.ZERO : debit);
        acc[1] = acc[1].add(credit == null ? BigDecimal.ZERO : credit);
    }

    private void recalculateBalances(String ent) {
        entityManager.flush();
        Map<Long, BigDecimal> expected = expectedBalances(ent);
        entityManager.createQuery("UPDATE " + ACCOUNT + " a SET a.amount = :z WHERE a.idEnterprise = :e AND a.amount <> :z")
                .setParameter("z", BigDecimal.ZERO).setParameter("e", ent).executeUpdate();
        for (Map.Entry<Long, BigDecimal> b : expected.entrySet()) {
            if (b.getValue().signum() != 0) {
                entityManager.createQuery("UPDATE " + ACCOUNT + " a SET a.amount = :v WHERE a.id = :id AND a.idEnterprise = :e")
                        .setParameter("v", b.getValue()).setParameter("id", b.getKey()).setParameter("e", ent)
                        .executeUpdate();
            }
        }
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
                ENTRY_ENTRIES, count("SELECT COUNT(x) FROM " + ENTRY + " x WHERE x.idEnterprise = :e", ent));
    }

    @Override
    public List<String> verifyIntegrity(UUID enterpriseId) {
        String ent = enterpriseId.toString();
        List<String> problems = new ArrayList<>();

        // asientos cuadrados: débitos == créditos
        List<String> unbalanced = entityManager.createQuery(
                "SELECT x.code FROM " + ENTRY + " x WHERE x.idEnterprise = :e AND "
                        + "COALESCE((SELECT SUM(m.debit) FROM " + MOVEMENT + " m WHERE m.accountingEntry = x), 0) <> "
                        + "COALESCE((SELECT SUM(m.credit) FROM " + MOVEMENT + " m WHERE m.accountingEntry = x), 0)",
                String.class).setParameter("e", ent).setMaxResults(10).getResultList();
        if (!unbalanced.isEmpty()) {
            problems.add("asientos descuadrados (débitos <> créditos): " + String.join(", ", unbalanced));
        }
        long noLines = count("SELECT COUNT(x) FROM " + ENTRY + " x WHERE x.idEnterprise = :e AND NOT EXISTS ("
                + "SELECT m.id FROM " + MOVEMENT + " m WHERE m.accountingEntry = x)", ent);
        add(problems, noLines, "asientos sin líneas de movimiento");

        String lines = "accounting_movements x JOIN accounting_entries h ON h.id = x.accounting_entry_id";
        String linesW = "h.id_enterprise = :e AND h.tenant_id = :t";
        add(problems, checks.orphanAccountIds(lines, linesW, "x.account_id", ent), "líneas de asiento referencian una cuenta que no es de la empresa destino");
        add(problems, checks.orphanThirds(lines, linesW, "x.third_party_id", ent), "líneas de asiento referencian un tercero que no es de la empresa destino");
        add(problems, checks.orphanCostCenters("accounting_entries x", "x.id_enterprise = :e AND x.tenant_id = :t", "x.center_cost_id", ent), "asientos referencian un centro de costo que no es de la empresa destino");
        add(problems, nativeCount("SELECT COUNT(*) FROM accounting_entries x WHERE x.id_enterprise = :e AND x.tenant_id = :t AND ("
                + "(x.type = 'RECEIPT' AND NOT EXISTS (SELECT 1 FROM accounting_receipts r WHERE r.id = x.source_document_id AND r.enterprise_id = :e AND r.tenant_id = :t)) OR "
                + "(x.type = 'PORTFOLIO_WRITEOFF' AND NOT EXISTS (SELECT 1 FROM portfolio_write_offs w WHERE w.id = x.source_document_id AND w.enterprise_id = :e AND w.tenant_id = :t)))", ent),
                "asientos referencian un documento origen (recibo / castigo) que no es de la empresa destino");

        String rec = "accounting_receipts x";
        String recW = "x.enterprise_id = :e AND x.tenant_id = :t";
        add(problems, checks.orphans(rec, recW, "x.original_receipt_id", "receipts", "id", "enterprise_id", ent), "recibos contables referencian un recibo de cartera que no es de la empresa destino");
        add(problems, checks.orphanThirds(rec, recW, "x.third_party_id", ent), "recibos contables referencian un tercero que no es de la empresa destino");
        add(problems, checks.orphanCostCenters(rec, recW, "x.center_cost_id", ent), "recibos contables referencian un centro de costo que no es de la empresa destino");
        add(problems, checks.orphans(rec, recW, "x.payment_method_id", "payment_methods", "id", "id_enterprise", ent), "recibos contables referencian un medio de pago que no es de la empresa destino");
        add(problems, checks.orphanAccountCodes(rec, recW, "x.payment_method_account", ent), "recibos contables referencian un código de cuenta (medio de pago) inexistente en el PUC destino");
        add(problems, checks.orphanAccountCodes(rec, recW, "x.ledger_account_id", ent), "recibos contables referencian un código de cuenta (ingreso directo) inexistente en el PUC destino");
        add(problems, checks.orphans("accounting_receipt_details x JOIN accounting_receipts h ON h.id = x.receipt_id",
                "h.enterprise_id = :e AND h.tenant_id = :t", "x.original_invoice_id", "accounting_invoice_replica", "id", "ent_id", ent),
                "detalles de recibo contable referencian una factura que no es de la empresa destino");
        add(problems, checks.orphanAccountCodes("accounting_receipt_details x JOIN accounting_receipts h ON h.id = x.receipt_id",
                "h.enterprise_id = :e AND h.tenant_id = :t", "x.accounting_account", ent),
                "detalles de recibo contable referencian un código de cuenta inexistente en el PUC destino");

        String inv = "accounting_invoice_replica x";
        String invW = "x.ent_id = :e AND x.tenant_id = :t";
        add(problems, checks.orphanThirds(inv, invW, "x.third_id", ent), "réplicas contables de factura referencian un tercero que no es de la empresa destino");
        add(problems, checks.orphanAccountCodes(inv, invW, "x.accounting_account", ent), "réplicas contables de factura referencian un código de cuenta inexistente en el PUC destino");

        // D5: saldo del PUC == suma de asientos activos + facturas
        Map<Long, BigDecimal> expected = expectedBalances(ent);
        List<Object[]> actual = entityManager.createQuery(
                "SELECT a.id, a.code, a.amount FROM " + ACCOUNT + " a WHERE a.idEnterprise = :e", Object[].class)
                .setParameter("e", ent).getResultList();
        List<String> drift = new ArrayList<>();
        for (Object[] a : actual) {
            BigDecimal want = expected.getOrDefault((Long) a[0], BigDecimal.ZERO);
            BigDecimal have = a[2] == null ? BigDecimal.ZERO : (BigDecimal) a[2];
            if (want.compareTo(have) != 0 && drift.size() < 10) {
                drift.add(a[1] + " (esperado " + want.toPlainString() + ", actual " + have.toPlainString() + ")");
            }
        }
        if (!drift.isEmpty()) {
            problems.add("saldos del PUC incoherentes con los asientos restaurados: " + String.join("; ", drift));
        }
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

    private long nativeCount(String sql, String ent) {
        TxSupport.flushIfActive(entityManager);
        Number n = (Number) entityManager.createNativeQuery(sql).setParameter("e", ent)
                .setParameter("t", TenantContext.getTenantId()).getSingleResult();
        return n.longValue();
    }
}
