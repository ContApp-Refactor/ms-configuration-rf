package co.unicauca.edu.co.contables.configuration.enterprise;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.PlatformTransactionManager;
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
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.backup.TaxesBackupParticipant;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.entity.TaxEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.repository.ITaxRepository;
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
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookFormat;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ECriteriaType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryWay;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EFrequency;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EJobStatus;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EState;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ETypeEvent;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.in.backup.AuxiliaryBackupParticipant;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookCriteriaEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookHistoryEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookLogEntity;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.scheduledReport.ScheduledReportEntity;
import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.RestoreActor;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import co.unicauca.edu.co.contables.commons.backup.zip.ManifestV2;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupFormat;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.dataAccess.entity.AccountingCalendarEntity;
import co.unicauca.edu.co.contables.configuration.accountingCalendar.dataAccess.repository.AccountingCalendarRepository;
import co.unicauca.edu.co.contables.configuration.backup.ConfigurationBackupParticipant;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.entity.DocumentClassEntity;
import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.repository.DocumentClassRepository;
import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.entity.CostCenterEntity;
import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.repository.CostCenterRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.CityEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.CountryEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.DepartmentEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseTypeEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.LocationEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.PersonTypeEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.TaxLiabilityEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.TaxPayerTypeEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.EnterpriseRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.StateEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.EnterpriseBackupCoordinatorImpl;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.IEnterpriseBackupCoordinator;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.RestoreOptions;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.RestoreResult;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.RestoreFailedException;
import co.unicauca.edu.co.contables.configuration.noCommercialTags.dataAccess.entity.TagEntity;
import co.unicauca.edu.co.contables.configuration.noCommercialTags.dataAccess.repository.ITagRepository;
import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ePersonType;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.PersonClassification;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.backup.ThirdsBackupParticipant;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.ThirdEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.ThirdTypeEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.ThirdsAndTypesEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.TypeIdEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.ThirdRepository;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.ThirdTypeRepository;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.ThirdsAndTypesRepository;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.TypeIdRepository;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.entity.DocumentTypeEntity;
import co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.repository.DocumentTypeRepository;
import co.unicauca.edu.co.contables.financial.domain.models.enums.EFinancialStatementType;
import co.unicauca.edu.co.contables.financial.infrastructure.in.backup.FinancialBackupParticipant;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementAnnotationEntity;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementEntity;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementHistoryEntity;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementLogEntity;
import co.unicauca.edu.co.contables.financial.infrastructure.out.persistence.entity.FinancialStatementTemplateEntity;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.backup.KardexBackupFormat;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.backup.KardexMovementsBackupParticipant;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.backup.PepsBackupFormat;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.backup.PepsMovementsBackupParticipant;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.DetailOutputEntity;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.backup.ProductsBackupFormat;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.CategoryEntity;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.ProductTypeEntity;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.UnitOfMeasureEntity;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository.ICategoryRepository;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository.IProductTypeRepository;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository.IUnitOfMeasureRepository;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.input.backup.StockBackupFormat;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.entity.StockEntity;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.repository.StockRepository;
import co.unicauca.edu.co.contables.notifications.infrastructure.input.backup.ThirdReplicaBackupParticipant;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.entity.ThirdReplicaEntity;
import co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.repository.IThirdReplicaRepository;
import jakarta.persistence.EntityManager;

/**
 * T13: ida y vuelta GLOBAL de la copia de seguridad completa, por el flujo REAL del coordinador
 * ({@code exportTo} + {@code restore}) con TODOS los participantes registrados en el contexto.
 *
 * <p>Se siembra una empresa en el tenant A con datos en todos los módulos (empresa, configuración, terceros y
 * réplicas, PUC, impuestos, productos, réplicas de inventario, movimientos de kardex, banking, facturas, cartera,
 * libro contable, tesorería, financial y auxiliary) y se restaura (1) como empresa NUEVA en otro tenant y (2) como
 * DUPLICADO en el mismo tenant. Comprueba conteos == manifest, integridad sin huérfanos, FKs a filas del destino,
 * saldos del PUC recalculados, origen intacto, que no se publique nada a Rabbit y el rollback total ante un fallo.
 *
 * <p><b>Truco documentado (UNIQUE global de {@code kardex_products.reference})</b>: mientras esa restricción sea
 * global, la referencia del origen impide restaurar la misma referencia en otra empresa (es lo que detecta el
 * preflight del participante {@code inventory.kardex}). Igual que
 * {@code InventoryBackupRoundTripTest#releaseSourceKardexReferences}, entre el export y cada restore se "liberan"
 * las referencias existentes (se les agrega un sufijo), como si el origen ya no las ocupara. Por eso el duplicado se
 * hace con {@code exportTo} + {@code restore(DUPLICATE)} y no con {@code duplicate()} (que no deja punto de
 * intervención entre ambos pasos). Es el único cambio que el test hace al origen; los conteos y saldos se
 * comparan antes y después.
 *
 * <p>Se siembran movimientos en AMBOS kardex (promedio y PEPS: capas + detalle de salida) aunque la empresa use
 * promedio ponderado: las réplicas de producto de los dos kardex viajan siempre, así que los movimientos del
 * otro kardex también se restauran.
 */
