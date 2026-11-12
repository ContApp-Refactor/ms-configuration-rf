package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.backup;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.repository.IAccountCatalogueRepository;
import co.unicauca.edu.co.contables.commons.backup.BackupIntegrityException;
import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * @brief Participante {@code puc} (order 30) de la copia completa: SOLO el
 * catálogo de cuentas ({@code Account}) de la empresa.
 *
 * Export: {@code puc/accounts.ndjson}, paginado por id (keyset) y sin entidades
 * JPA en el ZIP. Restore: PKs nuevas, {@code idEnterprise} = empresa destino
 * (el tenant lo pone {@code @TenantId}), y la jerarquía padre-hijo en DOS
 * pasadas (1: insertar todo sin padre y registrar {@code ACCOUNT} viejo→nuevo;
 * 2: asignar el padre ya remapeado), para no depender del orden del archivo ni
 * del {@code cascade = ALL} de {@code children}.
 *
 * D5: {@code amount} (saldo) y {@code usageCount} se reinician a 0 porque las
 * facturas y asientos que los alimentan no viajan hasta la fase 3. Bancos,
 * cuentas bancarias y medios de pago no se incluyen (fase 3).
 *
 * Escribe directo por repositorio: no pasa por servicios con {@code @Auditable}
 * ni por listeners de uso.
 */
@Component
@RequiredArgsConstructor
public class PucBackupParticipant implements BackupParticipant {

    public static final int SCHEMA_VERSION = 1;
    public static final String ENTRY_ACCOUNTS = "puc/accounts.ndjson";

    private static final int BATCH = 500;

    private final IAccountCatalogueRepository repository;
    private final EntityManager entityManager;

    /** Registro NDJSON de una cuenta. {@code parentId} es el id VIEJO del padre (mismo archivo). */
    public record AccountRow(
            Long id,
            String code,
            String description,
            NatureEnum nature,
            FinancialStatusEnum financialStatus,
            ClassificationEnum classification,
            Long parentId,
            Boolean crossing,
            Boolean costCenter,
            Boolean status) {
    }

    @Override
    public String moduleKey() {
        return BackupKeys.MODULE_PUC;
    }

    @Override
    public int order() {
        return BackupKeys.ORDER_PUC;
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public Set<String> requires() {
        return Set.of(BackupKeys.MODULE_ENTERPRISE);
    }

    /* ------------------------------------------------------------------ */
    /* Export                                                             */
    /* ------------------------------------------------------------------ */

    @Override
    public void export(UUID enterpriseId, BackupWriter writer) {
        writer.writeNdjson(ENTRY_ACCOUNTS, new AccountIterator(enterpriseId.toString()));
    }

    /** Recorre las cuentas de la empresa por páginas de id ascendente (keyset), sin cargar todo. */
    private final class AccountIterator implements Iterator<AccountRow> {
        private final String enterprise;
        private long lastId = 0L;
        private Iterator<AccountCatalogueEntity> page = List.<AccountCatalogueEntity>of().iterator();
        private boolean exhausted;

        AccountIterator(String enterprise) {
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
            List<AccountCatalogueEntity> next = repository.findBackupBatch(enterprise, lastId, PageRequest.of(0, BATCH));
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
        public AccountRow next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            AccountCatalogueEntity a = page.next();
            return new AccountRow(a.getId(), a.getCode(), a.getDescription(), a.getNature(),
                    a.getFinancialStatus(), a.getClassification(),
                    a.getParent() != null ? a.getParent().getId() : null,
                    a.getCrossing(), a.getCostCenter(), a.getStatus());
        }
    }

    /* ------------------------------------------------------------------ */
    /* Restore                                                            */
    /* ------------------------------------------------------------------ */

    @Override
    public void restore(BackupReader reader, IdMap idMap, RestoreContext ctx) {
        if (!reader.has(ENTRY_ACCOUNTS)) {
            return;
        }
        String enterprise = ctx.targetEnterpriseIdAsString();

        // Pasada 1: insertar sin padre; (idNuevo, idViejoDelPadre) para la pasada 2.
        List<long[]> parentLinks = new ArrayList<>();
        reader.forEachBatch(ENTRY_ACCOUNTS, AccountRow.class, BATCH, rows -> {
            List<AccountCatalogueEntity> entities = new ArrayList<>(rows.size());
            for (AccountRow r : rows) {
                if (r.parentId() != null && r.parentId().equals(r.id())) {
                    throw new BackupIntegrityException(BackupKeys.MODULE_PUC,
                            "la cuenta " + r.code() + " (id=" + r.id() + ") es su propio padre");
                }
                entities.add(AccountCatalogueEntity.builder()
                        .code(r.code())
                        .description(r.description())
                        .nature(r.nature())
                        .financialStatus(r.financialStatus())
                        .classification(r.classification())
                        .crossing(r.crossing())
                        .costCenter(r.costCenter())
                        .status(r.status() == null ? Boolean.TRUE : r.status())
                        .idEnterprise(enterprise)
                        .amount(BigDecimal.ZERO)
                        .usageCount(0)
                        .build());
            }
            repository.saveAll(entities);
            for (int i = 0; i < rows.size(); i++) {
                AccountRow r = rows.get(i);
                Long newId = entities.get(i).getId();
                idMap.put(BackupKeys.ACCOUNT, r.id(), newId);
                if (r.parentId() != null) {
                    parentLinks.add(new long[] { newId, r.parentId() });
                }
            }
            entityManager.flush();
            entityManager.clear();
        });

        // Pasada 2: padre remapeado (require: un padre fuera de la copia es un error).
        for (int from = 0; from < parentLinks.size(); from += BATCH) {
            List<long[]> chunk = parentLinks.subList(from, Math.min(from + BATCH, parentLinks.size()));
            Map<Long, Long> parentByChild = new java.util.HashMap<>();
            for (long[] link : chunk) {
                parentByChild.put(link[0], idMap.requireLong(BackupKeys.ACCOUNT, link[1]));
            }
            for (AccountCatalogueEntity child : repository.findAllById(parentByChild.keySet())) {
                child.setParent(repository.getReferenceById(parentByChild.get(child.getId())));
            }
            entityManager.flush();
            entityManager.clear();
        }
    }

    /* ------------------------------------------------------------------ */
    /* Purge (restore en el lugar)                                        */
    /* ------------------------------------------------------------------ */

    /** Borra las cuentas de la empresa: primero se rompe la auto-FK {@code parent}, luego un DELETE bulk. */
    @Override
    public void purge(UUID enterpriseId) {
        String enterprise = enterpriseId.toString();
        repository.clearParentByEnterprise(enterprise);
        repository.deleteAllByEnterprise(enterprise);
        entityManager.flush();
        entityManager.clear();
    }

    /* ------------------------------------------------------------------ */
    /* Verificación                                                       */
    /* ------------------------------------------------------------------ */

    @Override
    public Map<String, Long> count(UUID enterpriseId) {
        return Map.of(ENTRY_ACCOUNTS, repository.countByEnterprise(enterpriseId.toString()));
    }

    @Override
    public List<String> verifyIntegrity(UUID enterpriseId) {
        List<String> problems = new ArrayList<>();
        long orphans = repository.countWithParentOutsideEnterprise(enterpriseId.toString());
        if (orphans > 0) {
            problems.add(orphans + " cuentas del PUC referencian un padre que no es de la empresa destino");
        }
        return problems;
    }
}
