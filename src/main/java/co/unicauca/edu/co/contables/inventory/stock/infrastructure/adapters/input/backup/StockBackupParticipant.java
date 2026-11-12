package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.input.backup;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupPaging;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.input.backup.StockBackupFormat.StockRow;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.entity.StockEntity;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.entity.SyncStateEntity;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.repository.ISyncStateRepository;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.repository.StockRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * @brief Participante {@code inventory.stock} (order 60): réplica de stock de cada producto.
 *
 * Hoy {@code StockListener} crea esta fila al recibir el evento de producto creado. En el restore NO
 * se publica nada: las filas vienen en el ZIP y se escriben directo por {@link StockRepository}, con
 * {@code productId} remapeado con {@code PRODUCT}. La cantidad/precio son el snapshot del origen
 * (coherente con {@code product.quantity}); los movimientos de kardex son la fase 2.
 *
 * Además crea la fila {@code sync_state} ({@code syncType="products"}, fecha = ahora) de la empresa
 * destino —no se exporta—: sin ella, la primera sincronización de productos arrancaría desde el año
 * 2000 y reprocesaría todo el catálogo. La tabla es compartida con los módulos kardex y PEPS (usan la
 * misma entidad y el mismo {@code syncType}), por eso la crea un solo participante.
 */
@Component
@RequiredArgsConstructor
public class StockBackupParticipant implements BackupParticipant {

    public static final int SCHEMA_VERSION = 1;
    /** Mismo valor que usan los tres {@code ProductCommandService} de inventario. */
    public static final String SYNC_TYPE_PRODUCTS = "products";

    private static final int BATCH = BackupPaging.PAGE_SIZE;
    private static final Pageable SAMPLE = PageRequest.of(0, 20);

    private final StockRepository stockRepository;
    private final ISyncStateRepository syncStateRepository;
    private final EntityManager entityManager;

    @Override
    public String moduleKey() {
        return BackupKeys.MODULE_INVENTORY_STOCK;
    }

    @Override
    public int order() {
        return BackupKeys.ORDER_INVENTORY_STOCK;
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public Set<String> requires() {
        return Set.of(BackupKeys.MODULE_PRODUCTS);
    }

    @Override
    public void export(UUID enterpriseId, BackupWriter writer) {
        String ent = enterpriseId.toString();
        writer.writeNdjson(StockBackupFormat.STOCK, BackupPaging.iterate(
                p -> stockRepository.findByEnterpriseId(ent, BackupPaging.page(p)),
                s -> new StockRow(s.getId(), s.getProductId(), s.getName(), s.getQuantity(), s.getPrice(),
                        s.isState()),
                entityManager::clear));
    }

    @Override
    public void restore(BackupReader reader, IdMap idMap, RestoreContext ctx) {
        String ent = ctx.requireTargetEnterpriseId().toString();
        if (reader.has(StockBackupFormat.STOCK)) {
            reader.forEachBatch(StockBackupFormat.STOCK, StockRow.class, BATCH, rows -> {
                List<StockEntity> entities = new ArrayList<>(rows.size());
                for (StockRow r : rows) {
                    StockEntity e = new StockEntity();
                    e.setProductId(idMap.requireLong(BackupKeys.PRODUCT, r.productId()));
                    e.setName(r.name());
                    e.setEnterpriseId(ent);
                    e.setQuantity(r.quantity());
                    e.setPrice(r.price());
                    e.setState(r.state());
                    entities.add(e);
                }
                stockRepository.saveAll(entities);
                entityManager.flush();
                entityManager.clear();
            });
        }
        if (syncStateRepository.findBySyncTypeAndEnterpriseId(SYNC_TYPE_PRODUCTS, ent).isEmpty()) {
            SyncStateEntity state = new SyncStateEntity();
            state.setSyncType(SYNC_TYPE_PRODUCTS);
            state.setEnterpriseId(ent);
            state.setLastSyncDate(Instant.now());
            syncStateRepository.save(state);
        }
    }

    /**
     * Borra la réplica {@code stock_entity} de la empresa. {@code sync_state} NO se borra: es del tenant y el
     * restore ya no la recrea si existe.
     */
    @Override
    public void purge(UUID enterpriseId) {
        stockRepository.deleteAllByEnterpriseIdBulk(enterpriseId.toString());
        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public Map<String, Long> count(UUID enterpriseId) {
        return Map.of(StockBackupFormat.STOCK, stockRepository.countByEnterpriseId(enterpriseId.toString()));
    }

    @Override
    public List<String> verifyIntegrity(UUID enterpriseId) {
        List<Long> orphans = stockRepository.findProductIdsWithoutProduct(enterpriseId.toString(), SAMPLE);
        if (orphans.isEmpty()) {
            return List.of();
        }
        return List.of(orphans.size() + " registros de stock referencian un producto inexistente (productId "
                + orphans + ")");
    }
}