@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
@Import(TestMultiTenancyConfig.class)
@Sql(scripts = { "/enterprise-test-schema.sql", "/thirds-test-schema.sql", "/full-backup-test-schema.sql" },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FullEnterpriseBackupRoundTripTest {

    private static final String TENANT_A = "full-tenant-A";
    private static final String TENANT_B = "full-tenant-B";
    private static final AtomicLong RUN = new AtomicLong();

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;
    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Autowired private IEnterpriseBackupCoordinator coordinator;
    @Autowired private List<BackupParticipant> participants;
    @Autowired private ZipBackupFormat format;
    @Autowired private TransactionTemplate tx;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private EnterpriseRepository enterpriseRepository;
    @Autowired private EntityManager em;
    @Autowired private JdbcTemplate jdbc;

    // configuración
    @Autowired private DocumentClassRepository documentClasses;
    @Autowired private DocumentTypeRepository documentTypes;
    @Autowired private CostCenterRepository costCenters;
    @Autowired private AccountingCalendarRepository calendar;
    @Autowired private ITagRepository tags;
    // terceros
    @Autowired private TypeIdRepository typeIds;
    @Autowired private ThirdTypeRepository thirdTypes;
    @Autowired private ThirdRepository thirds;
    @Autowired private ThirdsAndTypesRepository thirdLinks;
    @Autowired private IThirdReplicaRepository thirdReplicas;
    // PUC / impuestos
    @Autowired private IAccountCatalogueRepository accounts;
    @Autowired private ITaxRepository taxRepository;
    // productos / inventario
    @Autowired private IUnitOfMeasureRepository units;
    @Autowired private IProductTypeRepository productTypes;
    @Autowired private ICategoryRepository categories;
    @Autowired private co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository.IProductRepository products;
    @Autowired private StockRepository stocks;
    @Autowired private co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.repository.IProductRepository kardexReplicas;
    @Autowired private co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IProductRepository pepsReplicas;
    // contabilidad
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

    @TempDir
    Path tmp;

    /** La interacción de arranque (RabbitMessagingTemplate.afterPropertiesSet) no cuenta: se mide solo el flujo. */
    @BeforeEach
    void clearRabbitInvocations() {
        clearInvocations(rabbitTemplate);
    }

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    private record Seed(UUID enterprise, Set<Long> accountIds, Set<Long> thirdIds, Set<Long> productIds,
            Set<Long> taxIds, Set<Long> costCenterIds, Set<Long> entryIds, long factCode, String entryCode,
            String receiptCode, String reference) {
    }

    /* ------------------------------------------------------------------ */
    /* Siembra                                                            */
    /* ------------------------------------------------------------------ */

    private AccountCatalogueEntity account(String ent, String code, NatureEnum nature, AccountCatalogueEntity parent) {
        return accounts.save(AccountCatalogueEntity.builder()
                .code(code).description("Cuenta " + code).nature(nature)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .parent(parent).crossing(false).costCenter(true).status(true)
                .idEnterprise(ent).amount(new BigDecimal("999.0000")).usageCount(2).build());
    }

    private AccountingMovementEntity line(AccountingEntryEntity entry, Long account, long third, String debit,
            String credit) {
        AccountingMovementEntity m = new AccountingMovementEntity();
        m.setAccount(account);
        m.setThirdPartyId(third);
        m.setDescription("linea");
        m.setDebit(new BigDecimal(debit));
        m.setCredit(new BigDecimal(credit));
        m.setAccountingEntry(entry);
        return m;
    }

    private AccountingEntryEntity entry(String ent, String code, AccountingEntryStatus status, Long source,
            String type, Long costCenter) {
        AccountingEntryEntity e = new AccountingEntryEntity();
        e.setIdEnterprise(ent);
        e.setCode(code);
        e.setDate(LocalDate.of(2026, 3, 1));
        e.setDescription("Asiento " + code);
        e.setStatus(status);
        e.setSourceDocumentId(source);
        e.setType(type);
        e.setCenterCostId(costCenter);
        e.setMovements(new ArrayList<>());
        return e;
    }

    private void kardexMove(long productId, MovementType type, int quantity, String unit, int balQty,
            String balUnit, String total, String date) {
        var k = new co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity();
        k.setFactCode("FV-" + productId + "-" + date.substring(8, 10));
        k.setQuantity(quantity);
        k.setUnitPrice(new BigDecimal(unit));
        k.setDetails("Movimiento " + type);
        k.setType(type);
        k.setBalanceQuantity(balQty);
        k.setBalanceUnitPrice(new BigDecimal(balUnit));
        k.setTotalBalance(new BigDecimal(total));
        k.setDate(ZonedDateTime.parse(date + "T10:00:00-05:00"));
        k.setProductId(productId);
        em.persist(k);
    }

    private co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.KardexEntity pepsMove(
            co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.ProductEntity product,
            co.unicauca.edu.co.contables.inventory.peps.domain.enums.MovementType type, int quantity, String unit,
            int available, String date) {
        var k = new co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.KardexEntity();
        k.setFactCode("PEPS-" + date.substring(8, 10));
        k.setDate(ZonedDateTime.parse(date + "T10:00:00-05:00"));
        k.setDetails("Capa " + type);
        k.setQuantity(quantity);
        k.setUnitPrice(new BigDecimal(unit));
        k.setType(type);
        k.setAvailableQuantity(available);
        k.setProduct(product);
        em.persist(k);
        return k;
    }

    private ThirdEntity third(String ent, TypeIdEntity type, long idNumber, String name) {
        return thirds.save(ThirdEntity.builder().entId(ent).typeId(type).personType(ePersonType.Natural)
                .names(name).lastNames("Perez").idNumber(idNumber).verificationNumber(7L).state(true)
                .country("CO").province("Cauca").city("Popayan").address("Calle 1").phoneNumber("300")
                .email(name.toLowerCase() + "@x.co").usageCount(5).build());
    }

    /** Empresa de promedio ponderado con datos en todos los módulos. Debe correr con TenantContext = tenant A. */
    private Seed seedTenantA() {
        long run = RUN.incrementAndGet();
        String suffix = Long.toString(Math.floorMod(System.nanoTime(), 100000000L) + 10000000L);
        String nit = "90" + suffix;
        long factCode = 880000000L + Math.floorMod(System.nanoTime(), 100000L);
        String reference = "REF-" + suffix + "-" + run;
        return tx.execute(s -> {
            // ---- empresa (con catálogos de ubicación e impositivos) ----
            TaxLiabilityEntity liability = TaxLiabilityEntity.builder().name("IVA " + suffix).build();
            em.persist(liability);
            TaxPayerTypeEntity payerType = TaxPayerTypeEntity.builder().name("Jurídica " + suffix).build();
            em.persist(payerType);
            EnterpriseTypeEntity enterpriseType = EnterpriseTypeEntity.builder().name("Matriz " + suffix).build();
            em.persist(enterpriseType);
            PersonTypeEntity personType = PersonTypeEntity.builder().type("JURIDICA").name("Empresa")
                    .surname("A").bussinessName("Empresa A SAS").build();
            em.persist(personType);
            CountryEntity country = CountryEntity.builder().name("Colombia " + suffix).build();
            em.persist(country);
            DepartmentEntity department = DepartmentEntity.builder().name("Cauca").country(country).build();
            em.persist(department);
            CityEntity city = CityEntity.builder().name("Popayán").department(department).build();
            em.persist(city);
            LocationEntity location = LocationEntity.builder().address("Calle 1 #2-3").city(city)
                    .country(country).department(department).build();
            em.persist(location);
            EnterpriseEntity enterprise = EnterpriseEntity.builder().idUser("u-a").name("Empresa A " + suffix)
                    .nit(nit).DV("7").phone("3001234567").branch("Tecnología").email("a@empresa.co")
                    .state(StateEnum.ACTIVE).inventoryConfigurationType(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE)
                    .mainActivity(6201L).secondaryActivity(6202L).taxLiabilities(new ArrayList<>(List.of(liability)))
                    .taxPayerType(payerType).enterpriseType(enterpriseType).personType(personType)
                    .location(location).build();
            em.persist(enterprise);
            em.flush();
            UUID source = enterprise.getId();
            String ent = source.toString();

            // ---- configuración ----
            DocumentClassEntity docClass = documentClasses.save(
                    DocumentClassEntity.builder().name("Contable").idEnterprise(ent).status(true).build());
            documentTypes.save(DocumentTypeEntity.builder().prefix("FV").name("Factura de venta")
                    .documentClass(docClass).module("SALES").idEnterprise(ent).status(true).usageCount(4).build());
            CostCenterEntity ccRoot = costCenters.save(CostCenterEntity.builder().code("CC1").name("Principal")
                    .idEnterprise(ent).status(true).usageCount(3).build());
            CostCenterEntity ccChild = costCenters.save(CostCenterEntity.builder().code("CC1-1").name("Sede")
                    .idEnterprise(ent).status(true).usageCount(1).build());
            ccChild.setParent(ccRoot);
            costCenters.save(ccChild);
            calendar.save(AccountingCalendarEntity.builder().idEnterprise(ent).date(LocalDate.of(2026, 1, 31)).build());
            TagEntity tag = new TagEntity();
            tag.setEnterpriseId(ent);
            tag.setTitle("Etiqueta");
            tag.setDescription("No comercial");
            tags.save(tag);

            // ---- terceros y réplicas ----
            TypeIdEntity cc = typeIds.save(TypeIdEntity.builder().tiId("CC").tiName("Cédula").tientId(ent)
                    .classification(PersonClassification.NATURAL_PERSON).status(true).build());
            ThirdTypeEntity cliente = thirdTypes.save(ThirdTypeEntity.builder().ttName("Cliente").ttentId(ent)
                    .status(true).build());
            ThirdTypeEntity proveedor = thirdTypes.save(ThirdTypeEntity.builder().ttName("Proveedor")
                    .ttentId(ent).status(true).build());
            ThirdEntity ana = third(ent, cc, 1001L + run, "Ana");
            ThirdEntity bar = third(ent, cc, 2002L + run, "Bar");
            thirdLinks.save(ThirdsAndTypesEntity.builder().thId(ana.getThId()).ttId(cliente.getTtId()).build());
            thirdLinks.save(ThirdsAndTypesEntity.builder().thId(bar.getThId()).ttId(proveedor.getTtId()).build());
            for (ThirdEntity t : List.of(ana, bar)) {
                ThirdReplicaEntity r = new ThirdReplicaEntity();
                r.setThirdId(t.getThId());
                r.setEnterpriseId(ent);
                r.setFullName(t.getNames());
                r.setEmail(t.getNames() + "@x.co");
                r.setActive(true);
                thirdReplicas.save(r);
            }
            long third1 = ana.getThId();
            long third2 = bar.getThId();

            // ---- PUC e impuestos ----
            AccountCatalogueEntity p1105 = account(ent, "1105", NatureEnum.DEBIT, null);
            AccountCatalogueEntity caja = account(ent, "110505", NatureEnum.DEBIT, p1105);
            AccountCatalogueEntity cartera = account(ent, "1305", NatureEnum.DEBIT, null);
            AccountCatalogueEntity ingresos = account(ent, "4135", NatureEnum.CREDIT, null);
            AccountCatalogueEntity proveedores = account(ent, "2205", NatureEnum.CREDIT, null);
            AccountCatalogueEntity ivaVentas = account(ent, "2408", NatureEnum.CREDIT, null);
            AccountCatalogueEntity ivaCompras = account(ent, "2409", NatureEnum.DEBIT, null);
            AccountCatalogueEntity inventario = account(ent, "1435", NatureEnum.DEBIT, null);
            AccountCatalogueEntity costo = account(ent, "6135", NatureEnum.DEBIT, null);
            AccountCatalogueEntity devoluciones = account(ent, "4175", NatureEnum.DEBIT, null);
            TaxEntity iva = taxRepository.save(TaxEntity.builder().code("IVA19").description("IVA 19").interest(19.0)
                    .salesTax(ivaVentas).purchaseTax(ivaCompras).idEnterprise(ent).status(true).usageCount(3).build());
            taxRepository.save(TaxEntity.builder().code("EXENTO").description("Exento").interest(0.5)
                    .idEnterprise(ent).status(true).usageCount(0).build());

            // ---- productos, réplicas de inventario y movimientos del kardex ----
            UnitOfMeasureEntity kg = new UnitOfMeasureEntity();
            kg.setName("Kilogramo " + suffix);
            kg.setDescription("u");
            kg.setAbbreviation("kg");
            kg.setEnterpriseId(ent);
            kg.setState(true);
            kg = units.save(kg);
            ProductTypeEntity type = productTypes.save(ProductTypeEntity.builder().name("Bien " + suffix)
                    .description("d").enterpriseId(ent).state(true).build());
            CategoryEntity category = new CategoryEntity();
            category.setName("Lacteos " + suffix);
            category.setDescription("c");
            category.setEnterpriseId(ent);
            category.setInventoryId(inventario.getId());
            category.setCostId(costo.getId());
            category.setSaleId(ingresos.getId());
            category.setReturnId(devoluciones.getId());
            category.setTaxes(new ArrayList<>(List.of(iva.getId())));
            category.setState(true);
            category = categories.save(category);

            Set<Long> productIds = new java.util.HashSet<>();
            List<co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.ProductEntity> pepsProducts =
                    new ArrayList<>();
            int[] balances = { 12, 7 };
            for (int i = 1; i <= 2; i++) {
                var p = new co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.ProductEntity();
                p.setCode("C" + i);
                p.setName("Producto " + i + " " + suffix);
                p.setDescription("desc");
                p.setQuantity(balances[i - 1]);
                p.setUnitOfMeasureId(kg.getId());
                p.setCategoryId(category.getId());
                p.setProductTypeId(type.getId());
                p.setEnterpriseId(ent);
                p.setCost(1500.5);
                p.setState(true);
                p.setReference(reference + "-" + i);
                p.setPresentation("caja");
                p.setUsageCount(7);
                p = products.save(p);
                productIds.add(p.getId());

                StockEntity st = new StockEntity();
                st.setProductId(p.getId());
                st.setName(p.getName());
                st.setEnterpriseId(ent);
                st.setQuantity(balances[i - 1]);
                st.setPrice(new BigDecimal("6.00"));
                st.setState(true);
                stocks.save(st);

                // Como hoy: los listeners crean la réplica en AMBOS kardex, sin mirar el método.
                var k = new co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity();
                k.setProductId(p.getId());
                k.setReference(p.getReference());
                k.setName(p.getName());
                k.setPresentation("caja");
                k.setEnterpriseId(ent);
                k.setState(true);
                kardexReplicas.save(k);
                var pp = new co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.ProductEntity();
                pp.setProductId(p.getId());
                pp.setReference(p.getReference());
                pp.setName(p.getName());
                pp.setPresentation("caja");
                pp.setEnterpriseId(ent);
                pp.setState(true);
                pepsProducts.add(pepsReplicas.save(pp));
            }
            List<Long> pids = productIds.stream().sorted().toList();
            long product1 = pids.get(0);
            long product2 = pids.get(1);
            kardexMove(product1, MovementType.PURCHASE, 10, "5.00", 10, "5.00", "50.00", "2026-01-01");
            kardexMove(product1, MovementType.SALE, 4, "5.00", 6, "5.00", "30.00", "2026-01-02");
            kardexMove(product1, MovementType.PURCHASE, 6, "7.00", 12, "6.00", "72.00", "2026-01-03");
            kardexMove(product2, MovementType.PURCHASE, 7, "3.00", 7, "3.00", "21.00", "2026-01-01");
            // Movimientos PEPS (capas + detalle de salida) en una empresa de promedio: viajan porque la réplica PEPS
            // viaja siempre, sea cual sea el método (el bug de importación "ids nuevos [..] sin réplica").
            var pepsPurchase = pepsMove(pepsProducts.get(0),
                    co.unicauca.edu.co.contables.inventory.peps.domain.enums.MovementType.PURCHASE, 10, "5.00", 3,
                    "2026-01-01");
            var pepsSale = pepsMove(pepsProducts.get(0),
                    co.unicauca.edu.co.contables.inventory.peps.domain.enums.MovementType.SALE, 7, "5.00", 0,
                    "2026-01-02");
            DetailOutputEntity pepsDetail = new DetailOutputEntity();
            pepsDetail.setQuantityUsed(7);
            pepsDetail.setUnitPrice(new BigDecimal("5.00"));
            pepsDetail.setMovementSale(pepsSale);
            pepsDetail.setMovementOrigin(pepsPurchase);
            em.persist(pepsDetail);

            // ---- banking ----
            BankEntity bank = banks.save(BankEntity.builder().code("07").name("Banco A")
                    .currencies(Set.of(Currency.COP, Currency.USD)).status(true).idEnterprise(ent).build());
            BankAccountEntity bankAccount = bankAccounts.save(BankAccountEntity.builder()
                    .accountNumber(100000L + Math.floorMod(System.nanoTime(), 899999L)).bank(bank)
                    .accountType(AccountType.AHORROS).accountingAccount(caja).status(true).idEnterprise(ent)
                    .usageCount(4).build());
            PaymentMethodEntity method = paymentMethods.save(PaymentMethodEntity.builder().name("Efectivo")
                    .accountingAccount(caja).status(true).idEnterprise(ent).usageCount(3).build());

            // ---- cartera (debt_payments) ----
            InvoiceReplicaEntity dpInvoice = new InvoiceReplicaEntity();
            dpInvoice.setFactCode(factCode);
            dpInvoice.setEntId(ent);
            dpInvoice.setThirdId(third1);
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
            dpReceipt.setThirdPartyId(third1);
            dpReceipt.setEnterpriseId(ent);
            dpReceipt.setPaymentMethodId(method.getId());
            dpReceipt.setPaymentMethodAccount(110505L);
            dpReceipt.setReceiptType(ReceiptType.INVOICE_PAYMENT);
            dpReceipt.setStatus(ReceiptStatus.FINALIZED);
            dpReceipt.setIssueDate(LocalDate.of(2026, 3, 1));
            dpReceipt.setTotalAmount(new BigDecimal("600"));
            dpReceipt.setCenterCostId(ccRoot.getId());
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
            dpWriteOff.setCode("WO-" + suffix);
            dpWriteOff.setTotalAmount(100L);
            dpWriteOff.setWriteOffDate(LocalDate.of(2026, 3, 5));
            dpWriteOff.setDebitAuxiliaryAccount(4135L);
            dpWriteOff.setDebitAuxiliaryAccountId(ingresos.getId());
            dpWriteOff.setCostCenterId(ccRoot.getId());
            dpWriteOff.setThirdId(third1);
            dpWriteOff.setStatus(WriteOffStatus.CONFIRMED);
            dpWriteOff.setEnterpriseId(ent);
            WriteOffDetailEntity dpWoDetail = new WriteOffDetailEntity();
            dpWoDetail.setInvoiceId(dpInvoice.getId());
            dpWoDetail.setAmountWrittenOff(100L);
            dpWoDetail.setAccountingAccount(1305L);
            dpWoDetail.setPortfolioWriteOff(dpWriteOff);
            dpWriteOff.setDetails(new ArrayList<>(List.of(dpWoDetail)));
            dpWriteOff = dpWriteOffs.save(dpWriteOff);

            // ---- libro contable (ledger) ----
            var accInvoice = new co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.InvoiceReplicaEntity();
            accInvoice.setFactCode(factCode);
            accInvoice.setEntId(ent);
            accInvoice.setThirdId(third1);
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
            accReceipt.setThirdPartyId(third1);
            accReceipt.setPaymentMethodId(method.getId());
            accReceipt.setPaymentMethodAccount(110505L);
            accReceipt.setStatus("FINALIZED");
            accReceipt.setIssueDate(LocalDate.of(2026, 3, 1));
            accReceipt.setTotalAmount(new BigDecimal("600"));
            accReceipt.setProcessingStatus(ProcessingStatus.PROCESSED);
            accReceipt.setCenterCostId(ccRoot.getId());
            var accDetail = new co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity.ReceiptDetailEntity();
            accDetail.setOriginalInvoiceId(accInvoice.getId());
            accDetail.setInvoiceCode("F-1");
            accDetail.setAccountingAccount(1305L);
            accDetail.setAmountPaid(new BigDecimal("600"));
            accDetail.setReceipt(accReceipt);
            accReceipt.setDetails(new ArrayList<>(List.of(accDetail)));
            accReceipt = accReceipts.save(accReceipt);

            String entryCode1 = "AE-" + suffix.substring(0, Math.min(8, suffix.length()));
            AccountingEntryEntity e1 = entry(ent, entryCode1, AccountingEntryStatus.ACTIVE, accReceipt.getId(),
                    "RECEIPT", ccRoot.getId());
            e1.getMovements().add(line(e1, caja.getId(), third1, "600", "0"));
            e1.getMovements().add(line(e1, cartera.getId(), third1, "0", "600"));
            e1 = entries.save(e1);
            AccountingEntryEntity e2 = entry(ent, "AC-" + suffix.substring(0, Math.min(8, suffix.length())),
                    AccountingEntryStatus.ACTIVE, dpWriteOff.getId(), "PORTFOLIO_WRITEOFF", ccRoot.getId());
            e2.getMovements().add(line(e2, ingresos.getId(), third1, "100", "0"));
            e2.getMovements().add(line(e2, cartera.getId(), third1, "0", "100"));
            e2 = entries.save(e2);
            AccountingEntryEntity e3 = entry(ent, "AE-V" + suffix.substring(0, Math.min(7, suffix.length())),
                    AccountingEntryStatus.VOIDED, accReceipt.getId(), "RECEIPT", ccRoot.getId());
            e3.getMovements().add(line(e3, caja.getId(), third1, "50", "0"));
            e3.getMovements().add(line(e3, cartera.getId(), third1, "0", "50"));
            e3 = entries.save(e3);

            // ---- tesorería ----
            SupplierInvoiceReplicaEntity si = new SupplierInvoiceReplicaEntity();
            si.setSourceInvoiceId(9001L);
            si.setReference("FP-" + suffix);
            si.setEnterpriseId(ent);
            si.setSupplierId(third2);
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
            vd.setSupplierId(third2);
            vd.setInvoiceId(si.getId());
            vd.setInvoiceReference("FP-" + suffix);
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
            sd.setSupplierId(third2);
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
            pwd.setSupplierId(third2);
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

            // ---- facturas (legacy + skeleton + devolución) ----
            ProductEntity legacyProduct = ProductEntity.builder().productId(product1).code("P1").descount(0)
                    .amount(2).description("Prod").vat(19).unitPrice(100).subtotal(200).build();
            FactureEntity lf = FactureEntity.builder().entId(ent).thId(third1).factCode(factCode)
                    .factObservations("legado").factureType(eFactureType.Venta).factSubtotals(200.0)
                    .facSalesTax(38.0).facWithholdingSource(0.0).descounts(0.0).build();
            lf.getFactProducts().add(legacyProduct);
            legacyFactures.save(lf);

            SkeletonFacture sk = SkeletonFacture.builder().factCode(factCode).entId(ent).thId(third1)
                    .totalValue("1000").totalPay("700").pendingValue("300").issueDate(LocalDate.of(2026, 2, 1))
                    .expirationDate(LocalDate.of(2026, 4, 1)).accountingAccount(1305L)
                    .factureType(SkeletonFactureType.SALE).purchaseStatus(PurchaseInvoiceStatus.ACTIVE).build();
            sk.addProduct(SkeletonProduct.builder().productId(product1).amount(2).description("Prod")
                    .discount(0.0).unitPrice(500.0).subtotal(1000.0).taxPercentage(List.of(19)).build());
            skeletons.save(sk);
            returns.save(SkeletonReturn.builder().originalFactCode(factCode).productId(product1)
                    .returnedQuantity(1).returnType(SkeletonReturn.ReturnType.RETURN_ON_SALE)
                    .details("devolucion").build());

            // ---- financial ----
            OffsetDateTime now = OffsetDateTime.now();
            em.persist(FinancialStatementTemplateEntity.builder().entId(ent).name("Clasica").pathLogotype("/logos/c")
                    .alignment("LEFT").font("Arial").fontSize(12).mainColor("#112233").isDefault(true)
                    .createdAt(now).build());
            FinancialStatementEntity fs = FinancialStatementEntity.builder().reportId(UUID.randomUUID())
                    .type(EFinancialStatementType.INCOME_STATEMENT).entId(ent).createdAt(now)
                    .reportSnapshot("{\"total\":123}").build();
            em.persist(fs);
            em.persist(FinancialStatementAnnotationEntity.builder().financialStatement(fs).text("nota")
                    .createdAt(now).updatedAt(now).build());
            em.persist(FinancialStatementHistoryEntity.builder().financialStatement(fs).state("SENT")
                    .deliveryWay("EMAIL").createdAt(now).build());
            em.persist(FinancialStatementLogEntity.builder().financialStatement(fs).eventType("CREATED")
                    .message("evento").icon("check").color("green").createdAt(now).build());

            // ---- auxiliary (referencias blandas a tercero y centro de costo) ----
            AuxiliaryBookEntity book = new AuxiliaryBookEntity();
            book.setPublicId(UUID.randomUUID().toString());
            book.setType(EAuxiliaryBookType.THIRD_PARTY);
            book.setEntId(ent);
            book.setUserId(1L);
            book.setFormat(EAuxiliaryBookFormat.PDF);
            book.setCriteria(criteria(third1, ccRoot.getId()));
            AuxiliaryBookLogEntity log = new AuxiliaryBookLogEntity();
            log.setPublicId(UUID.randomUUID().toString());
            log.setETypeEvent(ETypeEvent.REGISTERED);
            log.setMessage("registrado");
            log.setAuxiliaryBook(book);
            book.setLog(new ArrayList<>(List.of(log)));
            AuxiliaryBookHistoryEntity bookHistory = new AuxiliaryBookHistoryEntity();
            bookHistory.setPublicId(UUID.randomUUID().toString());
            bookHistory.setState(EState.GENERATED);
            bookHistory.setDeliveryWay(EDeliveryWay.DOWNLOAD);
            bookHistory.setAuxiliaryBook(book);
            book.setHistory(bookHistory);
            em.persist(book);
            ScheduledReportEntity job = new ScheduledReportEntity();
            job.setPublicId(UUID.randomUUID().toString());
            job.setBookType(EAuxiliaryBookType.THIRD_PARTY);
            job.setCriteria(criteria(third1, ccRoot.getId()));
            job.setFrequency(EFrequency.MONTHLY);
            job.setStartAt(Instant.parse("2026-01-01T00:00:00Z"));
            job.setNextRunAt(Instant.parse("2026-12-01T00:00:00Z"));
            job.setStatus(EJobStatus.ACTIVE);
            job.setEntId(ent);
            job.setUserId(1L);
            job.setOwnerSub("user-a");
            job.setCreatedBy("user-a");
            job.setDeliveryWay(EDeliveryWay.EMAIL);
            job.setEmailTo("contador@example.com");
            job.setEmailSubject("Libro mensual");
            em.persist(job);
            // SIN em.flush() aquí: un segundo flush haría dirty-check de la bolsa de logs y las entidades de auxiliary
            // (Lombok @Data bidireccional) se desbordan en hashCode(); un único flush al confirmar es inofensivo.

            return new Seed(source,
                    Set.of(p1105.getId(), caja.getId(), cartera.getId(), ingresos.getId(), proveedores.getId(),
                            ivaVentas.getId(), ivaCompras.getId(), inventario.getId(), costo.getId(),
                            devoluciones.getId()),
                    Set.of(third1, third2), productIds, Set.of(iva.getId()),
                    Set.of(ccRoot.getId(), ccChild.getId()), Set.of(e1.getId(), e2.getId(), e3.getId()), factCode,
                    entryCode1, receiptCode, reference);
        });
    }

    private static AuxiliaryBookCriteriaEntity criteria(long thirdId, long costCenterId) {
        AuxiliaryBookCriteriaEntity c = new AuxiliaryBookCriteriaEntity();
        c.setCriteriaType(ECriteriaType.ACCOUNT);
        c.setFromRange(1105L);
        c.setToRange(1105L);
        c.setThirdPartyId(String.valueOf(thirdId));
        c.setCostCenterId(String.valueOf(costCenterId));
        c.setStartDate(LocalDate.of(2026, 1, 1));
        c.setEndDate(LocalDate.of(2026, 12, 31));
        return c;
    }

    /* ------------------------------------------------------------------ */
    /* Utilidades                                                         */
    /* ------------------------------------------------------------------ */

    private Path export(Seed seed) {
        TenantContext.setTenantId(TENANT_A);
        Path zip = tmp.resolve("full-" + seed.enterprise() + ".zip");
        ManifestV2 manifest = coordinator.exportTo(seed.enterprise(), zip, "u-a", null);
        assertThat(manifest.modules()).extracting(ManifestV2.ModuleEntry::key)
                .containsAll(participants.stream().map(BackupParticipant::moduleKey).toList());
        assertThat(manifest.excluded()).isEqualTo(BackupKeys.EXCLUDED);
        return zip;
    }

    /** Ver el comentario de la clase: el UNIQUE global de kardex_products.reference. */
    private void releaseKardexReferences() {
        jdbc.update("UPDATE kardex_products SET reference = reference || '~' || id WHERE reference NOT LIKE '%~%'");
    }

    private <T> T inTenant(String tenant, java.util.function.Supplier<T> work) {
        String previous = TenantContext.getTenantId();
        TenantContext.setTenantId(tenant);
        try {
            return tx.execute(s -> work.get());
        } finally {
            if (previous == null) {
                TenantContext.clear();
            } else {
                TenantContext.setTenantId(previous);
            }
        }
    }

    private Map<String, Map<String, Long>> countsOf(String tenant, UUID enterprise) {
        return inTenant(tenant, () -> {
            Map<String, Map<String, Long>> all = new LinkedHashMap<>();
            for (BackupParticipant p : participants.stream().sorted(Comparator.comparingInt(BackupParticipant::order))
                    .toList()) {
                all.put(p.moduleKey(), new LinkedHashMap<>(p.count(enterprise)));
            }
            return all;
        });
    }

    private long account999(String tenant, UUID enterprise) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM account WHERE tenant_id = ? AND id_enterprise = ? AND amount = 999",
                Long.class, tenant, enterprise.toString());
    }

    private Map<String, Long> rowCountsOfAllTables() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String table : jdbc.queryForList("SELECT table_name FROM information_schema.tables "
                + "WHERE table_schema = 'PUBLIC' AND table_type = 'BASE TABLE' ORDER BY table_name", String.class)) {
            counts.put(table, jdbc.queryForObject("SELECT COUNT(*) FROM \"" + table + "\"", Long.class));
        }
        return counts;
    }

    private long jpql(String tenant, String query, Map<String, Object> params) {
        return inTenant(tenant, () -> {
            var q = em.createQuery(query, Long.class);
            params.forEach(q::setParameter);
            return q.getSingleResult();
        });
    }

    private BigDecimal amount(String tenant, String ent, String code) {
        return jdbc.queryForObject("SELECT amount FROM account WHERE tenant_id = ? AND id_enterprise = ? AND code = ?",
                BigDecimal.class, tenant, ent, code);
    }

    /* ------------------------------------------------------------------ */
    /* Verificación de un restore                                         */
    /* ------------------------------------------------------------------ */

    private void assertRestoredCopy(Seed seed, Path zip, RestoreResult result, String tenant) {
        UUID target = result.enterpriseId();
        String ent = target.toString();
        assertThat(target).isNotEqualTo(seed.enterprise());

        // todos los módulos registrados se restauraron y los conteos == manifest (el coordinador ya lo exige; se
        // vuelve a comprobar aquí contra el manifest del ZIP y se exige que las entradas sembradas no queden vacías)
        assertThat(result.restoredModules()).containsExactlyInAnyOrderElementsOf(
                participants.stream().map(BackupParticipant::moduleKey).toList());
        assertThat(result.skippedModules()).isEmpty();
        ManifestV2 manifest;
        try (var reader = format.open(zip)) {
            manifest = reader.manifest();
        }
        Map<String, Map<String, Long>> inDb = countsOf(tenant, target);
        long compared = 0;
        for (ManifestV2.ModuleEntry module : manifest.modules()) {
            Map<String, Long> actual = inDb.get(module.key());
            assertThat(actual).as("participante " + module.key()).isNotNull();
            for (ManifestV2.EntryInfo e : module.entries()) {
                Long db = actual.get(e.path());
                if (db != null) {
                    assertThat(db).as(module.key() + " :: " + e.path()).isEqualTo(e.count());
                    compared++;
                }
            }
        }
        assertThat(compared).isGreaterThan(30);
        for (String nonEmpty : List.of(ThirdsBackupParticipant.ENTRY_THIRDS, ThirdReplicaBackupParticipant.ENTRY_REPLICAS,
                ConfigurationBackupParticipant.ENTRY_COST_CENTERS, PucBackupParticipant.ENTRY_ACCOUNTS,
                TaxesBackupParticipant.ENTRY_TAXES, ProductsBackupFormat.PRODUCTS, StockBackupFormat.STOCK,
                KardexBackupFormat.PRODUCTS, KardexMovementsBackupParticipant.ENTRY_KARDEX,
                BankingBackupParticipant.ENTRY_BANK_ACCOUNTS, DebtPaymentsBackupParticipant.ENTRY_RECEIPTS,
                LedgerBackupParticipant.ENTRY_ENTRIES, TreasuryBackupParticipant.ENTRY_VOUCHERS,
                FactureBackupParticipant.ENTRY_SKELETON_FACTURES, FinancialBackupParticipant.ENTRY_STATEMENTS,
                AuxiliaryBackupParticipant.ENTRY_BOOKS)) {
            assertThat(inDb.values().stream().filter(m -> m.containsKey(nonEmpty)).findFirst().orElseThrow().get(nonEmpty))
                    .as(nonEmpty).isPositive();
        }
        assertThat(inDb.get(BackupKeys.MODULE_INVENTORY_KARDEX_MOVEMENTS)).containsEntry(
                KardexMovementsBackupParticipant.ENTRY_KARDEX, 4L);
        // la empresa usa promedio ponderado pero ambas réplicas y los movimientos PEPS también viajan
        assertThat(inDb.get(BackupKeys.MODULE_INVENTORY_PEPS)).containsEntry(PepsBackupFormat.PRODUCTS, 2L);
        assertThat(inDb.get(BackupKeys.MODULE_INVENTORY_PEPS_MOVEMENTS))
                .containsEntry(PepsMovementsBackupParticipant.ENTRY_KARDEX, 2L)
                .containsEntry(PepsMovementsBackupParticipant.ENTRY_DETAILS, 1L);
        assertThat(inDb.get(BackupKeys.MODULE_PUC)).containsEntry(PucBackupParticipant.ENTRY_ACCOUNTS, 10L);

        // verifyIntegrity vacío en TODOS los participantes
        inTenant(tenant, () -> {
            for (BackupParticipant p : participants) {
                assertThat(p.verifyIntegrity(target)).as("huérfanos en " + p.moduleKey()).isEmpty();
            }
            return null;
        });

        // ninguna FK apunta a ids del origen: todas apuntan a filas de la empresa destino
        Map<String, Object> e = Map.of("e", ent);
        String acc = "(select a.id from " + AccountCatalogueEntity.class.getName() + " a where a.idEnterprise = :e)";
        String thr = "(select t.thId from " + ThirdEntity.class.getName() + " t where t.entId = :e)";
        String prd = "(select p.id from co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.ProductEntity p where p.enterpriseId = :e)";
        assertThat(jpql(tenant, "select count(m) from " + AccountingMovementEntity.class.getName()
                + " m where m.accountingEntry.idEnterprise = :e and (m.account not in " + acc
                + " or m.thirdPartyId not in " + thr + ")", e)).isZero();
        assertThat(jpql(tenant, "select count(c) from " + CategoryEntity.class.getName() + " c where c.enterpriseId = :e"
                + " and (c.inventoryId not in " + acc + " or c.costId not in " + acc + " or c.saleId not in " + acc
                + " or c.returnId not in " + acc + ")", e)).isZero();
        assertThat(jpql(tenant, "select count(p) from co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.ProductEntity p"
                + " where p.enterpriseId = :e and (p.categoryId not in (select c.id from " + CategoryEntity.class.getName()
                + " c where c.enterpriseId = :e) or p.unitOfMeasureId not in (select u.id from "
                + UnitOfMeasureEntity.class.getName() + " u where u.enterpriseId = :e) or p.productTypeId not in "
                + "(select t.id from " + ProductTypeEntity.class.getName() + " t where t.enterpriseId = :e))", e)).isZero();
        assertThat(jpql(tenant, "select count(s) from " + StockEntity.class.getName()
                + " s where s.enterpriseId = :e and s.productId not in " + prd, e)).isZero();
        assertThat(jpql(tenant, "select count(s) from " + SupplierInvoiceReplicaEntity.class.getName()
                + " s where s.enterpriseId = :e and (s.supplierId not in " + thr + " or s.payableAccountId not in "
                + acc + ")", e)).isZero();
        assertThat(jpql(tenant, "select count(i) from " + InvoiceReplicaEntity.class.getName()
                + " i where i.entId = :e and i.thirdId not in " + thr, e)).isZero();
        assertThat(jpql(tenant, "select count(k) from co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity k"
                + " where k.productId in " + prd, Map.of("e", ent))).isEqualTo(4L);
        assertThat(jpql(tenant, "select count(k) from co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.KardexEntity k"
                + " where k.product.enterpriseId = :e and k.product.productId in " + prd, Map.of("e", ent))).isEqualTo(2L);
        // las categorías referencian el impuesto NUEVO
        Set<Long> newTaxIds = new java.util.HashSet<>();
        inTenant(tenant, () -> {
            taxRepository.findBackupBatch(ent, 0L, PageRequest.of(0, 50)).forEach(t -> newTaxIds.add(t.getId()));
            return null;
        });
        assertThat(newTaxIds).hasSize(2).doesNotContainAnyElementsOf(seed.taxIds());
        inTenant(tenant, () -> {
            categories.findByEnterpriseId(ent, PageRequest.of(0, 10)).getContent().forEach(c ->
                    assertThat(newTaxIds).containsAll(c.getTaxes()));
            return null;
        });
        // FKs con SQL conocido
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM bank_accounts b JOIN account a ON a.id = b.accounting_account_id "
                + "WHERE b.tenant_id = ? AND b.id_enterprise = ? AND a.id_enterprise = ? AND a.tenant_id = ?",
                Long.class, tenant, ent, ent, tenant)).isEqualTo(1L);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM payment_vouchers v JOIN accounting_entries e ON e.id = v.accounting_entry_id "
                + "WHERE v.tenant_id = ? AND v.enterprise_id = ? AND e.id_enterprise = ?", Long.class, tenant, ent, ent))
                        .isEqualTo(1L);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM accounting_receipts r JOIN receipts d ON d.id = r.original_receipt_id "
                + "WHERE r.tenant_id = ? AND r.enterprise_id = ? AND d.enterprise_id = ? AND d.tenant_id = ?",
                Long.class, tenant, ent, ent, tenant)).isEqualTo(1L);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM payment_vouchers WHERE tenant_id = ? AND enterprise_id = ? AND idempotency_key IS NULL",
                Long.class, tenant, ent)).isEqualTo(1L);

        // numeración global: los códigos del origen siguen siendo del origen
        List<String> codes = jdbc.queryForList("SELECT code FROM accounting_entries WHERE tenant_id = ? AND id_enterprise = ?",
                String.class, tenant, ent);
        assertThat(codes).hasSize(3).doesNotContain(seed.entryCode());
        Long newFact = jdbc.queryForObject("SELECT fact_code FROM skeleton_factures WHERE tenant_id = ? AND ent_id = ?",
                Long.class, tenant, ent);
        assertThat(newFact).isNotEqualTo(seed.factCode());
        assertThat(jdbc.queryForObject("SELECT fact_code FROM FACTURES WHERE ent_id = ?", Long.class, ent)).isEqualTo(newFact);

        // los trabajos programados no corren en paralelo con el original
        assertThat(jdbc.queryForObject("SELECT status FROM scheduled_report WHERE ent_id = ?", String.class, ent))
                .isEqualTo("PAUSED");

        // D5: saldos del PUC recalculados desde asientos ACTIVOS + facturas, coherentes con los asientos
        assertThat(amount(tenant, ent, "1305")).isEqualByComparingTo("300");
        assertThat(amount(tenant, ent, "110505")).isEqualByComparingTo("600");
        assertThat(amount(tenant, ent, "1105")).isEqualByComparingTo("600");
        assertThat(amount(tenant, ent, "4135")).isEqualByComparingTo("-100");
        assertThat(amount(tenant, ent, "2205")).isEqualByComparingTo("0");
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM accounting_entries e WHERE e.tenant_id = ? AND e.id_enterprise = ? AND "
                        + "(SELECT COALESCE(SUM(m.debit),0) FROM accounting_movements m WHERE m.accounting_entry_id = e.id) <> "
                        + "(SELECT COALESCE(SUM(m.credit),0) FROM accounting_movements m WHERE m.accounting_entry_id = e.id)",
                Long.class, tenant, ent)).isZero();
    }

    private void assertSourceIntact(Seed seed, Map<String, Map<String, Long>> before) {
        assertThat(countsOf(TENANT_A, seed.enterprise())).isEqualTo(before);
        assertThat(account999(TENANT_A, seed.enterprise())).isEqualTo(10L);
        assertThat(jdbc.queryForObject("SELECT status FROM scheduled_report WHERE ent_id = ?", String.class,
                seed.enterprise().toString())).isEqualTo("ACTIVE");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM accounting_entries WHERE tenant_id = ? AND id_enterprise = ?",
                Long.class, TENANT_A, seed.enterprise().toString())).isEqualTo(3L);
    }

    /* ------------------------------------------------------------------ */
    /* Casos                                                              */
    /* ------------------------------------------------------------------ */

    @Test
    void empresaCompletaViajaComoEmpresaNuevaEnOtroTenant() {
        TenantContext.setTenantId(TENANT_A);
        Seed seed = seedTenantA();
        Map<String, Map<String, Long>> before = countsOf(TENANT_A, seed.enterprise());
        Path zip = export(seed);
        releaseKardexReferences();

        TenantContext.setTenantId(TENANT_B);
        RestoreResult result = coordinator.restore(zip, RestoreOptions.newEnterprise("Empresa B", null, false),
                new RestoreActor(TENANT_B, "u-b"), null);

        assertRestoredCopy(seed, zip, result, TENANT_B);
        TenantContext.setTenantId(TENANT_A);
        assertSourceIntact(seed, before);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void empresaCompletaSeDuplicaEnElMismoTenant() {
        TenantContext.setTenantId(TENANT_A);
        Seed seed = seedTenantA();
        Map<String, Map<String, Long>> before = countsOf(TENANT_A, seed.enterprise());
        Path zip = export(seed);
        releaseKardexReferences();

        RestoreResult result = coordinator.restore(zip, RestoreOptions.duplicate("Empresa A (copia)"),
                new RestoreActor(TENANT_A, "u-a2"), null);

        assertRestoredCopy(seed, zip, result, TENANT_A);
        assertSourceIntact(seed, before);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void siUnParticipanteFallaAMitadDelRestoreNoQuedaNingunaFilaDelDestino() {
        TenantContext.setTenantId(TENANT_A);
        Seed seed = seedTenantA();
        Path zip = export(seed);
        releaseKardexReferences();

        // Mismos participantes reales; el de tesorería (order 120) inserta todo y DESPUÉS falla: para entonces ya
        // restauraron enterprise, configuración, terceros, PUC, impuestos, productos, inventario, banking,
        // facturas, cartera y libro contable.
        List<BackupParticipant> withFailure = participants.stream()
                .map(p -> AccountingBackupKeys.MODULE_TREASURY.equals(p.moduleKey())
                        ? (BackupParticipant) new FailingAfterRestore(p) : p)
                .toList();
        IEnterpriseBackupCoordinator failing = new EnterpriseBackupCoordinatorImpl(withFailure, format,
                enterpriseRepository, em, transactionManager, "test", tmp.toString());

        TenantContext.setTenantId(TENANT_B);
        Map<String, Long> rowsBefore = rowCountsOfAllTables();
        assertThatThrownBy(() -> failing.restore(zip, RestoreOptions.newEnterprise("Empresa B", null, false),
                new RestoreActor(TENANT_B, "u-b"), null))
                        .isInstanceOf(RestoreFailedException.class)
                        .hasMessageContaining(AccountingBackupKeys.MODULE_TREASURY);

        assertThat(rowCountsOfAllTables()).as("el rollback no deja filas en ninguna tabla").isEqualTo(rowsBefore);
        verifyNoInteractions(rabbitTemplate);
    }

    /** Hace el restore real del participante y luego falla, para probar el rollback de lo ya insertado. */
    private record FailingAfterRestore(BackupParticipant delegate) implements BackupParticipant {
        @Override public String moduleKey() { return delegate.moduleKey(); }
        @Override public int order() { return delegate.order(); }
        @Override public int schemaVersion() { return delegate.schemaVersion(); }
        @Override public Set<String> requires() { return delegate.requires(); }
        @Override public void export(UUID enterpriseId, co.unicauca.edu.co.contables.commons.backup.BackupWriter writer) {
            delegate.export(enterpriseId, writer);
        }
        @Override public void preflight(co.unicauca.edu.co.contables.commons.backup.BackupReader reader, RestoreContext ctx) {
            delegate.preflight(reader, ctx);
        }
        @Override public void restore(co.unicauca.edu.co.contables.commons.backup.BackupReader reader,
                co.unicauca.edu.co.contables.commons.backup.IdMap idMap, RestoreContext ctx) {
            delegate.restore(reader, idMap, ctx);
            throw new IllegalArgumentException("fallo inyectado después de restaurar " + delegate.moduleKey());
        }
        @Override public Map<String, Long> count(UUID enterpriseId) { return delegate.count(enterpriseId); }
        @Override public List<String> verifyIntegrity(UUID enterpriseId) { return delegate.verifyIntegrity(enterpriseId); }
    }
}
