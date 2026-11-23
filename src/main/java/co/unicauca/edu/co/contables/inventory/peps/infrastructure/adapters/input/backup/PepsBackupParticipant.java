package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.backup;

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
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.backup.PepsBackupFormat.PepsProductRow;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.ProductEntity;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository.IProductRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Participante {@code inventory.peps} (order 62): réplica de producto del kardex PEPS
 * ({@code peps_product}). Las capas ({@code peps_kardex}) y salidas ({@code DetailOutputFIFO}) son la fase 2.
 *
 * <b>Sin eventos</b>: se escribe directo por {@link IProductRepository}; no se publica a Rabbit ni se
 * dispara {@code ProductListener}.
 *
 * <b>Método de la empresa</b>: igual que el kardex de promedio ponderado, la réplica viaja y se restaura
 * SIEMPRE, sea cual sea el método vigente (los listeners la crean en ambos kardex y la empresa pudo cambiar
 * de método con movimientos en el otro). {@code peps_product} no tiene UNIQUE por
 * referencia (solo {@code product_id}, que es nuevo), así que no hay choque como en el otro kardex.
 * Esta tabla no tiene {@code @TenantId} (está comentado en la entidad): se filtra solo por empresa.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PepsBackupParticipant implements BackupParticipant {

    public static final int SCHEMA_VERSION = 1;

    private static final int BATCH = BackupPaging.PAGE_SIZE;
    private static final Pageable SAMPLE = PageRequest.of(0, 20);

    private final IProductRepository productRepository;
    private final EntityManager entityManager;

    @Override
    public String moduleKey() {
        return BackupKeys.MODULE_INVENTORY_PEPS;
    }

    @Override
    public int order() {
        return BackupKeys.ORDER_INVENTORY_PEPS;
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
        writer.writeNdjson(PepsBackupFormat.PRODUCTS, BackupPaging.iterate(
                p -> productRepository.findByEnterpriseId(ent, BackupPaging.page(p)),
                k -> new PepsProductRow(k.getId(), k.getProductId(), k.getReference(), k.getName(),
                        k.getPresentation(), k.isState()),
                entityManager::clear));
    }

    @Override
    public void restore(BackupReader reader, IdMap idMap, RestoreContext ctx) {
        UUID target = ctx.requireTargetEnterpriseId();
        if (!reader.has(PepsBackupFormat.PRODUCTS)) {
            return;
        }
        String ent = target.toString();
        reader.forEachBatch(PepsBackupFormat.PRODUCTS, PepsProductRow.class, BATCH, rows -> {
            List<ProductEntity> entities = new ArrayList<>(rows.size());
            for (PepsProductRow r : rows) {
                ProductEntity e = new ProductEntity();
                e.setProductId(idMap.requireLong(BackupKeys.PRODUCT, r.productId()));
                e.setReference(r.reference());
                e.setName(r.name());
                e.setPresentation(r.presentation());
                e.setEnterpriseId(ent);
                e.setState(r.state());
                entities.add(e);
            }
            productRepository.saveAll(entities);
            entityManager.flush();
            entityManager.clear();
        });
    }

    /** Borra las réplicas {@code peps_product} de la empresa (sus capas ya las borró el módulo de movimientos). */
    @Override
    public void purge(UUID enterpriseId) {
        productRepository.deleteAllByEnterpriseIdBulk(enterpriseId.toString());
        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public Map<String, Long> count(UUID enterpriseId) {
        return Map.of(PepsBackupFormat.PRODUCTS, productRepository.countByEnterpriseId(enterpriseId.toString()));
    }

    @Override
    public List<String> verifyIntegrity(UUID enterpriseId) {
        List<Long> orphans = productRepository.findProductIdsWithoutProduct(enterpriseId.toString(), SAMPLE);
        if (orphans.isEmpty()) {
            return List.of();
        }
        return List.of(orphans.size() + " réplicas de kardex PEPS referencian un producto inexistente "
                + "(productId " + orphans + ")");
    }
}
