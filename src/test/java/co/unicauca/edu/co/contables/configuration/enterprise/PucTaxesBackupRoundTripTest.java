package co.unicauca.edu.co.contables.configuration.enterprise;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionTemplate;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.backup.PucBackupParticipant;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.repository.IAccountCatalogueRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.backup.TaxesBackupParticipant;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.entity.TaxEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.repository.ITaxRepository;
import co.unicauca.edu.co.contables.commons.backup.BackupIntegrityException;
import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.InMemoryIdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import co.unicauca.edu.co.contables.commons.backup.RestoreMode;
import co.unicauca.edu.co.contables.commons.backup.zip.ManifestV2;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupFormat;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupReader;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupWriter;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;

/**
 * Ida y vuelta de los participantes {@code puc} y {@code taxes}: datos en el tenant A, export con
 * {@link ZipBackupFormat} y restore como tenant B con {@link InMemoryIdMap}.
 */
@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
@Import(TestMultiTenancyConfig.class)
@Sql(scripts = "/enterprise-test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PucTaxesBackupRoundTripTest {

    private static final String TENANT_A = "tenant-A";
    private static final String TENANT_B = "tenant-B";

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;

    @Autowired
    private PucBackupParticipant puc;
    @Autowired
    private TaxesBackupParticipant taxes;
    @Autowired
    private IAccountCatalogueRepository accounts;
    @Autowired
    private ITaxRepository taxRepository;
    @Autowired
    private ZipBackupFormat format;
    @Autowired
    private TransactionTemplate tx;
    @Autowired
    private JdbcTemplate jdbc;

    @TempDir
    Path tmp;

    private record Seed(UUID source, Set<Long> accountIds, Set<Long> taxIds) {
    }

    private AccountCatalogueEntity account(String enterprise, String code, AccountCatalogueEntity parent) {
        return accounts.save(AccountCatalogueEntity.builder()
                .code(code).description("Cuenta " + code)
                .nature(NatureEnum.DEBIT).financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .parent(parent).crossing(false).costCenter(true).status(true)
                .idEnterprise(enterprise).amount(new BigDecimal("1500.5000")).usageCount(7).build());
    }

    private TaxEntity tax(String enterprise, String code, AccountCatalogueEntity sales, AccountCatalogueEntity purchase) {
        return taxRepository.save(TaxEntity.builder().code(code).description("IVA " + code).interest(19.0)
                .salesTax(sales).purchaseTax(purchase).idEnterprise(enterprise).status(true).usageCount(3).build());
    }

    /** Tenant A: 110505 -> 1105 -> 11 (el padre 11 se crea DESPUES, con id mayor que sus hijos) y dos impuestos. */
    private Seed seedTenantA() {
        UUID source = UUID.randomUUID();
        String ent = source.toString();
        return tx.execute(s -> {
            AccountCatalogueEntity mid = account(ent, "1105", null);
            AccountCatalogueEntity leaf = account(ent, "110505", mid);
            AccountCatalogueEntity root = account(ent, "11", null);
            mid.setParent(root);
            accounts.save(mid);
            AccountCatalogueEntity ivaVentas = account(ent, "2408", null);
            AccountCatalogueEntity ivaCompras = account(ent, "2409", null);
            TaxEntity iva = tax(ent, "IVA19", ivaVentas, ivaCompras);
            TaxEntity exento = tax(ent, "EXENTO", null, null);
            return new Seed(source,
                    Set.of(mid.getId(), leaf.getId(), root.getId(), ivaVentas.getId(), ivaCompras.getId()),
                    Set.of(iva.getId(), exento.getId()));
        });
    }

    private Path exportTenantA(UUID source) {
        Path target = tmp.resolve("puc-taxes.zip");
        ZipBackupWriter.Header header = new ZipBackupWriter.Header(source, "Empresa A", "test",
                ManifestV2.CreatedBy.of(TENANT_A, "u-a"), List.of(), LocalDateTime.now());
        try (ZipBackupWriter zip = format.newWriter(target, header)) {
            for (BackupParticipant p : List.of(puc, taxes)) {
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

    @Test
    void pucYImpuestosViajanConJerarquiaCuentasNuevasYSaldosEnCero() {
        TenantContext.setTenantId(TENANT_A);
        try {
            Seed seed = seedTenantA();
            UUID source = seed.source();
            Path zipPath = exportTenantA(source);

            TenantContext.setTenantId(TENANT_B);
            UUID target = UUID.randomUUID();
            String targetStr = target.toString();
            InMemoryIdMap idMap = new InMemoryIdMap();
            try (ZipBackupReader reader = format.open(zipPath)) {
                reader.verifyAll();
                assertThat(reader.manifest().module(BackupKeys.MODULE_PUC).orElseThrow()
                        .entry(PucBackupParticipant.ENTRY_ACCOUNTS).orElseThrow().count()).isEqualTo(5);
                for (BackupParticipant p : List.of(puc, taxes)) {
                    tx.executeWithoutResult(s -> p.restore(reader.module(p.moduleKey()), idMap, ctxB(source, target)));
                }
            }

            // conteos == origen e integridad sin huerfanos
            assertThat(puc.count(target)).isEqualTo(Map.of(PucBackupParticipant.ENTRY_ACCOUNTS, 5L));
            assertThat(taxes.count(target)).isEqualTo(Map.of(TaxesBackupParticipant.ENTRY_TAXES, 2L));
            assertThat(puc.verifyIntegrity(target)).isEmpty();
            assertThat(taxes.verifyIntegrity(target)).isEmpty();
            assertThat(idMap.size(BackupKeys.ACCOUNT)).isEqualTo(5);
            assertThat(idMap.size(BackupKeys.TAX)).isEqualTo(2);

            // ids nuevos: ninguno coincide con los del tenant A
            for (Long oldId : seed.accountIds()) {
                assertThat(idMap.<Long>require(BackupKeys.ACCOUNT, oldId)).isNotIn(seed.accountIds());
            }

            tx.executeWithoutResult(s -> {
                // jerarquia intacta
                AccountCatalogueEntity leaf = accounts.findByCode("110505", targetStr);
                assertThat(leaf.getParent().getCode()).isEqualTo("1105");
                assertThat(leaf.getParent().getParent().getCode()).isEqualTo("11");
                assertThat(leaf.getParent().getParent().getParent()).isNull();
                assertThat(leaf.getIdEnterprise()).isEqualTo(targetStr);
                assertThat(leaf.getTenantId()).isEqualTo(TENANT_B);

                // impuestos apuntando a las cuentas NUEVAS
                List<TaxEntity> restored = taxRepository.findBackupBatch(targetStr, 0L, PageRequest.of(0, 10));
                assertThat(restored).extracting(TaxEntity::getCode).containsExactly("IVA19", "EXENTO");
                TaxEntity iva = restored.get(0);
                assertThat(iva.getSalesTax().getCode()).isEqualTo("2408");
                assertThat(iva.getPurchaseTax().getCode()).isEqualTo("2409");
                assertThat(iva.getSalesTax().getIdEnterprise()).isEqualTo(targetStr);
                assertThat(iva.getSalesTax().getId()).isNotIn(seed.accountIds());
                assertThat(iva.getId()).isNotIn(seed.taxIds());
                assertThat(restored.get(1).getSalesTax()).isNull();
                assertThat(restored.get(1).getPurchaseTax()).isNull();
                assertThat(restored).allSatisfy(t -> assertThat(t.getUsageCount()).isZero());
            });

            // saldos y contadores en 0 (D5)
            assertThat(jdbc.queryForObject(
                    "SELECT COUNT(*) FROM account WHERE tenant_id = ? AND id_enterprise = ? AND (amount <> 0 OR usage_count <> 0)",
                    Long.class, TENANT_B, targetStr)).isZero();
            // tenant A intacto (el esquema H2 se comparte entre tests: se filtra por la empresa sembrada)
            assertThat(jdbc.queryForObject(
                    "SELECT COUNT(*) FROM account WHERE tenant_id = ? AND id_enterprise = ? AND amount = 1500.5 AND usage_count = 7",
                    Long.class, TENANT_A, source.toString())).isEqualTo(5L);
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void impuestoConCuentaFueraDeLaCopiaFallaPorIntegridad() {
        TenantContext.setTenantId(TENANT_A);
        try {
            Seed seed = seedTenantA();
            Path zipPath = exportTenantA(seed.source());

            TenantContext.setTenantId(TENANT_B);
            UUID target = UUID.randomUUID();
            try (ZipBackupReader reader = format.open(zipPath)) {
                // IdMap vacio: el PUC no se restauro, la FK a ACCOUNT no se puede remapear
                assertThatThrownBy(() -> tx.executeWithoutResult(s -> taxes.restore(
                        reader.module(BackupKeys.MODULE_TAXES), new InMemoryIdMap(), ctxB(seed.source(), target))))
                                .isInstanceOf(BackupIntegrityException.class);
            }
            assertThat(taxes.count(target)).isEqualTo(Map.of(TaxesBackupParticipant.ENTRY_TAXES, 0L));
        } finally {
            TenantContext.clear();
        }
    }
}
