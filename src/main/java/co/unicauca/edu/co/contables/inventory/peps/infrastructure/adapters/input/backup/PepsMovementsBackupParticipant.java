package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.backup;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.commons.backup.BackupIntegrityException;
import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import co.unicauca.edu.co.contables.inventory.peps.domain.enums.MovementType;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.DetailOutputEntity;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.KardexEntity;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.ProductEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Participante {@code inventory.peps.movements} (order 80): las capas PEPS ({@code peps_kardex}) y el
 * detalle de salidas ({@code DetailOutputFIFO}) de la empresa.
 *
 * Entradas:
 * <ol>
 * <li>{@code inventory/peps_kardex.ndjson}: movimientos/capas; {@code availableQuantity} (la capa viva) se copia
 * tal cual y {@code productId} viaja como id VIEJO del producto.</li>
 * <li>{@code inventory/peps_detail_output.ndjson}: qué capa de origen consumió cada salida
 * ({@code saleId}/{@code originId} = ids VIEJOS de {@code peps_kardex}).</li>
 * </ol>
 *
 * Restore: primero todos los movimientos (registra {@code PEPS_KARDEX} viejo→nuevo), después los detalles con
 * {@code idMap.require(PEPS_KARDEX)}. El producto se enlaza con su réplica {@code peps_product} de la empresa
 * destino (la restaura {@code inventory.peps}, order 62); si falta, {@link BackupIntegrityException}. Las tablas
 * no tienen {@code @TenantId} (está comentado en las entidades): se acotan por {@code peps_product.enterprise_id}.
 * {@code factCode} es una etiqueta de texto histórica, no una FK.
 *
 * Verificación: conteos; sin cantidades disponibles negativas; cada salida con capa de origen del mismo
 * producto; y, SOLO si la empresa usa PEPS, stock de cada producto con capas == Σ cantidades disponibles.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PepsMovementsBackupParticipant implements BackupParticipant {

    public static final int SCHEMA_VERSION = 1;
    public static final String ENTRY_KARDEX = "inventory/peps_kardex.ndjson";
    public static final String ENTRY_DETAILS = "inventory/peps_detail_output.ndjson";

    private static final int BATCH = 500;
    private static final int MAX_PROBLEMS = 50;
    private static final String METHOD = "PEPS";

    private static final String STOCK_MISMATCH = """
            select s.productId, s.quantity, sum(k.availableQuantity)
            from KardexEntity k, StockEntity s
            where k.product.productId = s.productId and s.enterpriseId = :enterpriseId
            group by s.productId, s.quantity
            having s.quantity <> sum(k.availableQuantity)
            order by s.productId
            """;

    private final PepsMovementsBackupRepository repository;
    private final EntityManager entityManager;

    /** Capa/movimiento PEPS; {@code productId} = id VIEJO del producto, {@code date} ISO-8601 con offset. */
    public record PepsKardexRow(
            Long id,
            String factCode,
            String date,
            String details,
            int quantity,
            BigDecimal unitPrice,
            String type,
            int availableQuantity,
            Long productId) {
    }

    /** Salida: {@code saleId}/{@code originId} = ids VIEJOS de peps_kardex ({@code originId} puede ser null). */
    public record PepsDetailRow(Long id, int quantityUsed, BigDecimal unitPrice, Long saleId, Long originId) {
    }

    @Override
    public String moduleKey() {
        return BackupKeys.MODULE_INVENTORY_PEPS_MOVEMENTS;
    }

    @Override
    public int order() {
        return BackupKeys.ORDER_INVENTORY_PEPS_MOVEMENTS;
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public Set<String> requires() {
        return Set.of(BackupKeys.MODULE_PRODUCTS, BackupKeys.MODULE_INVENTORY_STOCK,
                BackupKeys.MODULE_INVENTORY_PEPS);
    }

    @Override
    public void preflight(BackupReader reader, RestoreContext ctx) {
        // Un detalle sin ninguna capa en la copia no se puede restaurar (sus FKs a peps_kardex quedarían huérfanas).
        if (reader.has(ENTRY_DETAILS) && reader.declaredCount(ENTRY_DETAILS) > 0
                && (!reader.has(ENTRY_KARDEX) || reader.declaredCount(ENTRY_KARDEX) == 0)) {
            throw new BackupIntegrityException(moduleKey(),
                    "la copia trae detalles de salidas PEPS pero ninguna capa en " + ENTRY_KARDEX);
        }
    }

    /* ------------------------------------------------------------------ */
    /* Export                                                             */
    /* ------------------------------------------------------------------ */

    @Override
    public void export(UUID enterpriseId, BackupWriter writer) {
        String enterprise = enterpriseId.toString();
        // Se escriben SIEMPRE ambas entradas (aunque queden en 0) para que el conteo del manifest sea completo.
        writer.writeNdjson(ENTRY_KARDEX, new KeysetIterator<>(
                last -> repository.findKardexBatch(enterprise, last, PageRequest.of(0, BATCH)),
                PepsMovementsBackupParticipant::toKardexRow));
        writer.writeNdjson(ENTRY_DETAILS, new KeysetIterator<>(
                last -> repository.findDetailBatch(enterprise, last, PageRequest.of(0, BATCH)),
                PepsMovementsBackupParticipant::toDetailRow));
    }

    private static PepsKardexRow toKardexRow(Object[] c) {
        return new PepsKardexRow((Long) c[0], (String) c[1], ((ZonedDateTime) c[2]).toOffsetDateTime().toString(),
                (String) c[3], ((Number) c[4]).intValue(), (BigDecimal) c[5], c[6] == null ? null : c[6].toString(),
                ((Number) c[7]).intValue(), (Long) c[8]);
    }

    private static PepsDetailRow toDetailRow(Object[] c) {
        return new PepsDetailRow((Long) c[0], ((Number) c[1]).intValue(), (BigDecimal) c[2], (Long) c[3],
                (Long) c[4]);
    }

    /** Recorre una consulta por páginas de id ascendente (keyset); el id es la columna 0 de cada fila. */
    private static final class KeysetIterator<R> implements Iterator<R> {
        private final java.util.function.LongFunction<List<Object[]>> fetch;
        private final Function<Object[], R> mapper;
        private long lastId = 0L;
        private Iterator<Object[]> page = List.<Object[]>of().iterator();
        private boolean exhausted;

        KeysetIterator(java.util.function.LongFunction<List<Object[]>> fetch, Function<Object[], R> mapper) {
            this.fetch = fetch;
            this.mapper = mapper;
        }

        @Override
        public boolean hasNext() {
            if (page.hasNext()) {
                return true;
            }
            if (exhausted) {
                return false;
            }
            List<Object[]> next = fetch.apply(lastId);
            if (next.size() < BATCH) {
                exhausted = true;
            }
            if (next.isEmpty()) {
                return false;
            }
            lastId = (Long) next.get(next.size() - 1)[0];
            page = next.iterator();
            return true;
        }

        @Override
        public R next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return mapper.apply(page.next());
        }
    }

    /* ------------------------------------------------------------------ */
    /* Restore                                                            */
    /* ------------------------------------------------------------------ */

    @Override
    public void restore(BackupReader reader, IdMap idMap, RestoreContext ctx) {
        String enterprise = ctx.targetEnterpriseIdAsString();

        if (reader.has(ENTRY_KARDEX)) {
            reader.forEachBatch(ENTRY_KARDEX, PepsKardexRow.class, BATCH, rows -> restoreKardex(rows, idMap, enterprise));
        }
        if (reader.has(ENTRY_DETAILS)) {
            reader.forEachBatch(ENTRY_DETAILS, PepsDetailRow.class, BATCH, rows -> restoreDetails(rows, idMap));
        }
    }

    /**
     * Borra el detalle de salidas y luego las capas PEPS de la empresa (se resuelven por la réplica
     * {@code peps_product}, así que debe correr ANTES de borrarla).
     */
    @Override
    public void purge(UUID enterpriseId) {
        String enterprise = enterpriseId.toString();
        repository.deleteDetailsByEnterprise(enterprise);
        repository.deleteLayersByEnterprise(enterprise);
        entityManager.flush();
        entityManager.clear();
    }

    private void restoreKardex(List<PepsKardexRow> rows, IdMap idMap, String enterprise) {
        List<Long> newProductIds = new ArrayList<>(rows.size());
        for (PepsKardexRow r : rows) {
            newProductIds.add(idMap.requireLong(BackupKeys.PRODUCT, r.productId()));
        }
        Map<Long, ProductEntity> replicas = loadReplicas(new LinkedHashSet<>(newProductIds), enterprise);

        List<KardexEntity> entities = new ArrayList<>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            PepsKardexRow r = rows.get(i);
            KardexEntity k = new KardexEntity();
            k.setFactCode(r.factCode());
            k.setDate(OffsetDateTime.parse(r.date()).toZonedDateTime());
            k.setDetails(r.details());
            k.setQuantity(r.quantity());
            k.setUnitPrice(r.unitPrice());
            k.setType(movementType(r));
            k.setAvailableQuantity(r.availableQuantity());
            k.setProduct(replicas.get(newProductIds.get(i)));
            entities.add(k);
        }
        repository.saveAll(entities);
        entityManager.flush();
        for (int i = 0; i < rows.size(); i++) {
            idMap.put(BackupKeys.PEPS_KARDEX, rows.get(i).id(), entities.get(i).getIdKardex());
        }
        entityManager.clear();
    }

    private void restoreDetails(List<PepsDetailRow> rows, IdMap idMap) {
        List<DetailOutputEntity> entities = new ArrayList<>(rows.size());
        for (PepsDetailRow r : rows) {
            Long saleId = idMap.requireLong(BackupKeys.PEPS_KARDEX, r.saleId());
            Long originId = idMap.requireNullable(BackupKeys.PEPS_KARDEX, r.originId());
            DetailOutputEntity d = new DetailOutputEntity();
            d.setQuantityUsed(r.quantityUsed());
            d.setUnitPrice(r.unitPrice());
            d.setMovementSale(entityManager.getReference(KardexEntity.class, saleId));
            d.setMovementOrigin(originId == null ? null : entityManager.getReference(KardexEntity.class, originId));
            entities.add(d);
        }
        entities.forEach(entityManager::persist);
        entityManager.flush();
        entityManager.clear();
    }

    private MovementType movementType(PepsKardexRow r) {
        try {
            return r.type() == null ? null : MovementType.valueOf(r.type());
        } catch (IllegalArgumentException e) {
            throw new BackupIntegrityException(moduleKey(),
                    "el movimiento PEPS id=" + r.id() + " tiene un tipo desconocido: '" + r.type() + "'", e);
        }
    }

    /** Réplicas peps_product de la empresa destino, por id NUEVO de producto; falla si falta alguna. */
    private Map<Long, ProductEntity> loadReplicas(Set<Long> productIds, String enterprise) {
        List<ProductEntity> found = entityManager.createQuery("""
                select p from PepsProductEntity p
                where p.productId in :ids and p.enterpriseId = :enterprise
                """, ProductEntity.class)
                .setParameter("ids", productIds)
                .setParameter("enterprise", enterprise)
                .getResultList();
        Map<Long, ProductEntity> byProductId = new HashMap<>();
        for (ProductEntity p : found) {
            byProductId.put(p.getProductId(), p);
        }
        Set<Long> missing = new LinkedHashSet<>(productIds);
        missing.removeAll(byProductId.keySet());
        if (!missing.isEmpty()) {
            throw new BackupIntegrityException(moduleKey(),
                    "las capas PEPS referencian productos sin réplica en peps_product de la empresa destino "
                            + "(ids nuevos " + missing + ": " + describeProducts(missing)
                            + "); el módulo inventory.peps debe restaurarse antes");
        }
        return byProductId;
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
        String enterprise = enterpriseId.toString();
        return Map.of(
                ENTRY_KARDEX, repository.countKardex(enterprise),
                ENTRY_DETAILS, repository.countDetails(enterprise));
    }

    @Override
    public List<String> verifyIntegrity(UUID enterpriseId) {
        String enterprise = enterpriseId.toString();
        List<String> problems = new ArrayList<>();

        long negatives = repository.countNegativeAvailable(enterprise);
        if (negatives > 0) {
            problems.add(negatives + " capas PEPS tienen cantidad disponible negativa");
        }
        long badOrigin = repository.countDetailsWithBadOrigin(enterprise);
        if (badOrigin > 0) {
            problems.add(badOrigin + " salidas PEPS no tienen capa de origen o su origen es de otro producto");
        }

        if (usesPeps(enterpriseId)) {
            List<?> mismatches = entityManager.createQuery(STOCK_MISMATCH)
                    .setParameter("enterpriseId", enterprise)
                    .setMaxResults(MAX_PROBLEMS)
                    .getResultList();
            for (Object row : mismatches) {
                Object[] c = (Object[]) row;
                // ADVERTENCIA, no error (diseño §6.8): si el origen ya estaba descuadrado, la copia lo refleja
                // tal cual y no debe bloquear el restore. El contrato de resultado no tiene canal de avisos.
                log.warn("Restore PEPS: producto {}: el stock ({}) no coincide con la suma de cantidades "
                        + "disponibles de las capas PEPS ({}); se copia tal cual desde el origen", c[0], c[1], c[2]);
            }
        }
        return problems;
    }

    /** {@code true} si la empresa está configurada con PEPS (si no, el stock lo rige el promedio ponderado). */
    private boolean usesPeps(UUID enterpriseId) {
        List<?> method = entityManager
                .createQuery("select e.inventoryConfigurationType from EnterpriseEntity e where e.id = :id")
                .setParameter("id", enterpriseId)
                .getResultList();
        return !method.isEmpty() && method.get(0) != null && METHOD.equals(String.valueOf(method.get(0)));
    }
}
