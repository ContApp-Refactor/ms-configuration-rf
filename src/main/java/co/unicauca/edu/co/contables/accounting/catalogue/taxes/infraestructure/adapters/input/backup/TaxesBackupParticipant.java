package co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.backup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.entity.TaxEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.repository.ITaxRepository;
import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * @brief Participante {@code taxes} (order 40) de la copia completa: los
 * impuestos ({@code Tax}) de la empresa.
 *
 * Export: {@code taxes/taxes.ndjson} paginado por id (keyset). Las cuentas de
 * IVA ventas/compras viajan como id VIEJO de cuenta del PUC.
 *
 * Restore: PKs nuevas, {@code idEnterprise} = empresa destino y las FKs
 * {@code salesTax}/{@code purchaseTax} remapeadas con
 * {@code idMap.require(ACCOUNT, ...)} (un {@code null} se respeta; un id que no
 * está en la copia es error y rollback). Registra {@code TAX} viejo→nuevo, que
 * consumen las categorías de productos. {@code usageCount} se reinicia a 0
 * (los documentos que lo incrementan no viajan hasta la fase 3).
 *
 * Escribe por repositorio: no pasa por {@code TaxCreateService} (auditoría).
 */
@Component
@RequiredArgsConstructor
public class TaxesBackupParticipant implements BackupParticipant {

    public static final int SCHEMA_VERSION = 1;
    public static final String ENTRY_TAXES = "taxes/taxes.ndjson";

    private static final int BATCH = 500;

    private final ITaxRepository repository;
    private final EntityManager entityManager;

    /** Registro NDJSON de un impuesto; {@code salesAccountId}/{@code purchaseAccountId} son ids VIEJOS de cuenta. */
    public record TaxRow(
            Long id,
            String code,
            String description,
            Double interest,
            Long salesAccountId,
            Long purchaseAccountId,
            Boolean status) {
    }

    @Override
    public String moduleKey() {
        return BackupKeys.MODULE_TAXES;
    }

    @Override
    public int order() {
        return BackupKeys.ORDER_TAXES;
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public Set<String> requires() {
        return Set.of(BackupKeys.MODULE_ENTERPRISE, BackupKeys.MODULE_PUC);
    }

    /* ------------------------------------------------------------------ */
    /* Export                                                             */
    /* ------------------------------------------------------------------ */

    @Override
    public void export(UUID enterpriseId, BackupWriter writer) {
        writer.writeNdjson(ENTRY_TAXES, new TaxIterator(enterpriseId.toString()));
    }

    /** Recorre los impuestos de la empresa por páginas de id ascendente (keyset). */
    private final class TaxIterator implements Iterator<TaxRow> {
        private final String enterprise;
        private long lastId = 0L;
        private Iterator<TaxEntity> page = List.<TaxEntity>of().iterator();
        private boolean exhausted;

        TaxIterator(String enterprise) {
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
            List<TaxEntity> next = repository.findBackupBatch(enterprise, lastId, PageRequest.of(0, BATCH));
            if (next.size() < BATCH) {
                exhausted = true;
            }
            if (next.isEmpty()) {
                return false;
            }
            lastId = next.get(next.size() - 1).getId();
            page = next.iterator();
            return true;
        }

        @Override
        public TaxRow next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            TaxEntity t = page.next();
            return new TaxRow(t.getId(), t.getCode(), t.getDescription(), t.getInterest(),
                    t.getSalesTax() != null ? t.getSalesTax().getId() : null,
                    t.getPurchaseTax() != null ? t.getPurchaseTax().getId() : null,
                    t.getStatus());
        }
    }

    /* ------------------------------------------------------------------ */
    /* Restore                                                            */
    /* ------------------------------------------------------------------ */

    @Override
    public void restore(BackupReader reader, IdMap idMap, RestoreContext ctx) {
        if (!reader.has(ENTRY_TAXES)) {
            return;
        }
        String enterprise = ctx.targetEnterpriseIdAsString();
        reader.forEachBatch(ENTRY_TAXES, TaxRow.class, BATCH, rows -> {
            List<TaxEntity> entities = new ArrayList<>(rows.size());
            for (TaxRow r : rows) {
                Long salesId = idMap.requireNullable(BackupKeys.ACCOUNT, r.salesAccountId());
                Long purchaseId = idMap.requireNullable(BackupKeys.ACCOUNT, r.purchaseAccountId());
                entities.add(TaxEntity.builder()
                        .code(r.code())
                        .description(r.description())
                        .interest(r.interest())
                        .salesTax(salesId == null ? null
                                : entityManager.getReference(AccountCatalogueEntity.class, salesId))
                        .purchaseTax(purchaseId == null ? null
                                : entityManager.getReference(AccountCatalogueEntity.class, purchaseId))
                        .idEnterprise(enterprise)
                        .status(r.status() == null ? Boolean.TRUE : r.status())
                        .usageCount(0)
                        .build());
            }
            repository.saveAll(entities);
            for (int i = 0; i < rows.size(); i++) {
                idMap.put(BackupKeys.TAX, rows.get(i).id(), entities.get(i).getId());
            }
            entityManager.flush();
            entityManager.clear();
        });
    }

    /* ------------------------------------------------------------------ */
    /* Purge (restore en el lugar)                                        */
    /* ------------------------------------------------------------------ */

    /** Borra los impuestos de la empresa (van antes que las cuentas del PUC, a las que apuntan). */
    @Override
    public void purge(UUID enterpriseId) {
        repository.deleteAllByEnterprise(enterpriseId.toString());
        entityManager.flush();
        entityManager.clear();
    }

    /* ------------------------------------------------------------------ */
    /* Verificación                                                       */
    /* ------------------------------------------------------------------ */

    @Override
    public Map<String, Long> count(UUID enterpriseId) {
        return Map.of(ENTRY_TAXES, repository.countByIdEnterprise(enterpriseId.toString()));
    }

    @Override
    public List<String> verifyIntegrity(UUID enterpriseId) {
        List<String> problems = new ArrayList<>();
        long orphans = repository.countWithAccountOutsideEnterprise(enterpriseId.toString());
        if (orphans > 0) {
            problems.add(orphans + " impuestos referencian una cuenta de IVA que no es de la empresa destino");
        }
        return problems;
    }
}
