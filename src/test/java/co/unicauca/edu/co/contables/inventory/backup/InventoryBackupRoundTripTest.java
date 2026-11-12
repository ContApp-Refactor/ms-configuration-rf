package co.unicauca.edu.co.contables.inventory.backup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verifyNoInteractions;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.InMemoryIdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import co.unicauca.edu.co.contables.commons.backup.RestoreMode;
import co.unicauca.edu.co.contables.commons.backup.zip.ManifestV2;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupFormat;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupReader;
import co.unicauca.edu.co.contables.commons.backup.zip.ZipBackupWriter;
import co.unicauca.edu.co.contables.commons.multitenancy.utils.TenantContext;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.StateEnum;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.backup.KardexBackupFormat;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.backup.KardexBackupParticipant;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.backup.PepsBackupFormat;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.backup.PepsBackupParticipant;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.backup.ProductsBackupFormat;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.backup.ProductsBackupParticipant;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.CategoryEntity;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.ProductEntity;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.ProductTypeEntity;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.UnitOfMeasureEntity;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository.ICategoryRepository;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository.IProductRepository;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository.IProductTypeRepository;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.repository.IUnitOfMeasureRepository;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.input.backup.StockBackupFormat;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.input.backup.StockBackupParticipant;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.entity.StockEntity;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.repository.ISyncStateRepository;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.repository.StockRepository;
import jakarta.persistence.EntityManager;

/**
 * Ida y vuelta de los participantes de PRODUCTOS y de las RÉPLICAS de inventario (stock, kardex de
 * promedio ponderado y PEPS) contra H2 con {@code @TenantId}: tenant A → ZIP v2 → tenant B.
 *
 * Verifica conteos, remapeo de FKs (categoría → cuentas/impuestos del IdMap; producto → categoría,
 * unidad, tipo; réplicas → producto), que las réplicas se crean SIEMPRE en ambos kardex (sea cual sea el
 * método de la empresa, origen o destino), que el restore NO publica nada a Rabbit (mock de {@link RabbitTemplate} sin interacciones)
 * y que el UNIQUE global de {@code kardex_products.reference} se detecta ANTES con un mensaje claro.
 */
