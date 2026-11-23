package co.unicauca.edu.co.contables.configuration.enterprise;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionTemplate;

import co.unicauca.edu.co.contables.accounting.backup.banking.BankingBackupParticipant;
import co.unicauca.edu.co.contables.accounting.backup.debtpayments.DebtPaymentsBackupParticipant;
import co.unicauca.edu.co.contables.accounting.backup.facture.FactureBackupParticipant;
import co.unicauca.edu.co.contables.accounting.backup.ledger.LedgerBackupParticipant;
import co.unicauca.edu.co.contables.accounting.backup.support.AccountingBackupKeys;
import co.unicauca.edu.co.contables.accounting.backup.treasury.TreasuryBackupParticipant;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.AccountingEntryStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.ProcessingStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.AccountingEntryEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.AccountingMovementEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.repository.IAccountingEntryRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.dataAccess.entity.BankAccountEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.dataAccess.repository.BankAccountRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.enums.AccountType;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.dataAccess.entity.BankEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.dataAccess.repository.BankRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.enums.Currency;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.backup.PucBackupParticipant;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.repository.IAccountCatalogueRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.dataAccess.entity.PaymentMethodEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.dataAccess.repository.PaymentMethodRepository;
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
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentScheduleStatus;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentVoucherStatus;
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
import co.unicauca.edu.co.contables.commons.backup.BackupIntegrityException;
import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.InMemoryIdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import co.unicauca.edu.co.contables.commons.backup.RestoreMode;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupFormat;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupReader;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupWriter;
import co.unicauca.edu.co.contables.commons.backup.zip.ManifestV2;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;

/**
 * Ida y vuelta de los participantes de contabilidad (banking, facture, debtPayments, ledger, treasury) junto
 * con el PUC: asientos con líneas en el tenant A, export, restore como tenant B y comprobaciones de
 * conteos, asientos cuadrados, FKs nuevas, numeración y saldos del PUC recalculados (D5).
 */
