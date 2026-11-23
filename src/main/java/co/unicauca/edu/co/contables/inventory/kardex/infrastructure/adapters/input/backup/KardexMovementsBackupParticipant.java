package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.backup;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.commons.backup.BackupIntegrityException;
import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Participante {@code inventory.kardex.movements} (order 70): los movimientos del kardex de
 * promedio ponderado ({@code kardex}) de la empresa.
 *
 * Export: {@code inventory/kardex.ndjson}, paginado por id (keyset). La tabla no tiene columna de empresa:
 * se exportan los movimientos de los productos que tienen réplica en {@code kardex_products} de la empresa.
 * {@code productId} viaja como id VIEJO del producto.
 *
 * Restore: PKs nuevas, {@code productId} = {@code idMap.require(PRODUCT)}, {@code @TenantId} automático.
 * Los saldos ({@code balance*}) y el costo promedio se copian TAL CUAL: no se recalcula nada. {@code factCode}
 * (número de factura/documento de origen) es una etiqueta histórica en texto, NO una FK: viaja como está y no
 * exige que el documento exista en la copia (cuando facturas/asientos entren al ZIP se podrá renumerar).
 * Cada producto referenciado debe tener ya su réplica {@code kardex_products} en la empresa destino
 * (la restaura el módulo {@code inventory.kardex}, order 61); si falta, se aborta con
 * {@link BackupIntegrityException}.
 *
 * Verificación: conteo, saldos no negativos y, SOLO si la empresa usa promedio ponderado, que el stock de cada
 * producto con movimientos sea igual al saldo del último movimiento. Escribe por repositorio: no pasa por
 * los servicios del kardex (publican a Rabbit y llaman a stock).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KardexMovementsBackupParticipant implements BackupParticipant {

    public static final int SCHEMA_VERSION = 1;
    public static final String ENTRY_KARDEX = "inventory/kardex.ndjson";

    private static final int BATCH = 500;
    private static final int MAX_PROBLEMS = 50;
    private static final String METHOD = "WEIGHTED_AVERAGE";

    /** Stock != saldo del último movimiento (último = mayor fecha; a igual fecha, mayor id). */
    private static final String STOCK_MISMATCH = """
            select s.productId, s.quantity, k.balanceQuantity
            from WeightedAverageKardexEntity k, StockEntity s
            where s.productId = k.productId
              and s.enterpriseId = :enterpriseId
              and k.id = (select max(k2.id) from WeightedAverageKardexEntity k2
                          where k2.productId = k.productId
                            and k2.date = (select max(k3.date) from WeightedAverageKardexEntity k3
                                           where k3.productId = k.productId))
              and s.quantity <> k.balanceQuantity
            order by s.productId
            """;

    private final KardexMovementsBackupRepository repository;
    private final EntityManager entityManager;

    /** Registro NDJSON de un movimiento; {@code productId} es el id VIEJO del producto, {@code date} ISO-8601 con offset. */
    public record KardexRow(
            Long id,
            String factCode,
            int quantity,
            BigDecimal unitPrice,
            String details,
            String type,
            int balanceQuantity,
            BigDecimal balanceUnitPrice,
            BigDecimal totalBalance,
            String date,
            Long productId) {
    }

    @Override
    public String moduleKey() {
        return BackupKeys.MODULE_INVENTORY_KARDEX_MOVEMENTS;
    }

    @Override
    public int order() {
        return BackupKeys.ORDER_INVENTORY_KARDEX_MOVEMENTS;
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public Set<String> requires() {
        return Set.of(BackupKeys.MODULE_PRODUCTS, BackupKeys.MODULE_INVENTORY_STOCK,
                BackupKeys.MODULE_INVENTORY_KARDEX);
    }

    /* ------------------------------------------------------------------ */
    /* Export                                                             */
    /* ------------------------------------------------------------------ */

    @Override
    public void export(UUID enterpriseId, BackupWriter writer) {
        String enterprise = enterpriseId.toString();
        warnOrphanMovements(enterprise);
        writer.writeNdjson(ENTRY_KARDEX, new KardexIterator(enterprise));
    }

    /**
     * Dato inconsistente del origen: movimientos de un producto de la empresa sin réplica en kardex_products.
     * La consulta del export los deja fuera (se resuelven por la réplica), así el ZIP siempre se puede restaurar
     * y el contador del manifest es el de lo realmente exportado.
     */
    private void warnOrphanMovements(String enterprise) {
        for (Object[] o : repository.findOrphanMovements(enterprise)) {
            log.warn("Backup kardex: se EXCLUYEN {} movimientos del producto id={} ref='{}' nombre='{}' de la "
                    + "empresa {}: el producto no tiene réplica en kardex_products (dato inconsistente del "
                    + "origen); incluirlos haría el ZIP irrestaurable", o[3], o[0], o[1], o[2], enterprise);
        }
    }

    /** Recorre los movimientos por páginas de id ascendente (keyset). */
    private final class KardexIterator implements Iterator<KardexRow> {
        private final String enterprise;
        private long lastId = 0L;
        private Iterator<KardexEntity> page = List.<KardexEntity>of().iterator();
        private boolean exhausted;

        KardexIterator(String enterprise) {
            this.enterprise = enterprise;
        }

        @Override
        public boolean hasNext() {
            if (page.hasNext()) {
                return true;
            }
            if (exhausted) {
                return false;
            }
            List<KardexEntity> next = repository.findBackupBatch(enterprise, lastId, PageRequest.of(0, BATCH));
            if (next.size() < BATCH) {
                exhausted = true;
            }
            if (next.isEmpty()) {
                return false;
            }
            lastId = next.get(next.size() - 1).getId();
            page = next.iterator();
            // acota el contexto de persistencia: los campos ya cargados siguen legibles
            next.forEach(entityManager::detach);
            return true;
        }

        @Override
        public KardexRow next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            KardexEntity k = page.next();
            return new KardexRow(k.getId(), k.getFactCode(), k.getQuantity(), k.getUnitPrice(), k.getDetails(),
                    k.getType().name(), k.getBalanceQuantity(), k.getBalanceUnitPrice(), k.getTotalBalance(),
                    k.getDate().toOffsetDateTime().toString(), k.getProductId());
        }
    }

    /* ------------------------------------------------------------------ */
    /* Restore                                                            */
    /* ------------------------------------------------------------------ */

    @Override
    public void restore(BackupReader reader, IdMap idMap, RestoreContext ctx) {
        if (!reader.has(ENTRY_KARDEX)) {
            return;
        }
        String enterprise = ctx.targetEnterpriseIdAsString();
        reader.forEachBatch(ENTRY_KARDEX, KardexRow.class, BATCH, rows -> {
            List<Long> newProductIds = new ArrayList<>(rows.size());
            for (KardexRow r : rows) {
                newProductIds.add(idMap.requireLong(BackupKeys.PRODUCT, r.productId()));
            }
            assertReplicas(newProductIds, enterprise);

            List<KardexEntity> entities = new ArrayList<>(rows.size());
            for (int i = 0; i < rows.size(); i++) {
                KardexRow r = rows.get(i);
                KardexEntity k = new KardexEntity();
                k.setFactCode(r.factCode());
                k.setQuantity(r.quantity());
                k.setUnitPrice(r.unitPrice());
                k.setDetails(r.details());
                k.setType(movementType(r));
                k.setBalanceQuantity(r.balanceQuantity());
                k.setBalanceUnitPrice(r.balanceUnitPrice());
                k.setTotalBalance(r.totalBalance());
                k.setDate(OffsetDateTime.parse(r.date()).toZonedDateTime());
                k.setProductId(newProductIds.get(i));
                entities.add(k);
            }
            repository.saveAll(entities);
            entityManager.flush();
            entityManager.clear();
        });
    }

    /**
     * Borra los movimientos de los productos de la empresa (la tabla no tiene empresa). Debe correr ANTES de
     * borrar productos y réplicas, porque se resuelve por ellos.
     */
    @Override
    public void purge(UUID enterpriseId) {
        repository.deleteByEnterprise(enterpriseId.toString());
        entityManager.flush();
        entityManager.clear();
    }

    private MovementType movementType(KardexRow r) {
        try {
            return MovementType.valueOf(r.type());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BackupIntegrityException(moduleKey(),
                    "el movimiento id=" + r.id() + " tiene un tipo desconocido: '" + r.type() + "'", e);
        }
    }

    /** Cada producto referenciado debe tener su réplica kardex_products en la empresa destino. */
    private void assertReplicas(List<Long> newProductIds, String enterprise) {
        Set<Long> wanted = new LinkedHashSet<>(newProductIds);
        List<Long> found = entityManager.createQuery("""
                select p.productId from WeightedAverageProductEntity p
                where p.productId in :ids and p.enterpriseId = :enterprise
                """, Long.class)
                .setParameter("ids", wanted)
                .setParameter("enterprise", enterprise)
                .getResultList();
        Set<Long> missing = new LinkedHashSet<>(wanted);
        missing.removeAll(new HashSet<>(found));
        if (!missing.isEmpty()) {
            throw new BackupIntegrityException(moduleKey(),
                    "los movimientos del kardex referencian productos sin réplica en kardex_products de la "
                            + "empresa destino (ids nuevos " + missing + ": " + describeProducts(missing)
                            + "); el módulo inventory.kardex debe restaurarse antes");
        }
    }

    /** "ref (nombre)" de los productos NUEVOS indicados, para que el error diga cuál falla. */
    private String describeProducts(Set<Long> newProductIds) {
        List<Object[]> rows = entityManager.createQuery(
                "select p.id, p.reference, p.name from InventoryProductEntity p where p.id in :ids order by p.id",
                Object[].class).setParameter("ids", newProductIds).getResultList();
        if (rows.isEmpty()) {
            return "productos no encontrados";
        }
        return rows.stream().map(r -> "id " + r[0] + " ref '" + r[1] + "' (" + r[2] + ")")
                .collect(java.util.stream.Collectors.joining(", "));
    }

    /* ------------------------------------------------------------------ */
    /* Verificación                                                       */
    /* ------------------------------------------------------------------ */

    @Override
    public Map<String, Long> count(UUID enterpriseId) {
        return Map.of(ENTRY_KARDEX, repository.countByEnterprise(enterpriseId.toString()));
    }

    @Override
    public List<String> verifyIntegrity(UUID enterpriseId) {
        String enterprise = enterpriseId.toString();
        List<String> problems = new ArrayList<>();

        long negatives = repository.countWithNegativeBalance(enterprise);
        if (negatives > 0) {
            problems.add(negatives + " movimientos del kardex tienen saldo de cantidad negativo");
        }

        if (usesWeightedAverage(enterpriseId)) {
            List<?> mismatches = entityManager.createQuery(STOCK_MISMATCH)
                    .setParameter("enterpriseId", enterprise)
                    .setMaxResults(MAX_PROBLEMS)
                    .getResultList();
            for (Object row : mismatches) {
                Object[] c = (Object[]) row;
                // ADVERTENCIA, no error (diseño §6.8): si el origen ya estaba descuadrado, la copia lo refleja
                // tal cual y no debe bloquear el restore. El contrato de resultado no tiene canal de avisos.
                log.warn("Restore kardex: producto {}: el stock ({}) no coincide con el saldo del último "
                        + "movimiento del kardex ({}); se copia tal cual desde el origen", c[0], c[1], c[2]);
            }
        }
        return problems;
    }

    /** {@code true} si la empresa está configurada con promedio ponderado (si no, el stock lo rige PEPS). */
    private boolean usesWeightedAverage(UUID enterpriseId) {
        List<?> method = entityManager
                .createQuery("select e.inventoryConfigurationType from EnterpriseEntity e where e.id = :id")
                .setParameter("id", enterpriseId)
                .getResultList();
        return !method.isEmpty() && method.get(0) != null && METHOD.equals(String.valueOf(method.get(0)));
    }
}
