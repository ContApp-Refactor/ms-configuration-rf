package co.unicauca.edu.co.contables.configuration.enterprise;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionTemplate;

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
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataInitializer;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.backup.KardexBackupFormat;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.backup.KardexBackupParticipant;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.backup.KardexMovementsBackupParticipant;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.backup.PepsBackupFormat;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.backup.PepsBackupParticipant;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.backup.PepsMovementsBackupParticipant;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.DetailOutputEntity;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.entity.StockEntity;
import jakarta.persistence.EntityManager;

/**
 * Ida y vuelta de los movimientos de inventario (kardex promedio ponderado, capas PEPS y detalle de salidas):
 * datos en el tenant A, export con {@link ZipBackupFormat} y restore como tenant B con {@link InMemoryIdMap}.
 * Las réplicas de producto ({@code kardex_products}, {@code peps_product}, {@code stock_entity}) las restaura otro
 * participante: aquí se siembran a mano en B con los ids nuevos, simulando ese paso.
 */
@SpringBootTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
@ActiveProfiles("test")
@Import(TestMultiTenancyConfig.class)
@Sql(scripts = "/enterprise-test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class InventoryMovementsBackupRoundTripTest {

    private static final String TENANT_A = "tenant-A";
    private static final String TENANT_B = "tenant-B";

    private static final java.util.concurrent.atomic.AtomicLong RUN = new java.util.concurrent.atomic.AtomicLong();
    private final long base = RUN.incrementAndGet() * 1000L;
    private final long P1 = base + 1;
    private final long B1 = base + 201;
    private final long B2 = base + 202;
    private final long B3 = base + 203; // promedio: 3 movimientos, saldo 12
    private final long P2 = base + 2; // promedio: 1 movimiento, saldo 7
    private final long P3 = base + 3; // PEPS: 3 capas (disponible 3 + 5 + 0 = 8)

    @MockitoBean
    private GeographyDataInitializer geographyDataInitializer;
    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private KardexMovementsBackupParticipant kardex;
    @Autowired
    private PepsMovementsBackupParticipant peps;
    @Autowired
    private KardexBackupParticipant kardexReplicas;
    @Autowired
    private PepsBackupParticipant pepsReplicas;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private ZipBackupFormat format;
    @Autowired
    private TransactionTemplate tx;
    @Autowired
    private EntityManager em;

    @TempDir
    Path tmp;

    private record Seed(UUID enterprise, Set<Long> pepsKardexIds) {
    }

    /* ----------------------------- siembra ----------------------------- */

    private UUID persistEnterprise(String name, InventoryConfigurationTypeEnum method) {
        EnterpriseEntity e = EnterpriseEntity.builder().name(name).inventoryConfigurationType(method).build();
        em.persist(e);
        return e.getId();
    }

    private void kardexReplica(String ent, long productId, String reference) {
        var p = new co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity();
        p.setProductId(productId);
        p.setReference(reference);
        p.setName("Producto " + productId);
        p.setPresentation("Unidad");
        p.setEnterpriseId(ent);
        p.setState(true);
        em.persist(p);
    }

    private void pepsReplica(String ent, long productId, String reference) {
        var p = new co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.ProductEntity();
        p.setProductId(productId);
        p.setReference(reference);
        p.setName("Producto " + productId);
        p.setPresentation("Unidad");
        p.setEnterpriseId(ent);
        p.setState(true);
        em.persist(p);
    }

    /** Fila del módulo de productos (tabla {@code product}); devuelve su id generado. */
    private long inventoryProduct(String ent, String reference, String name) {
        var p = new co.unicauca.edu.co.contables.inventory.products.infraestructure.output.persistence.entity.ProductEntity();
        p.setCode("C-" + reference);
        p.setName(name);
        p.setReference(reference);
        p.setPresentation("Unidad");
        p.setEnterpriseId(ent);
        p.setState(true);
        p.setQuantity(0);
        p.setUsageCount(0);
        em.persist(p);
        em.flush();
        return p.getId();
    }

    private void stock(String ent, long productId, int quantity) {
        StockEntity s = new StockEntity();
        s.setProductId(productId);
        s.setName("Producto " + productId);
        s.setEnterpriseId(ent);
        s.setQuantity(quantity);
        s.setPrice(new BigDecimal("6.00"));
        s.setState(true);
        em.persist(s);
    }

    private void avgMove(long productId, co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType type,
            int quantity, String unit, int balQty, String balUnit, String total, String date) {
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
            long productId, String ent,
            co.unicauca.edu.co.contables.inventory.peps.domain.enums.MovementType type, int quantity, String unit,
            int available, String date) {
        var product = em.createQuery(
                "select p from PepsProductEntity p where p.productId = :id and p.enterpriseId = :e",
                co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.ProductEntity.class)
                .setParameter("id", productId).setParameter("e", ent).getSingleResult();
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

    /** Tenant A (promedio ponderado): P1 y P2 con kardex promedio, P3 con capas PEPS; stock consistente. */
    private Seed seedTenantA() {
        return seedTenantA(InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE);
    }

    /** Igual, pero con el método de inventario indicado (los movimientos quedan en ambos kardex sea cual sea). */
    private Seed seedTenantA(InventoryConfigurationTypeEnum method) {
        return tx.execute(s -> {
            UUID ent = persistEnterprise("Empresa A", method);
            String e = ent.toString();
            kardexReplica(e, P1, "REF-A-1-" + base);
            kardexReplica(e, P2, "REF-A-2-" + base);
            pepsReplica(e, P3, "REF-A-3-" + base);
            stock(e, P1, 12);
            stock(e, P2, 7);
            stock(e, P3, 8);

            var buy = co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType.PURCHASE;
            var sell = co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType.SALE;
            avgMove(P1, buy, 10, "5.00", 10, "5.00", "50.00", "2026-01-01");
            avgMove(P1, sell, 4, "5.00", 6, "5.00", "30.00", "2026-01-02");
            avgMove(P1, buy, 6, "7.00", 12, "6.00", "72.00", "2026-01-03");
            avgMove(P2, buy, 7, "3.00", 7, "3.00", "21.00", "2026-01-01");

            var purchase = co.unicauca.edu.co.contables.inventory.peps.domain.enums.MovementType.PURCHASE;
            var sale = co.unicauca.edu.co.contables.inventory.peps.domain.enums.MovementType.SALE;
            var k1 = pepsMove(P3, e, purchase, 10, "4.00", 3, "2026-01-01");
            var k2 = pepsMove(P3, e, purchase, 5, "6.00", 5, "2026-01-02");
            var k3 = pepsMove(P3, e, sale, 7, "4.00", 0, "2026-01-03");
            DetailOutputEntity d = new DetailOutputEntity();
            d.setQuantityUsed(7);
            d.setUnitPrice(new BigDecimal("4.00"));
            d.setMovementSale(k3);
            d.setMovementOrigin(k1);
            em.persist(d);
            em.flush();
            return new Seed(ent, Set.of(k1.getIdKardex(), k2.getIdKardex(), k3.getIdKardex()));
        });
    }

    /** Lo que haría el restore de réplicas: filas del tenant B con los ids NUEVOS de producto. */
    private InMemoryIdMap seedReplicasTenantB(String ent) {
        InMemoryIdMap idMap = new InMemoryIdMap();
        tx.executeWithoutResult(s -> {
            kardexReplica(ent, B1, "REF-B-1-" + base);
            kardexReplica(ent, B2, "REF-B-2-" + base);
            pepsReplica(ent, B3, "REF-B-3-" + base);
            stock(ent, B1, 12);
            stock(ent, B2, 7);
            stock(ent, B3, 8);
        });
        idMap.put(BackupKeys.PRODUCT, P1, B1);
        idMap.put(BackupKeys.PRODUCT, P2, B2);
        idMap.put(BackupKeys.PRODUCT, P3, B3);
        return idMap;
    }

    private Path exportTenantA(UUID source) {
        return exportTenantA(source, List.of(kardex, peps));
    }

    private Path exportTenantA(UUID source, List<BackupParticipant> exporters) {
        Path target = tmp.resolve("inventory-movements-" + UUID.randomUUID() + ".zip");
        ZipBackupWriter.Header header = new ZipBackupWriter.Header(source, "Empresa A", "test",
                ManifestV2.CreatedBy.of(TENANT_A, "u-a"), List.of(), LocalDateTime.now());
        try (ZipBackupWriter zip = format.newWriter(target, header)) {
            for (BackupParticipant p : exporters) {
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

    /* ------------------------ instantáneas comparables ------------------------ */

    private List<String> avgSnapshot(long productId) {
        return tx.execute(s -> em.createQuery(
                "select k from WeightedAverageKardexEntity k where k.productId = :p order by k.id",
                co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity.class)
                .setParameter("p", productId).getResultList().stream()
                .map(k -> String.join("|", k.getFactCode(), k.getType().name(), String.valueOf(k.getQuantity()),
                        k.getUnitPrice().toPlainString(), k.getDetails(), String.valueOf(k.getBalanceQuantity()),
                        k.getBalanceUnitPrice().toPlainString(), k.getTotalBalance().toPlainString(),
                        k.getDate().toInstant().toString()))
                .toList());
    }

    private List<String> pepsSnapshot(long productId) {
        return tx.execute(s -> em.createQuery(
                "select k.factCode, k.type, k.quantity, k.unitPrice, k.availableQuantity, k.date, k.details "
                        + "from KardexEntity k where k.product.productId = :p order by k.idKardex",
                Object[].class)
                .setParameter("p", productId).getResultList().stream()
                .map(c -> c[0] + "|" + c[1] + "|" + c[2] + "|" + ((BigDecimal) c[3]).toPlainString() + "|" + c[4]
                        + "|" + ((ZonedDateTime) c[5]).toInstant() + "|" + c[6])
                .toList());
    }

    private long countAll(String entity) {
        return tx.execute(s -> em.createQuery("select count(x) from " + entity + " x", Long.class).getSingleResult());
    }

    /* -------------------------------- tests -------------------------------- */

    @Test
    void movimientosViajanConSaldosCapasYDetalleIntactosYFksNuevas() {
        TenantContext.setTenantId(TENANT_A);
        try {
            Seed seed = seedTenantA();
            UUID source = seed.enterprise();
            List<String> avg1A = avgSnapshot(P1);
            List<String> avg2A = avgSnapshot(P2);
            List<String> pepsA = pepsSnapshot(P3);
            long kardexTenantA = countAll("WeightedAverageKardexEntity");
            assertThat(kardex.verifyIntegrity(source)).isEmpty();
            assertThat(peps.verifyIntegrity(source)).isEmpty();
            Path zipPath = exportTenantA(source);

            TenantContext.setTenantId(TENANT_B);
            UUID target = tx.execute(s -> persistEnterprise("Empresa B", InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE));
            InMemoryIdMap idMap = seedReplicasTenantB(target.toString());
            try (ZipBackupReader reader = format.open(zipPath)) {
                reader.verifyAll();
                assertThat(reader.manifest().module(BackupKeys.MODULE_INVENTORY_KARDEX_MOVEMENTS).orElseThrow()
                        .entry(KardexMovementsBackupParticipant.ENTRY_KARDEX).orElseThrow().count()).isEqualTo(4);
                var pepsModule = reader.manifest().module(BackupKeys.MODULE_INVENTORY_PEPS_MOVEMENTS).orElseThrow();
                assertThat(pepsModule.entry(PepsMovementsBackupParticipant.ENTRY_KARDEX).orElseThrow().count())
                        .isEqualTo(3);
                assertThat(pepsModule.entry(PepsMovementsBackupParticipant.ENTRY_DETAILS).orElseThrow().count())
                        .isEqualTo(1);
                for (BackupParticipant p : List.of(kardex, peps)) {
                    tx.executeWithoutResult(s -> p.restore(reader.module(p.moduleKey()), idMap, ctxB(source, target)));
                }
            }

            // conteos == origen e integridad (FKs + stock == saldo/capas) sin problemas
            assertThat(kardex.count(target)).isEqualTo(Map.of(KardexMovementsBackupParticipant.ENTRY_KARDEX, 4L));
            assertThat(peps.count(target)).isEqualTo(Map.of(
                    PepsMovementsBackupParticipant.ENTRY_KARDEX, 3L,
                    PepsMovementsBackupParticipant.ENTRY_DETAILS, 1L));
            assertThat(kardex.verifyIntegrity(target)).isEmpty();
            assertThat(peps.verifyIntegrity(target)).isEmpty();
            assertThat(idMap.size(BackupKeys.PEPS_KARDEX)).isEqualTo(3);

            // contenido idéntico (saldos, costos, fechas) con los ids NUEVOS de producto
            assertThat(avgSnapshot(B1)).hasSize(3).isEqualTo(avg1A);
            assertThat(avgSnapshot(B2)).hasSize(1).isEqualTo(avg2A);
            assertThat(pepsSnapshot(B3)).isEqualTo(pepsA);

            // capas PEPS con ids nuevos y detalle apuntando a las capas NUEVAS
            for (Long oldId : seed.pepsKardexIds()) {
                assertThat(idMap.<Long>require(BackupKeys.PEPS_KARDEX, oldId)).isNotIn(seed.pepsKardexIds());
            }
            tx.executeWithoutResult(s -> {
                List<Object[]> detail = em.createQuery("""
                        select s.type, s.quantity, o.type, o.quantity, o.availableQuantity, d.quantityUsed,
                               d.unitPrice, sp.productId, op.productId, sp.enterpriseId
                        from DetailOutputEntity d join d.movementSale s join s.product sp
                          join d.movementOrigin o join o.product op
                        where sp.enterpriseId = :e
                        """, Object[].class).setParameter("e", target.toString()).getResultList();
                assertThat(detail).hasSize(1);
                Object[] c = detail.get(0);
                assertThat(c[0]).hasToString("SALE");
                assertThat(c[1]).isEqualTo(7);
                assertThat(c[2]).hasToString("PURCHASE");
                assertThat(c[3]).isEqualTo(10);
                assertThat(c[4]).isEqualTo(3);
                assertThat(c[5]).isEqualTo(7);
                assertThat((BigDecimal) c[6]).isEqualByComparingTo("4.00");
                assertThat(c[7]).isEqualTo(B3);
                assertThat(c[8]).isEqualTo(B3);
            });

            // tenant A intacto
            TenantContext.setTenantId(TENANT_A);
            assertThat(countAll("WeightedAverageKardexEntity")).isEqualTo(kardexTenantA);
            assertThat(avgSnapshot(P1)).isEqualTo(avg1A);
            assertThat(pepsSnapshot(P3)).isEqualTo(pepsA);

            // el restore no publicó nada a Rabbit
            verifyNoInteractions(rabbitTemplate);
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void elStockDescuadradoSegunElMetodoDeLaEmpresaEsAdvertenciaYNoBloquea() {
        // Diseño §6.8: el descuadre stock-vs-movimientos NO es un problema de integridad (no aborta el
        // restore): solo se registra un warning. Antes este test esperaba el problema en verifyIntegrity.
        ListAppender<ILoggingEvent> logs = new ListAppender<>();
        logs.start();
        Logger kardexLog = (Logger) LoggerFactory.getLogger(co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.backup.KardexMovementsBackupParticipant.class);
        Logger pepsLog = (Logger) LoggerFactory.getLogger(co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.backup.PepsMovementsBackupParticipant.class);
        kardexLog.addAppender(logs);
        pepsLog.addAppender(logs);
        TenantContext.setTenantId(TENANT_A);
        try {
            Seed seed = seedTenantA();
            Path zipPath = exportTenantA(seed.enterprise());

            TenantContext.setTenantId(TENANT_B);
            UUID target = tx.execute(s -> persistEnterprise("Empresa B", InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE));
            InMemoryIdMap idMap = seedReplicasTenantB(target.toString());
            try (ZipBackupReader reader = format.open(zipPath)) {
                for (BackupParticipant p : List.of(kardex, peps)) {
                    tx.executeWithoutResult(s -> p.restore(reader.module(p.moduleKey()),
                            idMap, ctxB(seed.enterprise(), target)));
                }
            }
            assertThat(kardex.verifyIntegrity(target)).isEmpty();

            // promedio ponderado: stock de P1 != saldo del ultimo movimiento -> solo warning; PEPS no se evalua
            setStock(B3, 99);
            setStock(B1, 13);
            assertThat(kardex.verifyIntegrity(target)).isEmpty();
            assertThat(warnings(logs)).singleElement().asString()
                    .contains(String.valueOf(B1)).contains("13").contains("12");
            logs.list.clear();
            assertThat(peps.verifyIntegrity(target)).isEmpty();
            assertThat(warnings(logs)).isEmpty();

            // la empresa pasa a PEPS: ahora manda la suma de capas disponibles (tambien solo warning)
            tx.executeWithoutResult(s -> em.createQuery(
                    "update EnterpriseEntity e set e.inventoryConfigurationType = :m where e.id = :id")
                    .setParameter("m", InventoryConfigurationTypeEnum.PEPS).setParameter("id", target)
                    .executeUpdate());
            assertThat(kardex.verifyIntegrity(target)).isEmpty();
            assertThat(warnings(logs)).isEmpty();
            assertThat(peps.verifyIntegrity(target)).isEmpty();
            assertThat(warnings(logs)).singleElement().asString()
                    .contains(String.valueOf(B3)).contains("99").contains("8");
        } finally {
            kardexLog.detachAppender(logs);
            pepsLog.detachAppender(logs);
            TenantContext.clear();
        }
    }

    private static List<String> warnings(ListAppender<ILoggingEvent> logs) {
        return logs.list.stream().filter(e -> e.getLevel() == Level.WARN)
                .map(ILoggingEvent::getFormattedMessage).toList();
    }

    private void setStock(long productId, int quantity) {
        tx.executeWithoutResult(s -> em.createQuery(
                "update StockEntity s set s.quantity = :q where s.productId = :p")
                .setParameter("q", quantity).setParameter("p", productId).executeUpdate());
    }

    @Test
    void productoFueraDeLaCopiaOSinReplicaFallaPorIntegridadYNoDejaFilas() {
        TenantContext.setTenantId(TENANT_A);
        try {
            Seed seed = seedTenantA();
            UUID source = seed.enterprise();
            Path zipPath = exportTenantA(source);

            TenantContext.setTenantId(TENANT_B);
            UUID target = tx.execute(s -> persistEnterprise("Empresa B", InventoryConfigurationTypeEnum.PEPS));
            try (ZipBackupReader reader = format.open(zipPath)) {
                // IdMap vacio: los productos no se restauraron, la FK PRODUCT no se puede remapear
                assertThatThrownBy(() -> tx.executeWithoutResult(s -> kardex.restore(
                        reader.module(kardex.moduleKey()), new InMemoryIdMap(), ctxB(source, target))))
                                .isInstanceOf(BackupIntegrityException.class);

                // IdMap con productos pero SIN las réplicas en la empresa destino
                // Productos reales del destino (sin réplica): el error debe nombrar su referencia y nombre.
                long bp1 = tx.execute(s -> inventoryProduct(target.toString(), "REF-FALTA-K-" + base, "Leche faltante"));
                long bp3 = tx.execute(s -> inventoryProduct(target.toString(), "REF-FALTA-P-" + base, "Queso faltante"));
                InMemoryIdMap idMap = new InMemoryIdMap();
                idMap.put(BackupKeys.PRODUCT, P1, bp1);
                idMap.put(BackupKeys.PRODUCT, P2, B2);
                idMap.put(BackupKeys.PRODUCT, P3, bp3);
                assertThatThrownBy(() -> tx.executeWithoutResult(s -> kardex.restore(
                        reader.module(kardex.moduleKey()), idMap, ctxB(source, target))))
                                .isInstanceOf(BackupIntegrityException.class)
                                .hasMessageContaining("sin réplica")
                                .hasMessageContaining("REF-FALTA-K-" + base)
                                .hasMessageContaining("Leche faltante");
                assertThatThrownBy(() -> tx.executeWithoutResult(s -> peps.restore(
                        reader.module(peps.moduleKey()), idMap, ctxB(source, target))))
                                .isInstanceOf(BackupIntegrityException.class)
                                .hasMessageContaining("sin réplica")
                                .hasMessageContaining("REF-FALTA-P-" + base)
                                .hasMessageContaining("Queso faltante");
            }
            assertThat(kardex.count(target)).isEqualTo(Map.of(KardexMovementsBackupParticipant.ENTRY_KARDEX, 0L));
            assertThat(peps.count(target).values()).containsOnly(0L);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * El bug real: empresa con método PEPS pero con movimientos en el kardex de PROMEDIO. Las réplicas viajan en
     * ambos kardex sea cual sea el método (aquí kardex_products tiene 2 productos y peps_product 1: no tienen por qué
     * coincidir) y los movimientos de los dos kardex se restauran con su réplica.
     */
    @Test
    void empresaPepsConMovimientosEnElKardexDePromedio_restauraConLasReplicasDeAmbosKardex() {
        TenantContext.setTenantId(TENANT_A);
        try {
            Seed seed = seedTenantA(InventoryConfigurationTypeEnum.PEPS);
            UUID source = seed.enterprise();
            List<String> avg1A = avgSnapshot(P1);
            List<String> pepsA = pepsSnapshot(P3);
            Path zipPath = exportTenantA(source, List.of(kardexReplicas, pepsReplicas, kardex, peps));
            // UNIQUE global de kardex_products.reference: se "libera" la del origen (ver InventoryBackupRoundTripTest).
            jdbc.update("UPDATE kardex_products SET reference = reference || '-src' WHERE enterprise_id = ?",
                    source.toString());

            TenantContext.setTenantId(TENANT_B);
            UUID target = tx.execute(s -> persistEnterprise("Empresa B", InventoryConfigurationTypeEnum.PEPS));
            InMemoryIdMap idMap = new InMemoryIdMap();
            idMap.put(BackupKeys.PRODUCT, P1, B1);
            idMap.put(BackupKeys.PRODUCT, P2, B2);
            idMap.put(BackupKeys.PRODUCT, P3, B3);
            try (ZipBackupReader reader = format.open(zipPath)) {
                reader.verifyAll();
                assertThat(reader.manifest().module(BackupKeys.MODULE_INVENTORY_KARDEX).orElseThrow()
                        .entry(KardexBackupFormat.PRODUCTS).orElseThrow().count()).isEqualTo(2);
                assertThat(reader.manifest().module(BackupKeys.MODULE_INVENTORY_PEPS).orElseThrow()
                        .entry(PepsBackupFormat.PRODUCTS).orElseThrow().count()).isEqualTo(1);
                kardexReplicas.preflight(reader.module(kardexReplicas.moduleKey()), ctxB(source, target));
                for (BackupParticipant p : List.of(kardexReplicas, pepsReplicas, kardex, peps)) {
                    tx.executeWithoutResult(s -> p.restore(reader.module(p.moduleKey()), idMap, ctxB(source, target)));
                }
            }

            assertThat(kardexReplicas.count(target)).isEqualTo(Map.of(KardexBackupFormat.PRODUCTS, 2L));
            assertThat(pepsReplicas.count(target)).isEqualTo(Map.of(PepsBackupFormat.PRODUCTS, 1L));
            assertThat(kardex.count(target)).isEqualTo(Map.of(KardexMovementsBackupParticipant.ENTRY_KARDEX, 4L));
            assertThat(peps.count(target)).isEqualTo(Map.of(
                    PepsMovementsBackupParticipant.ENTRY_KARDEX, 3L,
                    PepsMovementsBackupParticipant.ENTRY_DETAILS, 1L));
            assertThat(kardex.verifyIntegrity(target)).isEmpty();
            assertThat(peps.verifyIntegrity(target)).isEmpty();
            assertThat(avgSnapshot(B1)).hasSize(3).isEqualTo(avg1A);
            assertThat(pepsSnapshot(B3)).isEqualTo(pepsA);
            verifyNoInteractions(rabbitTemplate);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Dato inconsistente del origen: movimientos de un producto de la empresa SIN réplica en kardex_products. El
     * export los excluye con un warning que nombra el producto y su cantidad, el contador del manifest es el de lo
     * exportado y el ZIP se restaura sin error.
     */
    @Test
    void movimientoHuerfanoEnElOrigenSeExcluyeConWarningYElZipRestaura() {
        ListAppender<ILoggingEvent> logs = new ListAppender<>();
        logs.start();
        Logger kardexLog = (Logger) LoggerFactory.getLogger(KardexMovementsBackupParticipant.class);
        kardexLog.addAppender(logs);
        TenantContext.setTenantId(TENANT_A);
        try {
            Seed seed = seedTenantA();
            UUID source = seed.enterprise();
            String orphanRef = "REF-HUERFANO-" + base;
            long orphanProduct = tx.execute(s -> {
                long id = inventoryProduct(source.toString(), orphanRef, "Producto huerfano");
                var buy = co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType.PURCHASE;
                avgMove(id, buy, 3, "2.00", 3, "2.00", "6.00", "2026-01-01");
                avgMove(id, buy, 2, "2.00", 5, "2.00", "10.00", "2026-01-02");
                return id;
            });
            Path zipPath = exportTenantA(source);

            assertThat(warnings(logs)).singleElement().asString()
                    .contains(orphanRef).contains("Producto huerfano").contains(String.valueOf(orphanProduct))
                    .contains("EXCLUYEN 2 movimientos");

            TenantContext.setTenantId(TENANT_B);
            UUID target = tx.execute(s -> persistEnterprise("Empresa B", InventoryConfigurationTypeEnum.WEIGHTED_AVERAGE));
            InMemoryIdMap idMap = seedReplicasTenantB(target.toString());
            try (ZipBackupReader reader = format.open(zipPath)) {
                reader.verifyAll();
                // el manifest cuenta lo exportado (4), no los 6 movimientos que hay en la tabla
                assertThat(reader.manifest().module(BackupKeys.MODULE_INVENTORY_KARDEX_MOVEMENTS).orElseThrow()
                        .entry(KardexMovementsBackupParticipant.ENTRY_KARDEX).orElseThrow().count()).isEqualTo(4);
                tx.executeWithoutResult(s -> kardex.restore(reader.module(kardex.moduleKey()), idMap,
                        ctxB(source, target)));
            }
            assertThat(kardex.count(target)).isEqualTo(Map.of(KardexMovementsBackupParticipant.ENTRY_KARDEX, 4L));
            assertThat(kardex.verifyIntegrity(target)).isEmpty();
        } finally {
            kardexLog.detachAppender(logs);
            TenantContext.clear();
        }
    }
}
