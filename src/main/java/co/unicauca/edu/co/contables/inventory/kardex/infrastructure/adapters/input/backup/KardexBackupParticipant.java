package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.backup;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.commons.backup.BackupIntegrityException;
import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupPaging;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import co.unicauca.edu.co.contables.commons.backup.RestoreMode;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.backup.KardexBackupFormat.KardexProductRow;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.ProductEntity;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.repository.IProductRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Participante {@code inventory.kardex} (order 61): réplica de producto del kardex de PROMEDIO
 * PONDERADO ({@code kardex_products}). Los movimientos ({@code kardex}) son la fase 2.
 *
 * <b>Sin eventos</b>: se escribe directo por {@link IProductRepository}; no se publica a Rabbit ni se
 * dispara {@code ProductListener}.
 *
 * <b>Método de la empresa</b>: los listeners crean la réplica en AMBOS kardex sin mirar el método y los
 * movimientos pueden estar en cualquiera de los dos (la empresa pudo cambiar de método). Por eso la
 * réplica viaja y se restaura SIEMPRE, sea cual sea el método; el método viaja en el snapshot de la empresa.
 *
 * <b>Choque con el UNIQUE global de {@code kardex_products.reference}</b> (D2): la columna es única en
 * TODA la tabla (no por empresa ni por tenant), así que duplicar una empresa dentro del mismo tenant
 * (o restaurar una referencia que ya usa otra empresa) violaría la restricción. {@link #preflight}
 * —antes de abrir la transacción— y {@link #restore} comparan las referencias de la copia contra la
 * tabla con SQL nativo (no filtrado por {@code @TenantId}) y lanzan {@link BackupIntegrityException}
 * con las referencias en conflicto, en vez de dejar pasar un error genérico de BD. La comprobación
 * solo se hace si la restricción actual sigue siendo global: tras la migración a
 * {@code UNIQUE(enterprise_id, reference)} se desactiva sola (se lee del metadata de la BD).
 */
@Component
@Slf4j
public class KardexBackupParticipant implements BackupParticipant {

    public static final int SCHEMA_VERSION = 1;

    private static final String TABLE = "kardex_products";
    private static final int BATCH = BackupPaging.PAGE_SIZE;
    private static final int MAX_REFERENCES_IN_MESSAGE = 10;
    private static final Pageable SAMPLE = PageRequest.of(0, 20);

    private final IProductRepository productRepository;
    private final EntityManager entityManager;
    private final DataSource dataSource;

    public KardexBackupParticipant(IProductRepository productRepository, EntityManager entityManager,
            DataSource dataSource) {
        this.productRepository = productRepository;
        this.entityManager = entityManager;
        this.dataSource = dataSource;
    }

    @Override
    public String moduleKey() {
        return BackupKeys.MODULE_INVENTORY_KARDEX;
    }

    @Override
    public int order() {
        return BackupKeys.ORDER_INVENTORY_KARDEX;
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public Set<String> requires() {
        return Set.of(BackupKeys.MODULE_PRODUCTS);
    }

    /* ------------------------------------------------------------------ */
    /* Export                                                             */
    /* ------------------------------------------------------------------ */

    @Override
    public void export(UUID enterpriseId, BackupWriter writer) {
        String ent = enterpriseId.toString();
        writer.writeNdjson(KardexBackupFormat.PRODUCTS, BackupPaging.iterate(
                p -> productRepository.findByEnterpriseId(ent, BackupPaging.page(p)),
                k -> new KardexProductRow(k.getId(), k.getProductId(), k.getReference(), k.getName(),
                        k.getPresentation(), k.isState()),
                entityManager::clear));
    }

    /* ------------------------------------------------------------------ */
    /* Restore                                                            */
    /* ------------------------------------------------------------------ */

    @Override
    public void preflight(BackupReader reader, RestoreContext ctx) {
        if (reader.has(KardexBackupFormat.PRODUCTS)) {
            checkReferenceConflicts(reader, ctx);
        }
    }

    /** Borra las réplicas {@code kardex_products} de la empresa (sus movimientos ya los borró el otro módulo). */
    @Override
    public void purge(UUID enterpriseId) {
        productRepository.deleteAllByEnterpriseIdBulk(enterpriseId.toString());
        entityManager.flush();
        entityManager.clear();
    }

    @Override
    public void restore(BackupReader reader, IdMap idMap, RestoreContext ctx) {
        UUID target = ctx.requireTargetEnterpriseId();
        if (!reader.has(KardexBackupFormat.PRODUCTS)) {
            return;
        }
        // Defensa en profundidad: el preflight ya lo comprobó antes de la transacción.
        checkReferenceConflicts(reader, ctx);
        String ent = target.toString();
        reader.forEachBatch(KardexBackupFormat.PRODUCTS, KardexProductRow.class, BATCH, rows -> {
            List<ProductEntity> entities = new ArrayList<>(rows.size());
            for (KardexProductRow r : rows) {
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

    /**
     * Compara las referencias de la copia con {@code kardex_products.reference} (toda la tabla). Lanza
     * {@link BackupIntegrityException} listando las primeras en conflicto.
     */
    private void checkReferenceConflicts(BackupReader reader, RestoreContext ctx) {
        if (!referenceIsGloballyUnique()) {
            return;
        }
        // En IN_PLACE las referencias de la propia empresa destino se borran (purge) antes de restaurar: no chocan.
        String ownEnterprise = ctx.mode() == RestoreMode.IN_PLACE && ctx.targetEnterpriseId() != null
                ? ctx.targetEnterpriseId().toString() : null;
        Set<String> conflicts = new LinkedHashSet<>();
        reader.forEachBatch(KardexBackupFormat.PRODUCTS, KardexProductRow.class, BATCH, rows -> {
            List<String> refs = rows.stream().map(KardexProductRow::reference)
                    .filter(r -> r != null && !r.isBlank()).distinct().toList();
            if (!refs.isEmpty()) {
                conflicts.addAll(ownEnterprise == null
                        ? productRepository.findExistingReferences(refs)
                        : productRepository.findExistingReferencesExcludingEnterprise(refs, ownEnterprise));
            }
        });
        if (conflicts.isEmpty()) {
            return;
        }
        List<String> shown = conflicts.stream().limit(MAX_REFERENCES_IN_MESSAGE).toList();
        String more = conflicts.size() > shown.size() ? " y " + (conflicts.size() - shown.size()) + " más" : "";
        throw new BackupIntegrityException(BackupKeys.MODULE_INVENTORY_KARDEX,
                conflicts.size() + " referencias de producto ya existen en el kardex de promedio ponderado ("
                        + String.join(", ", shown) + more + ") y la restricción actual de la base de datos exige "
                        + "referencias únicas en toda la tabla (no por empresa), por eso no se puede copiar la "
                        + "empresa dentro del mismo tenant ni usar una referencia de otra empresa. Hay que "
                        + "migrar kardex_products.reference a UNIQUE(enterprise_id, reference).");
    }

    /**
     * {@code true} si existe un índice/constraint UNIQUE cuya única columna es {@code reference}. Se lee
     * del metadata de la BD para que la comprobación se apague sola cuando se migre a una restricción
     * por empresa. Ante cualquier duda (tabla no encontrada, error de metadata) se asume global.
     */
    public boolean referenceIsGloballyUnique() {
        try (Connection c = dataSource.getConnection()) {
            DatabaseMetaData md = c.getMetaData();
            for (String table : new String[] { TABLE, TABLE.toUpperCase(Locale.ROOT), TABLE.toLowerCase(Locale.ROOT) }) {
                Map<String, List<String>> indexes = new LinkedHashMap<>();
                try (ResultSet rs = md.getIndexInfo(c.getCatalog(), c.getSchema(), table, true, false)) {
                    while (rs.next()) {
                        String index = rs.getString("INDEX_NAME");
                        String column = rs.getString("COLUMN_NAME");
                        if (index != null && column != null) {
                            indexes.computeIfAbsent(index, k -> new ArrayList<>()).add(column.toLowerCase(Locale.ROOT));
                        }
                    }
                }
                if (!indexes.isEmpty()) {
                    return indexes.values().stream().anyMatch(cols -> cols.size() == 1 && cols.get(0).equals("reference"));
                }
            }
        } catch (SQLException e) {
            log.warn("No se pudo leer el metadata de {}; se asume UNIQUE global en reference", TABLE, e);
        }
        return true;
    }

    /* ------------------------------------------------------------------ */
    /* Verificación                                                       */
    /* ------------------------------------------------------------------ */

    @Override
    public Map<String, Long> count(UUID enterpriseId) {
        return Map.of(KardexBackupFormat.PRODUCTS, productRepository.countByEnterpriseId(enterpriseId.toString()));
    }

    @Override
    public List<String> verifyIntegrity(UUID enterpriseId) {
        List<Long> orphans = productRepository.findProductIdsWithoutProduct(enterpriseId.toString(), SAMPLE);
        if (orphans.isEmpty()) {
            return List.of();
        }
        return List.of(orphans.size() + " réplicas de kardex (promedio ponderado) referencian un producto "
                + "inexistente (productId " + orphans + ")");
    }
}