@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
@Import(TestMultiTenancyConfig.class)
@Sql(scripts = { "/enterprise-test-schema.sql", "/thirds-test-schema.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AccountingBackupRoundTripTest {

    private static final String TENANT_A = "tenant-A";
    private static final String TENANT_B = "tenant-B";
    private static final long OLD_THIRD_1 = 111L;
    private static final long OLD_THIRD_2 = 222L;
    private static final long OLD_COST_CENTER = 333L;

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @Autowired private PucBackupParticipant puc;
    @Autowired private BankingBackupParticipant banking;
    @Autowired private FactureBackupParticipant facture;
    @Autowired private DebtPaymentsBackupParticipant debtPayments;
    @Autowired private LedgerBackupParticipant ledger;
    @Autowired private TreasuryBackupParticipant treasury;

    @Autowired private IAccountCatalogueRepository accounts;
    @Autowired private BankRepository banks;
    @Autowired private BankAccountRepository bankAccounts;
    @Autowired private PaymentMethodRepository paymentMethods;
    @Autowired private IInvoiceRepository dpInvoices;
    @Autowired private IReceiptRepository dpReceipts;
    @Autowired private IPortfolioWriteOffRepository dpWriteOffs;
    @Autowired private co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.repository.IInvoiceRepository accInvoices;
    @Autowired private co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.repository.IReceiptRepository accReceipts;
    @Autowired private IAccountingEntryRepository entries;
    @Autowired private ISupplierInvoiceRepository supplierInvoices;
    @Autowired private IPaymentVoucherRepository vouchers;
    @Autowired private IPaymentScheduleRepository schedules;
    @Autowired private IPayableWriteOffRepository payableWriteOffs;
    @Autowired private IDueDateHistoryRepository history;
    @Autowired private FactureRepository legacyFactures;
    @Autowired private SkeletonFactureRepository skeletons;
    @Autowired private SkeletonReturnRepository returns;

    @Autowired private ZipBackupFormat format;
    @Autowired private TransactionTemplate tx;
    @Autowired private JdbcTemplate jdbc;

    @TempDir
    Path tmp;

    private record Seed(UUID source, long oldProduct, long factCode, String entryCode1, String receiptCode,
            Set<Long> accountIds, Set<Long> entryIds) {
    }

    private AccountCatalogueEntity account(String ent, String code, NatureEnum nature, AccountCatalogueEntity parent) {
        return accounts.save(AccountCatalogueEntity.builder()
                .code(code).description("Cuenta " + code).nature(nature)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .parent(parent).crossing(false).costCenter(true).status(true)
                .idEnterprise(ent).amount(new BigDecimal("999.0000")).usageCount(2).build());
    }

    private AccountingMovementEntity line(AccountingEntryEntity entry, Long account, long third, String debit, String credit) {
        AccountingMovementEntity m = new AccountingMovementEntity();
        m.setAccount(account);
        m.setThirdPartyId(third);
        m.setDescription("linea");
        m.setDebit(new BigDecimal(debit));
        m.setCredit(new BigDecimal(credit));
        m.setAccountingEntry(entry);
        return m;
    }

    private AccountingEntryEntity entry(String ent, String code, AccountingEntryStatus status, Long source, String type) {
        AccountingEntryEntity e = new AccountingEntryEntity();
        e.setIdEnterprise(ent);
        e.setCode(code);
        e.setDate(LocalDate.of(2026, 3, 1));
        e.setDescription("Asiento " + code);
        e.setStatus(status);
        e.setSourceDocumentId(source);
        e.setType(type);
        e.setCenterCostId(OLD_COST_CENTER);
        e.setMovements(new ArrayList<>());
        return e;
    }

    /**
     * Tenant A. PUC: 1105 > 110505 (caja, DEBIT), 1305 (cartera, DEBIT), 4135 (ingresos, CREDIT), 2205 (proveedores, CREDIT).
     * Saldo esperado tras recalcular: factura 1000 + recibo(-600) + castigo(-100) = 300 en 1305; 110505 y 1105 = 600; 4135 = -100.
     */
    private Seed seedTenantA() {
        UUID source = UUID.randomUUID();
        String ent = source.toString();
        long factCode = 880000000L + Math.floorMod(System.nanoTime(), 100000L);
        long oldProduct = 7000000L + Math.floorMod(System.nanoTime(), 1000000L);
        String suffix = Long.toString(Math.floorMod(System.nanoTime(), 100000000L) + 10000000L);
        return tx.execute(s -> {
            AccountCatalogueEntity p1105 = account(ent, "1105", NatureEnum.DEBIT, null);
            AccountCatalogueEntity caja = account(ent, "110505", NatureEnum.DEBIT, p1105);
            AccountCatalogueEntity cartera = account(ent, "1305", NatureEnum.DEBIT, null);
            AccountCatalogueEntity ingresos = account(ent, "4135", NatureEnum.CREDIT, null);
            AccountCatalogueEntity proveedores = account(ent, "2205", NatureEnum.CREDIT, null);

            // banking
            BankEntity bank = banks.save(BankEntity.builder().code("07").name("Banco A")
                    .currencies(Set.of(Currency.COP, Currency.USD)).status(true).idEnterprise(ent).build());
            BankAccountEntity bankAccount = bankAccounts.save(BankAccountEntity.builder().accountNumber(123456L)
                    .bank(bank).accountType(AccountType.AHORROS).accountingAccount(caja).status(true)
                    .idEnterprise(ent).usageCount(4).build());
            PaymentMethodEntity method = paymentMethods.save(PaymentMethodEntity.builder().name("Efectivo")
                    .accountingAccount(caja).status(true).idEnterprise(ent).usageCount(3).build());

            // cartera (debt_payments)
            InvoiceReplicaEntity dpInvoice = new InvoiceReplicaEntity();
            dpInvoice.setFactCode(factCode);
            dpInvoice.setEntId(ent);
            dpInvoice.setThirdId(OLD_THIRD_1);
            dpInvoice.setTotalValue(1000L);
            dpInvoice.setTotalPay(700L);
            dpInvoice.setPendingValue(300L);
            dpInvoice.setCreationDate(LocalDate.of(2026, 2, 1));
            dpInvoice.setExpirationDate(LocalDate.of(2026, 4, 1));
            dpInvoice.setActive(true);
            dpInvoice.setAccountingAccount(1305L);
            dpInvoice.setStatus(InvoiceStatus.PENDING);
            dpInvoice = dpInvoices.save(dpInvoice);

            String receiptCode = "RC-" + suffix;
            ReceiptEntity dpReceipt = new ReceiptEntity();
            dpReceipt.setReceiptCode(receiptCode);
            dpReceipt.setThirdPartyId(OLD_THIRD_1);
            dpReceipt.setEnterpriseId(ent);
            dpReceipt.setPaymentMethodId(method.getId());
            dpReceipt.setPaymentMethodAccount(110505L);
            dpReceipt.setReceiptType(ReceiptType.INVOICE_PAYMENT);
            dpReceipt.setStatus(ReceiptStatus.FINALIZED);
            dpReceipt.setIssueDate(LocalDate.of(2026, 3, 1));
            dpReceipt.setTotalAmount(new BigDecimal("600"));
            dpReceipt.setCenterCostId(OLD_COST_CENTER);
            ReceiptDetailEntity dpDetail = new ReceiptDetailEntity();
            dpDetail.setInvoiceId(dpInvoice.getId());
            dpDetail.setInvoiceCode("F-1");
            dpDetail.setAccountingAccount(1305L);
            dpDetail.setAmountPaid(600L);
            dpDetail.setReceipt(dpReceipt);
            dpReceipt.getDetails().add(dpDetail);
            dpReceipt = dpReceipts.save(dpReceipt);

            PortfolioWriteOffEntity dpWriteOff = new PortfolioWriteOffEntity();
            dpWriteOff.setJustification("incobrable");
            dpWriteOff.setCode("WO-1");
            dpWriteOff.setTotalAmount(100L);
            dpWriteOff.setWriteOffDate(LocalDate.of(2026, 3, 5));
            dpWriteOff.setDebitAuxiliaryAccount(4135L);
            dpWriteOff.setDebitAuxiliaryAccountId(ingresos.getId());
            dpWriteOff.setCostCenterId(OLD_COST_CENTER);
            dpWriteOff.setThirdId(OLD_THIRD_1);
            dpWriteOff.setStatus(WriteOffStatus.CONFIRMED);
            dpWriteOff.setEnterpriseId(ent);
            WriteOffDetailEntity dpWoDetail = new WriteOffDetailEntity();
            dpWoDetail.setInvoiceId(dpInvoice.getId());
            dpWoDetail.setAmountWrittenOff(100L);
            dpWoDetail.setAccountingAccount(1305L);
            dpWoDetail.setPortfolioWriteOff(dpWriteOff);
            dpWriteOff.setDetails(new ArrayList<>(List.of(dpWoDetail)));
            dpWriteOff = dpWriteOffs.save(dpWriteOff);

            // libro contable (ledger)
            var accInvoice = new co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.InvoiceReplicaEntity();
            accInvoice.setFactCode(factCode);
            accInvoice.setEntId(ent);
            accInvoice.setThirdId(OLD_THIRD_1);
            accInvoice.setTotalValue(new BigDecimal("1000"));
            accInvoice.setTotalPay(new BigDecimal("700"));
            accInvoice.setPendingValue(new BigDecimal("300"));
            accInvoice.setCreationDate(LocalDate.of(2026, 2, 1));
            accInvoice.setExpirationDate(LocalDate.of(2026, 4, 1));
            accInvoice.setActive(true);
            accInvoice.setAccountingAccount(1305L);
            accInvoice.setStatus(co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.InvoiceStatus.PENDING);
            accInvoice = accInvoices.save(accInvoice);

            var accReceipt = new co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.ReceiptEntity();
            accReceipt.setOriginalReceiptId(dpReceipt.getId());
            accReceipt.setReceiptCode(receiptCode);
            accReceipt.setEnterpriseId(ent);
            accReceipt.setReceiptTypeId(1L);
            accReceipt.setThirdPartyId(OLD_THIRD_1);
            accReceipt.setPaymentMethodId(method.getId());
            accReceipt.setPaymentMethodAccount(110505L);
            accReceipt.setStatus("FINALIZED");
            accReceipt.setIssueDate(LocalDate.of(2026, 3, 1));
            accReceipt.setTotalAmount(new BigDecimal("600"));
            accReceipt.setProcessingStatus(ProcessingStatus.PROCESSED);
            accReceipt.setCenterCostId(OLD_COST_CENTER);
            var accDetail = new co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.ReceiptDetailEntity();
            accDetail.setOriginalInvoiceId(accInvoice.getId());
            accDetail.setInvoiceCode("F-1");
            accDetail.setAccountingAccount(1305L);
            accDetail.setAmountPaid(new BigDecimal("600"));
            accDetail.setReceipt(accReceipt);
            accReceipt.setDetails(new ArrayList<>(List.of(accDetail)));
            accReceipt = accReceipts.save(accReceipt);

            String entryCode1 = "AE-" + suffix.substring(0, Math.min(8, suffix.length()));
            AccountingEntryEntity e1 = entry(ent, entryCode1, AccountingEntryStatus.ACTIVE, accReceipt.getId(), "RECEIPT");
            e1.getMovements().add(line(e1, caja.getId(), OLD_THIRD_1, "600", "0"));
            e1.getMovements().add(line(e1, cartera.getId(), OLD_THIRD_1, "0", "600"));
            e1 = entries.save(e1);
            AccountingEntryEntity e2 = entry(ent, "AC-" + suffix.substring(0, Math.min(8, suffix.length())), AccountingEntryStatus.ACTIVE,
                    dpWriteOff.getId(), "PORTFOLIO_WRITEOFF");
            e2.getMovements().add(line(e2, ingresos.getId(), OLD_THIRD_1, "100", "0"));
            e2.getMovements().add(line(e2, cartera.getId(), OLD_THIRD_1, "0", "100"));
            e2 = entries.save(e2);
            AccountingEntryEntity e3 = entry(ent, "AE-V" + suffix.substring(0, Math.min(7, suffix.length())), AccountingEntryStatus.VOIDED,
                    accReceipt.getId(), "RECEIPT");
            e3.getMovements().add(line(e3, caja.getId(), OLD_THIRD_1, "50", "0"));
            e3.getMovements().add(line(e3, cartera.getId(), OLD_THIRD_1, "0", "50"));
            e3 = entries.save(e3);

            // tesorería
            SupplierInvoiceReplicaEntity si = new SupplierInvoiceReplicaEntity();
            si.setSourceInvoiceId(9001L);
            si.setReference("FP-1");
            si.setEnterpriseId(ent);
            si.setSupplierId(OLD_THIRD_2);
            si.setOriginalAmount(new BigDecimal("500"));
            si.setPaidAmount(new BigDecimal("200"));
            si.setPendingAmount(new BigDecimal("300"));
            si.setIssueDate(LocalDate.of(2026, 2, 10));
            si.setOriginalDueDate(LocalDate.of(2026, 3, 10));
            si.setDueDate(LocalDate.of(2026, 3, 20));
            si.setDueDateOverridden(true);
            si.setPayableAccountId(proveedores.getId());
            si.setPayableAccountCode("2205");
            si = supplierInvoices.save(si);

            PaymentVoucherEntity v = new PaymentVoucherEntity();
            v.setVoucherNumber("CE-" + suffix);
            v.setEnterpriseId(ent);
            v.setIssueDate(LocalDate.of(2026, 3, 15));
            v.setStatus(PaymentVoucherStatus.POSTED);
            v.setPaymentMethodId(method.getId());
            v.setBankAccountId(bankAccount.getId());
            v.setTotal(new BigDecimal("200"));
            v.setIdempotencyKey("idem-" + suffix);
            v.setAccountingEntryId(e1.getId());
            PaymentVoucherDetailEntity vd = new PaymentVoucherDetailEntity();
            vd.setSupplierId(OLD_THIRD_2);
            vd.setInvoiceId(si.getId());
            vd.setInvoiceReference("FP-1");
            vd.setPayableAccountId(proveedores.getId());
            vd.setPayableAccountCode("2205");
            vd.setPreviousBalance(new BigDecimal("500"));
            vd.setAmountPaid(new BigDecimal("200"));
            vd.setRemainingBalance(new BigDecimal("300"));
            vd.setPaymentVoucher(v);
            v.getDetails().add(vd);
            v = vouchers.save(v);

            PaymentScheduleEntity sch = new PaymentScheduleEntity();
            sch.setEnterpriseId(ent);
            sch.setExecutionDate(LocalDate.of(2026, 4, 1));
            sch.setPaymentMethodId(method.getId());
            sch.setBankAccountId(bankAccount.getId());
            sch.setStatus(PaymentScheduleStatus.SCHEDULED);
            sch.setVoucherId(v.getId());
            PaymentScheduleDetailEntity sd = new PaymentScheduleDetailEntity();
            sd.setSupplierId(OLD_THIRD_2);
            sd.setInvoiceId(si.getId());
            sd.setAmount(new BigDecimal("100"));
            sd.setSchedule(sch);
            sch.getDetails().add(sd);
            schedules.save(sch);

            PayableWriteOffEntity pw = new PayableWriteOffEntity();
            pw.setEnterpriseId(ent);
            pw.setReason("condonado");
            pw.setCounterpartAccountId(ingresos.getId());
            pw.setCounterpartAccountCode("4135");
            pw.setTotal(new BigDecimal("50"));
            pw.setStatus(co.unicauca.edu.co.contables.accounting.treasury.domain.model.WriteOffStatus.POSTED);
            pw.setAccountingEntryId(e2.getId());
            PayableWriteOffDetailEntity pwd = new PayableWriteOffDetailEntity();
            pwd.setSupplierId(OLD_THIRD_2);
            pwd.setInvoiceId(si.getId());
            pwd.setPayableAccountId(proveedores.getId());
            pwd.setPayableAccountCode("2205");
            pwd.setAmount(new BigDecimal("50"));
            pwd.setWriteOff(pw);
            pw.getDetails().add(pwd);
            payableWriteOffs.save(pw);

            DueDateHistoryEntity h = new DueDateHistoryEntity();
            h.setInvoiceId(si.getId());
            h.setPreviousDate(LocalDate.of(2026, 3, 10));
            h.setNewDate(LocalDate.of(2026, 3, 20));
            h.setReason("negociado");
            h.setChangedAt(Instant.parse("2026-03-01T10:00:00Z"));
            h.setChangedBy("user-a");
            history.save(h);

            // facturas (legacy + skeleton + devolución)
            ProductEntity legacyProduct = ProductEntity.builder().productId(oldProduct).code("P1").descount(0)
                    .amount(2).description("Prod").vat(19).unitPrice(100).subtotal(200).build();
            FactureEntity lf = FactureEntity.builder().entId(ent).thId(OLD_THIRD_1).factCode(factCode)
                    .factObservations("legado").factureType(eFactureType.Venta).factSubtotals(200.0)
                    .facSalesTax(38.0).facWithholdingSource(0.0).descounts(0.0).build();
            lf.getFactProducts().add(legacyProduct);
            legacyFactures.save(lf);

            SkeletonFacture sk = SkeletonFacture.builder().factCode(factCode).entId(ent).thId(OLD_THIRD_1)
                    .totalValue("1000").totalPay("700").pendingValue("300").issueDate(LocalDate.of(2026, 2, 1))
                    .expirationDate(LocalDate.of(2026, 4, 1)).accountingAccount(1305L)
                    .factureType(SkeletonFactureType.SALE).purchaseStatus(PurchaseInvoiceStatus.ACTIVE).build();
            sk.addProduct(SkeletonProduct.builder().productId(oldProduct).amount(2).description("Prod")
                    .discount(0.0).unitPrice(500.0).subtotal(1000.0).taxPercentage(List.of(19)).build());
            skeletons.save(sk);
            returns.save(SkeletonReturn.builder().originalFactCode(factCode).productId(oldProduct)
                    .returnedQuantity(1).returnType(SkeletonReturn.ReturnType.RETURN_ON_SALE).details("devolucion").build());

            return new Seed(source, oldProduct, factCode, entryCode1, receiptCode,
                    Set.of(p1105.getId(), caja.getId(), cartera.getId(), ingresos.getId(), proveedores.getId()),
                    Set.of(e1.getId(), e2.getId(), e3.getId()));
        });
    }

    private List<BackupParticipant> participants() {
        return List.of(puc, banking, facture, debtPayments, ledger, treasury);
    }

    private Path export(UUID source) {
        Path target = tmp.resolve("accounting-" + source + ".zip");
        ZipBackupWriter.Header header = new ZipBackupWriter.Header(source, "Empresa A", "test",
                ManifestV2.CreatedBy.of(TENANT_A, "u-a"), List.of(), LocalDateTime.now());
        try (ZipBackupWriter zip = format.newWriter(target, header)) {
            for (BackupParticipant p : participants()) {
                tx.executeWithoutResult(s -> p.export(source, zip.module(p.moduleKey(), p.schemaVersion())));
            }
            zip.finish();
        }
        return target;
    }

    private RestoreContext ctxB(UUID source, UUID target) {
        return new RestoreContext(TENANT_B, "u-b", source, target, 2, LocalDateTime.now(),
                RestoreMode.NEW_ENTERPRISE, null, null, false);
    }

    /** Terceros, centro de costo y producto reales en el tenant B (los restauran otros participantes). */
    private void seedPrerequisitesB(UUID target, long oldProduct, InMemoryIdMap idMap) {
        String ent = target.toString();
        jdbc.update("INSERT INTO THIRDS (ent_id, tenant_id, th_state, th_usage_count) VALUES (?, ?, TRUE, 0)", ent, TENANT_B);
        jdbc.update("INSERT INTO THIRDS (ent_id, tenant_id, th_state, th_usage_count) VALUES (?, ?, TRUE, 0)", ent, TENANT_B);
        List<Long> thirds = jdbc.queryForList("SELECT th_id FROM THIRDS WHERE ent_id = ? ORDER BY th_id", Long.class, ent);
        idMap.put(BackupKeys.THIRD, OLD_THIRD_1, thirds.get(0));
        idMap.put(BackupKeys.THIRD, OLD_THIRD_2, thirds.get(1));
        jdbc.update("INSERT INTO cost_centers (code, name, id_enterprise, status, usage_count, tenant_id) VALUES ('CC1', 'Centro', ?, TRUE, 0, ?)", ent, TENANT_B);
        Long cc = jdbc.queryForObject("SELECT id FROM cost_centers WHERE id_enterprise = ?", Long.class, ent);
        idMap.put(BackupKeys.COST_CENTER, OLD_COST_CENTER, cc);
        jdbc.update("INSERT INTO product (code, name, enterprise_id, tenant_id, cost, state, quantity, usage_count) VALUES ('P1', 'Prod', ?, ?, 10, TRUE, 0, 0)", ent, TENANT_B);
        Long product = jdbc.queryForObject("SELECT id FROM product WHERE enterprise_id = ?", Long.class, ent);
        idMap.put(BackupKeys.PRODUCT, oldProduct, product);
    }

    private void restoreAll(Path zipPath, UUID source, UUID target, InMemoryIdMap idMap) {
        try (ZipBackupReader reader = format.open(zipPath)) {
            reader.verifyAll();
            for (BackupParticipant p : participants()) {
                tx.executeWithoutResult(s -> p.restore(reader.module(p.moduleKey()), idMap, ctxB(source, target)));
            }
        }
    }

    private BigDecimal amount(String ent, String code) {
        return jdbc.queryForObject("SELECT amount FROM account WHERE tenant_id = ? AND id_enterprise = ? AND code = ?",
                BigDecimal.class, TENANT_B, ent, code);
    }

    @Test
    void contabilidadViajaConAsientosCuadradosFKsNuevasNumeracionYSaldosRecalculados() {
        TenantContext.setTenantId(TENANT_A);
        try {
            Seed seed = seedTenantA();
            UUID source = seed.source();
            Path zipPath = export(source);

            TenantContext.setTenantId(TENANT_B);
            UUID target = UUID.randomUUID();
            String ent = target.toString();
            InMemoryIdMap idMap = new InMemoryIdMap();
            seedPrerequisitesB(target, seed.oldProduct(), idMap);
            restoreAll(zipPath, source, target, idMap);

            // conteos == origen (el manifest declara lo mismo) y sin huérfanos
            assertThat(banking.count(target)).isEqualTo(Map.of(
                    BankingBackupParticipant.ENTRY_BANKS, 1L,
                    BankingBackupParticipant.ENTRY_BANK_ACCOUNTS, 1L,
                    BankingBackupParticipant.ENTRY_PAYMENT_METHODS, 1L));
            assertThat(debtPayments.count(target)).isEqualTo(Map.of(
                    DebtPaymentsBackupParticipant.ENTRY_INVOICES, 1L,
                    DebtPaymentsBackupParticipant.ENTRY_RECEIPTS, 1L,
                    DebtPaymentsBackupParticipant.ENTRY_WRITE_OFFS, 1L));
            assertThat(ledger.count(target)).isEqualTo(Map.of(
                    LedgerBackupParticipant.ENTRY_INVOICES, 1L,
                    LedgerBackupParticipant.ENTRY_RECEIPTS, 1L,
                    LedgerBackupParticipant.ENTRY_ENTRIES, 3L));
            assertThat(treasury.count(target)).isEqualTo(Map.of(
                    TreasuryBackupParticipant.ENTRY_SUPPLIER_INVOICES, 1L,
                    TreasuryBackupParticipant.ENTRY_VOUCHERS, 1L,
                    TreasuryBackupParticipant.ENTRY_SCHEDULES, 1L,
                    TreasuryBackupParticipant.ENTRY_WRITE_OFFS, 1L,
                    TreasuryBackupParticipant.ENTRY_DUE_DATE_HISTORY, 1L));
            assertThat(facture.count(target)).isEqualTo(Map.of(
                    FactureBackupParticipant.ENTRY_LEGACY_PRODUCTS, 1L,
                    FactureBackupParticipant.ENTRY_LEGACY_FACTURES, 1L,
                    FactureBackupParticipant.ENTRY_SKELETON_FACTURES, 1L,
                    FactureBackupParticipant.ENTRY_SKELETON_RETURNS, 1L));
            for (BackupParticipant p : participants()) {
                List<String> found = tx.execute(s -> p.verifyIntegrity(target));
                assertThat(found).as(p.moduleKey()).isEmpty();
            }

            // ids nuevos, ninguno de A
            for (Long oldId : seed.entryIds()) {
                assertThat(idMap.<Long>require(AccountingBackupKeys.ACC_ENTRY, oldId)).isNotIn(seed.entryIds());
            }

            // asientos cuadrados y con líneas; E3 anulado conserva su estado
            assertThat(jdbc.queryForObject(
                    "SELECT COUNT(*) FROM accounting_entries e WHERE e.tenant_id = ? AND e.id_enterprise = ? AND "
                            + "(SELECT COALESCE(SUM(m.debit),0) FROM accounting_movements m WHERE m.accounting_entry_id = e.id) <> "
                            + "(SELECT COALESCE(SUM(m.credit),0) FROM accounting_movements m WHERE m.accounting_entry_id = e.id)",
                    Long.class, TENANT_B, ent)).isZero();
            assertThat(jdbc.queryForObject(
                    "SELECT COUNT(*) FROM accounting_movements m JOIN accounting_entries e ON e.id = m.accounting_entry_id "
                            + "WHERE e.tenant_id = ? AND e.id_enterprise = ?", Long.class, TENANT_B, ent)).isEqualTo(6L);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM accounting_entries WHERE tenant_id = ? AND id_enterprise = ? AND status = 'VOIDED'",
                    Long.class, TENANT_B, ent)).isEqualTo(1L);

            // numeración con unicidad global: los códigos del origen siguen en A, así que en B se regeneran con el mismo prefijo
            List<String> codes = jdbc.queryForList("SELECT code FROM accounting_entries WHERE tenant_id = ? AND id_enterprise = ?", String.class, TENANT_B, ent);
            assertThat(codes).hasSize(3).doesNotContain(seed.entryCode1()).allMatch(c -> c.startsWith("AE-") || c.startsWith("AC-"));
            String newReceiptCode = jdbc.queryForObject("SELECT receipt_code FROM receipts WHERE tenant_id = ? AND enterprise_id = ?", String.class, TENANT_B, ent);
            assertThat(newReceiptCode).startsWith("RC-").isNotEqualTo(seed.receiptCode());
            assertThat(jdbc.queryForObject("SELECT receipt_code FROM accounting_receipts WHERE tenant_id = ? AND enterprise_id = ?", String.class, TENANT_B, ent))
                    .isEqualTo(newReceiptCode);
            // el mismo número de factura nuevo en TODAS las tablas que lo guardan
            Long newFact = idMap.<Long>require(AccountingBackupKeys.FACT_CODE, seed.factCode());
            assertThat(newFact).isNotEqualTo(seed.factCode());
            assertThat(jdbc.queryForObject("SELECT fact_code FROM skeleton_factures WHERE tenant_id = ? AND ent_id = ?", Long.class, TENANT_B, ent)).isEqualTo(newFact);
            assertThat(jdbc.queryForObject("SELECT fact_code FROM FACTURES WHERE ent_id = ?", Long.class, ent)).isEqualTo(newFact);
            assertThat(jdbc.queryForObject("SELECT fact_code FROM invoice_replica WHERE tenant_id = ? AND ent_id = ?", Long.class, TENANT_B, ent)).isEqualTo(newFact);
            assertThat(jdbc.queryForObject("SELECT fact_code FROM accounting_invoice_replica WHERE tenant_id = ? AND ent_id = ?", Long.class, TENANT_B, ent)).isEqualTo(newFact);
            assertThat(jdbc.queryForObject("SELECT original_fact_code FROM skeleton_returns WHERE original_fact_code = ?", Long.class, newFact)).isEqualTo(newFact);

            // FKs apuntan a filas NUEVAS de B
            Long newCaja = idMap.require(BackupKeys.ACCOUNT, jdbcOldCajaId(seed));
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM bank_accounts WHERE tenant_id = ? AND id_enterprise = ? AND accounting_account_id = ?",
                    Long.class, TENANT_B, ent, newCaja)).isEqualTo(1L);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM accounting_receipts r JOIN receipts d ON d.id = r.original_receipt_id "
                    + "WHERE r.tenant_id = ? AND r.enterprise_id = ? AND d.enterprise_id = ? AND d.tenant_id = ?", Long.class, TENANT_B, ent, ent, TENANT_B)).isEqualTo(1L);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM payment_vouchers v JOIN accounting_entries e ON e.id = v.accounting_entry_id "
                    + "WHERE v.tenant_id = ? AND v.enterprise_id = ? AND e.id_enterprise = ?", Long.class, TENANT_B, ent, ent)).isEqualTo(1L);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM payment_vouchers WHERE tenant_id = ? AND enterprise_id = ? AND idempotency_key IS NULL", Long.class, TENANT_B, ent)).isEqualTo(1L);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM accounting_entries e JOIN portfolio_write_offs w ON w.id = e.source_document_id "
                    + "WHERE e.tenant_id = ? AND e.id_enterprise = ? AND e.type = 'PORTFOLIO_WRITEOFF' AND w.enterprise_id = ?", Long.class, TENANT_B, ent, ent)).isEqualTo(1L);
            assertThat(jdbc.queryForObject("SELECT return_date FROM skeleton_returns WHERE original_fact_code = ?", java.sql.Timestamp.class, newFact)).isNotNull();

            // D5: saldos recalculados desde asientos ACTIVOS + facturas, con jerarquía por naturaleza
            assertThat(amount(ent, "1305")).isEqualByComparingTo("300");
            assertThat(amount(ent, "110505")).isEqualByComparingTo("600");
            assertThat(amount(ent, "1105")).isEqualByComparingTo("600");
            assertThat(amount(ent, "4135")).isEqualByComparingTo("-100");
            assertThat(amount(ent, "2205")).isEqualByComparingTo("0");

            // contadores de uso de bancos / medios de pago se copian (los documentos viajan)
            assertThat(jdbc.queryForObject("SELECT usage_count FROM bank_accounts WHERE tenant_id = ? AND id_enterprise = ?", Integer.class, TENANT_B, ent)).isEqualTo(4);

            // tenant A intacto: saldos sembrados y sus 3 asientos
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM account WHERE tenant_id = ? AND id_enterprise = ? AND amount = 999",
                    Long.class, TENANT_A, source.toString())).isEqualTo(5L);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM accounting_entries WHERE tenant_id = ? AND id_enterprise = ?",
                    Long.class, TENANT_A, source.toString())).isEqualTo(3L);
        } finally {
            TenantContext.clear();
        }
    }

    /** id viejo (tenant A) de la cuenta 110505. */
    private Long jdbcOldCajaId(Seed seed) {
        return jdbc.queryForObject("SELECT id FROM account WHERE tenant_id = ? AND id_enterprise = ? AND code = '110505'",
                Long.class, TENANT_A, seed.source().toString());
    }

    @Test
    void fkSinMapeoFallaPorIntegridadYHaceRollbackDelModulo() {
        TenantContext.setTenantId(TENANT_A);
        try {
            Seed seed = seedTenantA();
            Path zipPath = export(seed.source());

            TenantContext.setTenantId(TENANT_B);
            UUID target = UUID.randomUUID();
            InMemoryIdMap idMap = new InMemoryIdMap();
            try (ZipBackupReader reader = format.open(zipPath)) {
                // IdMap vacío: el PUC y la cartera no se restauraron, las FKs no se pueden remapear
                assertThatThrownBy(() -> tx.executeWithoutResult(s -> ledger.restore(
                        reader.module(AccountingBackupKeys.MODULE_LEDGER), idMap, ctxB(seed.source(), target))))
                                .isInstanceOf(BackupIntegrityException.class);
            }
            assertThat(ledger.count(target)).isEqualTo(Map.of(
                    LedgerBackupParticipant.ENTRY_INVOICES, 0L,
                    LedgerBackupParticipant.ENTRY_RECEIPTS, 0L,
                    LedgerBackupParticipant.ENTRY_ENTRIES, 0L));
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void verifyIntegrityDetectaAsientoDescuadradoYSaldoIncoherente() {
        TenantContext.setTenantId(TENANT_B);
        try {
            UUID target = UUID.randomUUID();
            String ent = target.toString();
            tx.executeWithoutResult(s -> {
                AccountCatalogueEntity a = account(ent, "9999", NatureEnum.DEBIT, null);
                AccountingEntryEntity e = entry(ent, "AE-BAD" + UUID.randomUUID().toString().substring(0, 5), AccountingEntryStatus.ACTIVE, 1L, "RECEIPT");
                e.setCenterCostId(null);
                e.getMovements().add(line(e, a.getId(), 1L, "10", "0"));
                e.getMovements().add(line(e, a.getId(), 1L, "0", "7"));
                entries.save(e);
            });
            List<String> problems = tx.execute(s -> ledger.verifyIntegrity(target));
            assertThat(problems).anyMatch(p -> p.contains("descuadrados"));
            assertThat(problems).anyMatch(p -> p.contains("saldos del PUC incoherentes"));
        } finally {
            TenantContext.clear();
        }
    }
}