@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
@org.springframework.test.context.jdbc.Sql(scripts = "/enterprise-test-schema.sql",
        executionPhase = org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class InventoryBackupRoundTripTest {

    private static final String TENANT_A = "tenant-A";
    private static final String TENANT_B = "tenant-B";

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;
    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ProductsBackupParticipant products;
    @Autowired
    private StockBackupParticipant stock;
    @Autowired
    private KardexBackupParticipant kardex;
    @Autowired
    private PepsBackupParticipant peps;

    @Autowired
    private IUnitOfMeasureRepository unitRepository;
    @Autowired
    private IProductTypeRepository typeRepository;
    @Autowired
    private ICategoryRepository categoryRepository;
    @Autowired
    private IProductRepository productRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private ISyncStateRepository syncStateRepository;
    @Autowired
    private co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.repository.IProductRepository kardexRepository;
    @Autowired
    private co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IProductRepository pepsRepository;

    @Autowired
    private ZipBackupFormat format;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private TransactionTemplate tx;
    @Autowired
    private JdbcTemplate jdbc;

    @TempDir
    Path tmp;

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    /* ------------------------------------------------------------------ */
    /* Casos                                                              */
    /* ------------------------------------------------------------------ */

    @Test
    void promedioPonderado_idaYVuelta_creaLasReplicasDeAmbosKardexSinPublicarEventos() {
        String suffix = suffix();
        UUID source = newEnterprise(TENANT_A, InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        seed(TENANT_A, source, suffix);
        Path zip = export(TENANT_A, source);
        List<Long> oldIds = productRepository.findByEnterpriseId(source.toString(), PageRequest.of(0, 10))
                .getContent().stream().map(ProductEntity::getId).toList();
        releaseSourceKardexReferences(source);

        // Qué viaja: las réplicas de AMBOS kardex, sea cual sea el método de la empresa.
        try (ZipBackupReader reader = format.open(zip)) {
            assertThat(entries(reader, BackupKeys.MODULE_INVENTORY_KARDEX)).containsEntry(KardexBackupFormat.PRODUCTS, 3L);
            assertThat(entries(reader, BackupKeys.MODULE_INVENTORY_PEPS)).containsEntry(PepsBackupFormat.PRODUCTS, 3L);
            assertThat(entries(reader, BackupKeys.MODULE_PRODUCTS))
                    .containsEntry(ProductsBackupFormat.UNITS_OF_MEASURE, 2L)
                    .containsEntry(ProductsBackupFormat.PRODUCT_TYPES, 1L)
                    .containsEntry(ProductsBackupFormat.CATEGORIES, 1L)
                    .containsEntry(ProductsBackupFormat.PRODUCTS, 3L);
            assertThat(entries(reader, BackupKeys.MODULE_INVENTORY_STOCK)).containsEntry(StockBackupFormat.STOCK, 3L);
        }

        UUID target = restore(zip, source, TENANT_B, InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);

        String ent = target.toString();
        TenantContext.setTenantId(TENANT_B);
        assertThat(unitRepository.countByEnterpriseId(ent)).isEqualTo(2);
        assertThat(typeRepository.countByEnterpriseId(ent)).isEqualTo(1);
        assertThat(stockRepository.countByEnterpriseId(ent)).isEqualTo(3);
        assertThat(kardexRepository.countByEnterpriseId(ent)).isEqualTo(3);
        assertThat(pepsRepository.countByEnterpriseId(ent)).isEqualTo(3);
        assertThat(syncStateRepository.findBySyncTypeAndEnterpriseId("products", ent)).isPresent();

        // FKs de la categoría: cuentas e impuestos remapeados con el IdMap (101..104 → 901..904, 201/202 → 701/702).
        List<CategoryEntity> categories = categoryRepository.findByEnterpriseId(ent, PageRequest.of(0, 10)).getContent();
        assertThat(categories).hasSize(1);
        CategoryEntity category = categories.get(0);
        assertThat(category.getInventoryId()).isEqualTo(901L);
        assertThat(category.getCostId()).isEqualTo(902L);
        assertThat(category.getSaleId()).isEqualTo(903L);
        assertThat(category.getReturnId()).isEqualTo(904L);
        assertThat(category.getTaxes()).containsExactlyInAnyOrder(701L, 702L);
        assertThat(category.getTenantId()).isEqualTo(TENANT_B);

        // FKs del producto: apuntan a las filas NUEVAS de la empresa destino; usageCount en 0.
        List<ProductEntity> newProducts = productRepository.findByEnterpriseId(ent, PageRequest.of(0, 10)).getContent();
        assertThat(newProducts).hasSize(3);
        assertThat(newProducts).extracting(ProductEntity::getId).doesNotContainAnyElementsOf(oldIds);
        assertThat(newProducts).allSatisfy(p -> {
            assertThat(p.getCategoryId()).isEqualTo(category.getId());
            assertThat(p.getUnitOfMeasureId()).isNotNull();
            assertThat(unitRepository.findById(p.getUnitOfMeasureId())).get()
                    .extracting(UnitOfMeasureEntity::getEnterpriseId).isEqualTo(ent);
            assertThat(typeRepository.findById(p.getProductTypeId())).get()
                    .extracting(ProductTypeEntity::getEnterpriseId).isEqualTo(ent);
            assertThat(p.getUsageCount()).isZero();
            assertThat(p.getEnterpriseId()).isEqualTo(ent);
        });
        assertThat(newProducts).extracting(ProductEntity::getQuantity).containsExactlyInAnyOrder(10, 20, 30);
        assertThat(newProducts).extracting(ProductEntity::getCost).containsExactlyInAnyOrder(1500.5, 1500.5, 1500.5);

        // Réplicas: apuntan a los productos NUEVOS.
        List<Long> newIds = newProducts.stream().map(ProductEntity::getId).toList();
        assertThat(stockRepository.findAllByEnterpriseId(ent)).extracting(StockEntity::getProductId)
                .containsExactlyInAnyOrderElementsOf(newIds);
        assertThat(kardexRepository.findAllByEnterpriseId(ent)).allSatisfy(k -> {
            assertThat(newIds).contains(k.getProductId());
            assertThat(k.getTenantId()).isEqualTo(TENANT_B);
        });

        // El restore escribió por repositorios: nada se publicó a Rabbit.
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void peps_idaYVuelta_creaLasReplicasDeAmbosKardex() {
        String suffix = suffix();
        UUID source = newEnterprise(TENANT_A, InventoryConfigurationTypeEnum.PEPS);
        seed(TENANT_A, source, suffix);
        Path zip = export(TENANT_A, source);
        releaseSourceKardexReferences(source);

        try (ZipBackupReader reader = format.open(zip)) {
            assertThat(entries(reader, BackupKeys.MODULE_INVENTORY_PEPS)).containsEntry(PepsBackupFormat.PRODUCTS, 3L);
            assertThat(entries(reader, BackupKeys.MODULE_INVENTORY_KARDEX)).containsEntry(KardexBackupFormat.PRODUCTS, 3L);
        }

        UUID target = restore(zip, source, TENANT_B, InventoryConfigurationTypeEnum.PEPS);

        TenantContext.setTenantId(TENANT_B);
        assertThat(pepsRepository.countByEnterpriseId(target.toString())).isEqualTo(3);
        assertThat(kardexRepository.countByEnterpriseId(target.toString())).isEqualTo(3);
        assertThat(stockRepository.countByEnterpriseId(target.toString())).isEqualTo(3);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void siElMetodoDestinoNoCoincideConLaCopia_ambasReplicasSeRestauranIgual() {
        String suffix = suffix();
        UUID source = newEnterprise(TENANT_A, InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        seed(TENANT_A, source, suffix);
        Path zip = export(TENANT_A, source);
        releaseSourceKardexReferences(source);

        UUID target = restore(zip, source, TENANT_B, InventoryConfigurationTypeEnum.PEPS);

        TenantContext.setTenantId(TENANT_B);
        assertThat(kardexRepository.countByEnterpriseId(target.toString())).isEqualTo(3);
        assertThat(pepsRepository.countByEnterpriseId(target.toString())).isEqualTo(3);
        assertThat(stockRepository.countByEnterpriseId(target.toString())).isEqualTo(3);
    }

    @Test
    void referenciaDeKardexRepetida_yaNoEsConflictoPorqueLaUnicidadEsPorEmpresa() {
        String suffix = suffix();
        UUID source = newEnterprise(TENANT_A, InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
        seed(TENANT_A, source, suffix);
        Path zip = export(TENANT_A, source);

        // Convertido (antes: "hoy kardex_products.reference es UNIQUE global" y el preflight lo rechazaba). El
        // commit 6fc1512 cambió la unicidad a (enterprise_id, reference): ya no hay conflicto al duplicar.
        assertThat(kardex.referenceIsGloballyUnique()).as("kardex_products.reference ya NO es UNIQUE global").isFalse();

        // Duplicar dentro del MISMO tenant: las referencias de la copia ya existen, pero en OTRA empresa.
        TenantContext.setTenantId(TENANT_A);
        RestoreContext initial = initialContext(TENANT_A, source);
        try (ZipBackupReader reader = format.open(zip)) {
            reader.verifyAll();
            assertThatCode(() -> kardex.preflight(reader.module(BackupKeys.MODULE_INVENTORY_KARDEX), initial))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void laComprobacionDeReferenciasSeApagaSolaCuandoLaRestriccionEsPorEmpresa() throws Exception {
        assertThat(referenceIsGloballyUnique("UNIQUE (reference)")).isTrue();
        assertThat(referenceIsGloballyUnique("UNIQUE (enterprise_id, reference)")).isFalse();
    }

    private static boolean referenceIsGloballyUnique(String constraint) throws Exception {
        org.h2.jdbcx.JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
        ds.setURL("jdbc:h2:mem:meta" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        try (var c = ds.getConnection(); var st = c.createStatement()) {
            st.execute("CREATE TABLE kardex_products (id BIGINT PRIMARY KEY, enterprise_id VARCHAR(50), "
                    + "reference VARCHAR(50), " + constraint + ")");
        }
        return new KardexBackupParticipant(null, null, ds).referenceIsGloballyUnique();
    }

    /* ------------------------------------------------------------------ */
    /* Utilidades                                                         */
    /* ------------------------------------------------------------------ */

    /**
     * Mientras {@code kardex_products.reference} sea UNIQUE global, la empresa de origen impide restaurar
     * la misma referencia en otra empresa (es justo lo que detecta el preflight). Para probar el resto de
     * la ida y vuelta se "libera" la referencia del origen, como si el origen ya no existiera.
     */
    private void releaseSourceKardexReferences(UUID source) {
        jdbc.update("UPDATE kardex_products SET reference = reference || '-src' WHERE enterprise_id = ?",
                source.toString());
    }

    private static String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private List<BackupParticipant> participants() {
        return List.of(products, stock, kardex, peps);
    }

    private UUID newEnterprise(String tenant, InventoryConfigurationTypeEnum method) {
        TenantContext.setTenantId(tenant);
        return tx.execute(s -> {
            EnterpriseEntity e = EnterpriseEntity.builder().idUser("u").name("Emp " + method).nit("900")
                    .state(StateEnum.ACTIVE).inventoryConfigurationType(method).build();
            entityManager.persist(e);
            entityManager.flush();
            return e.getId();
        });
    }

    /** 2 unidades, 1 tipo, 1 categoría (con cuentas/impuestos), 3 productos y sus 3 réplicas en stock, kardex y PEPS. */
    private void seed(String tenant, UUID enterprise, String suffix) {
        TenantContext.setTenantId(tenant);
        String ent = enterprise.toString();
        tx.executeWithoutResult(s -> {
            UnitOfMeasureEntity kg = unitRepository.save(unit(ent, "Kilogramo " + suffix, "kg"));
            UnitOfMeasureEntity lt = unitRepository.save(unit(ent, "Litro " + suffix, "lt"));
            ProductTypeEntity type = typeRepository
                    .save(ProductTypeEntity.builder().name("Bien " + suffix).description("d").enterpriseId(ent).state(true).build());
            CategoryEntity category = new CategoryEntity();
            category.setName("Lacteos " + suffix);
            category.setDescription("c");
            category.setEnterpriseId(ent);
            category.setInventoryId(101L);
            category.setCostId(102L);
            category.setSaleId(103L);
            category.setReturnId(104L);
            category.setTaxes(new ArrayList<>(List.of(201L, 202L)));
            category.setState(true);
            category = categoryRepository.save(category);

            for (int i = 1; i <= 3; i++) {
                ProductEntity p = new ProductEntity();
                p.setCode("C" + i);
                p.setName("Producto " + i + " " + suffix);
                p.setDescription("desc");
                p.setQuantity(i * 10);
                p.setUnitOfMeasureId((i % 2 == 0 ? lt : kg).getId());
                p.setCategoryId(category.getId());
                p.setProductTypeId(type.getId());
                p.setEnterpriseId(ent);
                p.setCost(1500.5);
                p.setState(true);
                p.setReference("REF-" + suffix + "-" + i);
                p.setPresentation("caja");
                p.setUsageCount(7);
                p = productRepository.save(p);

                StockEntity st = new StockEntity();
                st.setProductId(p.getId());
                st.setName(p.getName());
                st.setEnterpriseId(ent);
                st.setQuantity(i * 10);
                st.setPrice(new java.math.BigDecimal("1500.50"));
                st.setState(true);
                stockRepository.save(st);

                // Como hoy: los listeners crean la réplica en AMBOS kardex, sin mirar el método.
                var k = new co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity();
                k.setProductId(p.getId());
                k.setReference(p.getReference());
                k.setName(p.getName());
                k.setPresentation("caja");
                k.setEnterpriseId(ent);
                k.setState(true);
                kardexRepository.save(k);

                var pp = new co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.ProductEntity();
                pp.setProductId(p.getId());
                pp.setReference(p.getReference());
                pp.setName(p.getName());
                pp.setPresentation("caja");
                pp.setEnterpriseId(ent);
                pp.setState(true);
                pepsRepository.save(pp);
            }
        });
    }

    private static UnitOfMeasureEntity unit(String ent, String name, String abbreviation) {
        UnitOfMeasureEntity u = new UnitOfMeasureEntity();
        u.setName(name);
        u.setDescription("u");
        u.setAbbreviation(abbreviation);
        u.setEnterpriseId(ent);
        u.setState(true);
        return u;
    }

    private Path export(String tenant, UUID enterprise) {
        TenantContext.setTenantId(tenant);
        Path file = tmp.resolve("backup-" + UUID.randomUUID() + ".zip");
        ZipBackupWriter.Header header = new ZipBackupWriter.Header(enterprise, "Emp", "test",
                ManifestV2.CreatedBy.of(tenant, "user"), BackupKeys.EXCLUDED, LocalDateTime.now());
        tx.executeWithoutResult(s -> {
            try (ZipBackupWriter zip = format.newWriter(file, header)) {
                for (BackupParticipant p : participants()) {
                    p.export(enterprise, zip.module(p.moduleKey(), p.schemaVersion()));
                }
                zip.finish();
            }
        });
        return file;
    }

    private static RestoreContext initialContext(String tenant, UUID source) {
        return new RestoreContext(tenant, "user-b", source, null, 2, LocalDateTime.now(),
                RestoreMode.NEW_ENTERPRISE, null, null, false);
    }

    /**
     * Restore como lo haría el coordinador: preflight sin transacción; luego, en UNA transacción, la
     * empresa destino (la crea el test, que hace de participante enterprise), cada participante en
     * orden con flush, y la verificación (huérfanos + conteos contra el manifest).
     */
    private UUID restore(Path zip, UUID source, String tenant, InventoryConfigurationTypeEnum targetMethod) {
        TenantContext.setTenantId(tenant);
        RestoreContext initial = initialContext(tenant, source);
        try (ZipBackupReader reader = format.open(zip)) {
            reader.verifyAll();
            for (BackupParticipant p : participants()) {
                p.preflight(reader.module(p.moduleKey()), initial);
            }
            return tx.execute(s -> {
                EnterpriseEntity e = EnterpriseEntity.builder().idUser("user-b").name("Copia").nit("901")
                        .state(StateEnum.ACTIVE).inventoryConfigurationType(targetMethod).build();
                entityManager.persist(e);
                entityManager.flush();
                UUID target = e.getId();
                RestoreContext ctx = initial.withTargetEnterpriseId(target);

                IdMap idMap = new InMemoryIdMap();
                idMap.put(BackupKeys.ENTERPRISE, source, target);
                // Namespaces que registran los participantes puc/taxes (aquí simulados).
                for (long i = 1; i <= 4; i++) {
                    idMap.put(BackupKeys.ACCOUNT, 100L + i, 900L + i);
                }
                idMap.put(BackupKeys.TAX, 201L, 701L);
                idMap.put(BackupKeys.TAX, 202L, 702L);

                for (BackupParticipant p : participants()) {
                    p.restore(reader.module(p.moduleKey()), idMap, ctx);
                    entityManager.flush();
                }
                for (BackupParticipant p : participants()) {
                    assertThat(p.verifyIntegrity(target)).as("huérfanos en " + p.moduleKey()).isEmpty();
                    Map<String, Long> actual = p.count(target);
                    for (ManifestV2.EntryInfo entry : reader.manifest().module(p.moduleKey()).orElseThrow().entries()) {
                        if (actual.get(entry.path()) != null) {
                            assertThat(actual.get(entry.path())).as(entry.path()).isEqualTo(entry.count());
                        }
                    }
                }
                return target;
            });
        }
    }

    private static Map<String, Long> entries(ZipBackupReader reader, String moduleKey) {
        Map<String, Long> result = new java.util.LinkedHashMap<>();
        reader.manifest().module(moduleKey).orElseThrow().entries()
                .forEach(e -> result.put(e.path(), e.count()));
        return result;
    }
}
