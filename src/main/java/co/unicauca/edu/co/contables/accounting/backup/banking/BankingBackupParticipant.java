package co.unicauca.edu.co.contables.accounting.backup.banking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.backup.support.AccountingBackupKeys;
import co.unicauca.edu.co.contables.accounting.backup.support.KeysetPager;
import co.unicauca.edu.co.contables.accounting.backup.support.TxSupport;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.dataAccess.entity.BankAccountEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.dataAccess.repository.BankAccountRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.enums.AccountType;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.dataAccess.entity.BankEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.dataAccess.repository.BankRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.enums.Currency;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.dataAccess.entity.PaymentMethodEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.dataAccess.repository.PaymentMethodRepository;
import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * @brief Participante {@code accounting.banking} (order 35): bancos, cuentas bancarias y medios de pago.
 *
 * Entradas: {@code accounting/banks.ndjson}, {@code accounting/bank_accounts.ndjson},
 * {@code accounting/payment_methods.ndjson} (en ese orden de restore). Namespaces registrados:
 * {@code BANK}, {@code BANK_ACCOUNT}, {@code PAYMENT_METHOD}. Consume {@code ACCOUNT} del PUC.
 *
 * Los bancos llevan sus monedas ({@code bank_currencies}) dentro de la fila. {@code usageCount} se
 * COPIA tal cual (no se resetea): lo incrementan los recibos y comprobantes, que ahora viajan en el mismo
 * ZIP, así que el contador sigue siendo coherente con los documentos restaurados.
 *
 * Escribe directo por repositorio (sin servicios con auditoría ni listeners de uso).
 */
@Component
@RequiredArgsConstructor
public class BankingBackupParticipant implements BackupParticipant {

    public static final int SCHEMA_VERSION = 1;
    public static final String ENTRY_BANKS = "accounting/banks.ndjson";
    public static final String ENTRY_BANK_ACCOUNTS = "accounting/bank_accounts.ndjson";
    public static final String ENTRY_PAYMENT_METHODS = "accounting/payment_methods.ndjson";

    private static final int BATCH = 500;

    private static final String BANK = BankEntity.class.getName();
    private static final String BANK_ACCOUNT = BankAccountEntity.class.getName();
    private static final String PAYMENT_METHOD = PaymentMethodEntity.class.getName();
    private static final String ACCOUNT = AccountCatalogueEntity.class.getName();

    private final BankRepository bankRepository;
    private final BankAccountRepository bankAccountRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final EntityManager entityManager;

    public record BankRow(Long id, String code, String name, List<Currency> currencies, Boolean status) {
    }

    /** {@code bankId} y {@code accountingAccountId} son ids VIEJOS. */
    public record BankAccountRow(Long id, Long accountNumber, Long bankId, AccountType accountType,
            Long accountingAccountId, Boolean status, Integer usageCount) {
    }

    public record PaymentMethodRow(Long id, String name, Long accountingAccountId, Boolean status,
            Integer usageCount) {
    }

    @Override
    public String moduleKey() {
        return AccountingBackupKeys.MODULE_BANKING;
    }

    @Override
    public int order() {
        return AccountingBackupKeys.ORDER_BANKING;
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public Set<String> requires() {
        return Set.of(BackupKeys.MODULE_ENTERPRISE, BackupKeys.MODULE_PUC);
    }

    /* ------------------------------------------------------------------ */
    /* Export                                                             */
    /* ------------------------------------------------------------------ */

    @Override
    public void export(UUID enterpriseId, BackupWriter writer) {
        String ent = enterpriseId.toString();
        writer.writeNdjson(ENTRY_BANKS, banks(ent));
        writer.writeNdjson(ENTRY_BANK_ACCOUNTS, bankAccounts(ent));
        writer.writeNdjson(ENTRY_PAYMENT_METHODS, paymentMethods(ent));
    }

    private Iterator<BankRow> banks(String ent) {
        return KeysetPager.<BankEntity, BankRow>of(BATCH,
                (after, size) -> entityManager.createQuery(
                        "SELECT b FROM " + BANK + " b WHERE b.idEnterprise = :e AND b.id > :a ORDER BY b.id",
                        BankEntity.class).setParameter("e", ent).setParameter("a", after).setMaxResults(size)
                        .getResultList(),
                BankEntity::getId,
                page -> {
                    List<BankRow> rows = new ArrayList<>(page.size());
                    for (BankEntity b : page) {
                        List<Currency> currencies = b.getCurrencies() == null ? List.of()
                                : b.getCurrencies().stream().sorted(Comparator.comparing(Enum::name)).toList();
                        rows.add(new BankRow(b.getId(), b.getCode(), b.getName(), currencies, b.getStatus()));
                    }
                    entityManager.clear();
                    return rows;
                });
    }

    private Iterator<BankAccountRow> bankAccounts(String ent) {
        return KeysetPager.<BankAccountEntity, BankAccountRow>of(BATCH,
                (after, size) -> entityManager.createQuery(
                        "SELECT b FROM " + BANK_ACCOUNT + " b WHERE b.idEnterprise = :e AND b.id > :a ORDER BY b.id",
                        BankAccountEntity.class).setParameter("e", ent).setParameter("a", after)
                        .setMaxResults(size).getResultList(),
                BankAccountEntity::getId,
                page -> {
                    List<BankAccountRow> rows = new ArrayList<>(page.size());
                    for (BankAccountEntity b : page) {
                        rows.add(new BankAccountRow(b.getId(), b.getAccountNumber(), b.getBank().getId(),
                                b.getAccountType(), b.getAccountingAccount().getId(), b.getStatus(),
                                b.getUsageCount()));
                    }
                    entityManager.clear();
                    return rows;
                });
    }

    private Iterator<PaymentMethodRow> paymentMethods(String ent) {
        return KeysetPager.<PaymentMethodEntity, PaymentMethodRow>of(BATCH,
                (after, size) -> entityManager.createQuery(
                        "SELECT p FROM " + PAYMENT_METHOD + " p WHERE p.idEnterprise = :e AND p.id > :a ORDER BY p.id",
                        PaymentMethodEntity.class).setParameter("e", ent).setParameter("a", after)
                        .setMaxResults(size).getResultList(),
                PaymentMethodEntity::getId,
                page -> {
                    List<PaymentMethodRow> rows = new ArrayList<>(page.size());
                    for (PaymentMethodEntity p : page) {
                        rows.add(new PaymentMethodRow(p.getId(), p.getName(),
                                p.getAccountingAccount() == null ? null : p.getAccountingAccount().getId(),
                                p.getStatus(), p.getUsageCount()));
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

        if (reader.has(ENTRY_BANKS)) {
            reader.forEachBatch(ENTRY_BANKS, BankRow.class, BATCH, rows -> {
                List<BankEntity> entities = new ArrayList<>(rows.size());
                for (BankRow r : rows) {
                    entities.add(BankEntity.builder()
                            .code(r.code())
                            .name(r.name())
                            .currencies(r.currencies() == null ? new HashSet<>() : new HashSet<>(r.currencies()))
                            .status(r.status() == null ? Boolean.TRUE : r.status())
                            .idEnterprise(ent)
                            .build());
                }
                bankRepository.saveAll(entities);
                for (int i = 0; i < rows.size(); i++) {
                    idMap.put(AccountingBackupKeys.BANK, rows.get(i).id(), entities.get(i).getId());
                }
                flushAndClear();
            });
        }

        if (reader.has(ENTRY_BANK_ACCOUNTS)) {
            reader.forEachBatch(ENTRY_BANK_ACCOUNTS, BankAccountRow.class, BATCH, rows -> {
                List<BankAccountEntity> entities = new ArrayList<>(rows.size());
                for (BankAccountRow r : rows) {
                    entities.add(BankAccountEntity.builder()
                            .accountNumber(r.accountNumber())
                            .bank(entityManager.getReference(BankEntity.class,
                                    idMap.requireLong(AccountingBackupKeys.BANK, r.bankId())))
                            .accountType(r.accountType())
                            .accountingAccount(entityManager.getReference(AccountCatalogueEntity.class,
                                    idMap.requireLong(BackupKeys.ACCOUNT, r.accountingAccountId())))
                            .status(r.status() == null ? Boolean.TRUE : r.status())
                            .idEnterprise(ent)
                            .usageCount(r.usageCount() == null ? 0 : r.usageCount())
                            .build());
                }
                bankAccountRepository.saveAll(entities);
                for (int i = 0; i < rows.size(); i++) {
                    idMap.put(AccountingBackupKeys.BANK_ACCOUNT, rows.get(i).id(), entities.get(i).getId());
                }
                flushAndClear();
            });
        }

        if (reader.has(ENTRY_PAYMENT_METHODS)) {
            reader.forEachBatch(ENTRY_PAYMENT_METHODS, PaymentMethodRow.class, BATCH, rows -> {
                List<PaymentMethodEntity> entities = new ArrayList<>(rows.size());
                for (PaymentMethodRow r : rows) {
                    Long accountId = idMap.requireNullable(BackupKeys.ACCOUNT, r.accountingAccountId());
                    entities.add(PaymentMethodEntity.builder()
                            .name(r.name())
                            .accountingAccount(accountId == null ? null
                                    : entityManager.getReference(AccountCatalogueEntity.class, accountId))
                            .status(r.status() == null ? Boolean.TRUE : r.status())
                            .idEnterprise(ent)
                            .usageCount(r.usageCount() == null ? 0 : r.usageCount())
                            .build());
                }
                paymentMethodRepository.saveAll(entities);
                for (int i = 0; i < rows.size(); i++) {
                    idMap.put(AccountingBackupKeys.PAYMENT_METHOD, rows.get(i).id(), entities.get(i).getId());
                }
                flushAndClear();
            });
        }
    }

    /**
     * Restore en el lugar: borra medios de pago, cuentas bancarias, monedas de banco y bancos de la empresa
     * (hijos antes que padres). SQL nativo directo, sin servicios ni auditoría.
     */
    @Override
    public void purge(UUID enterpriseId) {
        String ent = enterpriseId.toString();
        entityManager.flush();
        TxSupport.purge(entityManager, "DELETE FROM payment_methods WHERE id_enterprise = :e", ent);
        TxSupport.purge(entityManager, "DELETE FROM bank_accounts WHERE id_enterprise = :e", ent);
        TxSupport.purge(entityManager,
                "DELETE FROM bank_currencies WHERE bank_id IN (SELECT id FROM banks WHERE id_enterprise = :e)", ent);
        TxSupport.purge(entityManager, "DELETE FROM banks WHERE id_enterprise = :e", ent);
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
                ENTRY_BANKS, count("SELECT COUNT(b) FROM " + BANK + " b WHERE b.idEnterprise = :e", ent),
                ENTRY_BANK_ACCOUNTS, count("SELECT COUNT(b) FROM " + BANK_ACCOUNT + " b WHERE b.idEnterprise = :e", ent),
                ENTRY_PAYMENT_METHODS, count("SELECT COUNT(p) FROM " + PAYMENT_METHOD + " p WHERE p.idEnterprise = :e", ent));
    }

    @Override
    public List<String> verifyIntegrity(UUID enterpriseId) {
        String ent = enterpriseId.toString();
        List<String> problems = new ArrayList<>();
        long a = count("SELECT COUNT(ba) FROM " + BANK_ACCOUNT + " ba WHERE ba.idEnterprise = :e AND NOT EXISTS ("
                + "SELECT b.id FROM " + BANK + " b WHERE b.id = ba.bank.id AND b.idEnterprise = :e)", ent);
        if (a > 0) {
            problems.add(a + " cuentas bancarias referencian un banco que no es de la empresa destino");
        }
        long b = count("SELECT COUNT(ba) FROM " + BANK_ACCOUNT + " ba WHERE ba.idEnterprise = :e AND NOT EXISTS ("
                + "SELECT c.id FROM " + ACCOUNT + " c WHERE c.id = ba.accountingAccount.id AND c.idEnterprise = :e)", ent);
        if (b > 0) {
            problems.add(b + " cuentas bancarias referencian una cuenta del PUC que no es de la empresa destino");
        }
        long c = count("SELECT COUNT(p) FROM " + PAYMENT_METHOD + " p WHERE p.idEnterprise = :e AND p.accountingAccount IS NOT NULL AND NOT EXISTS ("
                + "SELECT c.id FROM " + ACCOUNT + " c WHERE c.id = p.accountingAccount.id AND c.idEnterprise = :e)", ent);
        if (c > 0) {
            problems.add(c + " medios de pago referencian una cuenta del PUC que no es de la empresa destino");
        }
        return problems;
    }

    private long count(String jpql, String ent) {
        return entityManager.createQuery(jpql, Long.class).setParameter("e", ent).getSingleResult();
    }
}
