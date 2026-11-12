package co.unicauca.edu.co.contables.configuration.enterprise.domain.services.backup;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import co.unicauca.edu.co.contables.commons.backup.BackupKeys;
import co.unicauca.edu.co.contables.commons.backup.BackupParticipant;
import co.unicauca.edu.co.contables.commons.backup.BackupReader;
import co.unicauca.edu.co.contables.commons.backup.BackupWriter;
import co.unicauca.edu.co.contables.commons.backup.IdMap;
import co.unicauca.edu.co.contables.commons.backup.RestoreContext;
import co.unicauca.edu.co.contables.commons.backup.RestoreMode;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.EnterpriseRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest.EnterpriseSnapshot;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IEnterpriseService;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.ILogoStorageService;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.IPersonTypeService;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.EnterpriseNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Participante {@code enterprise} (order 0) de la copia completa.
 *
 * Export: {@code enterprise/enterprise.json} (empresa + tipo de persona +
 * ubicación + ids de catálogos seed + códigos de materias, igual que el ZIP v1)
 * y, si el logo es local y existe, {@code assets/logo} con su content-type.
 *
 * Restore IN_PLACE: actualiza la empresa existente (ver {@link #purge(UUID)} y
 * el método de restore en el lugar).
 *
 * Restore (NEW_ENTERPRISE/DUPLICATE): crea una empresa nueva (UUID nuevo; tenant e
 * {@code idUser} del usuario que restaura; nombre/NIT del contexto o de la
 * copia; método de inventario y tipos se conservan). Las materias se
 * resuelven por código en el tenant destino y las que no existen se omiten
 * con advertencia. El logo se escribe a disco recién en {@code afterCommit}
 * con clave NUEVA. Al terminar registra
 * {@code IdMap[ENTERPRISE][ctx.sourceEnterpriseId()] = nuevoId}: de ahí el
 * coordinador saca el {@code targetEnterpriseId} para los demás módulos.
 *
 * Usa {@code IEnterpriseService.create} porque no publica eventos ni audita.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EnterpriseBackupParticipant implements BackupParticipant {

    public static final int SCHEMA_VERSION = 1;

    private final EnterpriseRepository enterpriseRepository;
    private final IEnterpriseService enterpriseService;
    private final ILogoStorageService logoStorage;
    private final EnterpriseSnapshotSupport support;
    private final IPersonTypeService personTypeService;

    @Override
    public String moduleKey() {
        return BackupKeys.MODULE_ENTERPRISE;
    }

    @Override
    public int order() {
        return BackupKeys.ORDER_ENTERPRISE;
    }

    @Override
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public Set<String> requires() {
        return Set.of();
    }

    /* ------------------------------------------------------------------ */
    /* Export                                                             */
    /* ------------------------------------------------------------------ */

    @Override
    public void export(UUID enterpriseId, BackupWriter writer) {
        EnterpriseEntity entity = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new EnterpriseNotFoundException(enterpriseId));
        writer.json(EnterpriseSnapshotSupport.ENTRY_V2, support.toManifest(entity));
        exportLogo(entity.getLogo(), writer);
    }

    private void exportLogo(String logoUrl, BackupWriter writer) {
        String key = ILogoStorageService.keyFromUrl(logoUrl);
        if (key == null) {
            if (logoUrl != null && !logoUrl.isBlank()) {
                log.warn("El logo '{}' no es un archivo local; la copia no lo incluye", logoUrl);
            }
            return;
        }
        Optional<ILogoStorageService.LogoContent> logo = logoStorage.open(key);
        if (logo.isEmpty()) {
            log.warn("El logo '{}' no existe en disco; la copia se genera sin logo", key);
            return;
        }
        try (InputStream in = logo.get().content()) {
            writer.asset(EnterpriseSnapshotSupport.LOGO_ASSET, logo.get().contentType(), in);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo copiar el logo de la empresa", e);
        }
    }

    /* ------------------------------------------------------------------ */
    /* Restore                                                            */
    /* ------------------------------------------------------------------ */

    @Override
    public void preflight(BackupReader reader, RestoreContext ctx) {
        // El NIT no es único por regla de negocio (el alta normal tampoco lo valida):
        // solo se verifica que el payload de la empresa sea legible.
        EnterpriseSnapshotSupport.readPayload(reader).getEnterprise();
    }

    @Override
    public void restore(BackupReader reader, IdMap idMap, RestoreContext ctx) {
        if (ctx.mode() == RestoreMode.IN_PLACE) {
            restoreInPlace(reader, idMap, ctx);
            return;
        }
        BackupManifest payload = EnterpriseSnapshotSupport.readPayload(reader);
        EnterpriseSnapshot snapshot = payload.getEnterprise();
        String name = ctx.targetName() != null ? ctx.targetName() : snapshot.getName();
        String nit = effectiveNit(snapshot, ctx);

        Enterprise toCreate = support.toDomainEnterprise(snapshot, ctx.userId(), name, nit);
        toCreate.setLogo(restoreLogo(reader, snapshot));
        warnMissingSubjects(snapshot.getSubjectCodes());

        Enterprise created = enterpriseService.create(toCreate);
        idMap.put(BackupKeys.ENTERPRISE, ctx.sourceEnterpriseId(), created.getId());
    }

    /**
     * IN_PLACE (D4 actualizado): ACTUALIZA la empresa existente con el snapshot
     * (conserva id, tenant e {@code idUser}; reemplaza nombre, NIT, datos,
     * ubicación, tipo de persona, tipos, responsabilidades, materias, estado y
     * método de inventario). Las materias/responsabilidades ya fueron vaciadas
     * por {@link #purge(UUID)}. El logo nuevo se escribe en afterCommit y el
     * anterior se borra también en afterCommit (solo si nadie más lo usa).
     */
    private void restoreInPlace(BackupReader reader, IdMap idMap, RestoreContext ctx) {
        UUID target = ctx.requireTargetEnterpriseId();
        EnterpriseSnapshot snapshot = EnterpriseSnapshotSupport.readPayload(reader).getEnterprise();
        EnterpriseEntity current = enterpriseRepository.findById(target)
                .orElseThrow(() -> new EnterpriseNotFoundException(target));
        String oldLogo = current.getLogo();
        Long oldPersonTypeId = current.getPersonType() != null ? current.getPersonType().getId() : null;

        String name = ctx.targetName() != null ? ctx.targetName() : snapshot.getName();
        String nit = effectiveNit(snapshot, ctx);

        // idUser no cambia: la empresa sigue siendo de su dueño actual.
        Enterprise changes = support.toDomainEnterprise(snapshot, current.getIdUser(), name, nit);
        String newLogo = restoreLogo(reader, snapshot);
        changes.setLogo(newLogo);
        changes.setRemoveLogo(newLogo == null);
        warnMissingSubjects(snapshot.getSubjectCodes());

        enterpriseService.update(target, changes);
        if (snapshot.getState() != null) {
            enterpriseService.updateStatus(target, snapshot.getState());
        }
        if (oldPersonTypeId != null) {
            personTypeService.delete(oldPersonTypeId);
        }
        deleteOldLogoAfterCommit(oldLogo, newLogo, target);
        idMap.put(BackupKeys.ENTERPRISE, ctx.sourceEnterpriseId(), target);
    }

    private void deleteOldLogoAfterCommit(String oldLogoUrl, String newLogoUrl, UUID enterpriseId) {
        String oldKey = ILogoStorageService.keyFromUrl(oldLogoUrl);
        if (oldKey == null || oldLogoUrl.equals(newLogoUrl)
                || enterpriseRepository.existsByLogoAndIdNot(oldLogoUrl, enterpriseId)) {
            return;
        }
        Runnable delete = () -> {
            try {
                logoStorage.delete(oldKey);
            } catch (RuntimeException e) {
                log.warn("No se pudo borrar el logo anterior '{}' tras restaurar la empresa", oldKey, e);
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    delete.run();
                }
            });
        } else {
            delete.run();
        }
    }

    private static String effectiveNit(EnterpriseSnapshot snapshot, RestoreContext ctx) {
        return ctx.targetNit() != null ? ctx.targetNit() : snapshot.getNit();
    }

    /**
     * v2: el logo viaja como asset → clave nueva, escrita en afterCommit.
     * v1: solo trae la URL; si apunta a un logo local que existe en ESTE
     * entorno se copia con clave nueva (no se comparte la clave), si no se
     * restaura sin logo (nunca con la URL rota).
     */
    private String restoreLogo(BackupReader reader, EnterpriseSnapshot snapshot) {
        if (reader.hasAsset(EnterpriseSnapshotSupport.LOGO_ASSET)) {
            try (InputStream in = reader.openAsset(EnterpriseSnapshotSupport.LOGO_ASSET)) {
                String key = logoStorage.saveAfterCommit(in,
                        reader.assetContentType(EnterpriseSnapshotSupport.LOGO_ASSET));
                return ILogoStorageService.PUBLIC_URL_PREFIX + key;
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo leer el logo de la copia", e);
            }
        }
        String oldKey = ILogoStorageService.keyFromUrl(snapshot.getLogo());
        if (oldKey == null) {
            return null;
        }
        Optional<ILogoStorageService.LogoContent> existing = logoStorage.open(oldKey);
        if (existing.isEmpty()) {
            log.warn("La copia referencia el logo '{}', que no existe en este entorno; se restaura sin logo", oldKey);
            return null;
        }
        try (InputStream in = existing.get().content()) {
            return ILogoStorageService.PUBLIC_URL_PREFIX
                    + logoStorage.saveAfterCommit(in, existing.get().contentType());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo copiar el logo de la empresa", e);
        }
    }

    private void warnMissingSubjects(List<String> codes) {
        List<String> missing = support.missingSubjectCodes(codes);
        if (!missing.isEmpty()) {
            log.warn("Restore de empresa: se omiten las materias {} porque no existen en el tenant destino", missing);
        }
    }

    /* ------------------------------------------------------------------ */
    /* Purge (restore IN_PLACE)                                           */
    /* ------------------------------------------------------------------ */

    /**
     * NO borra el registro de la empresa (el restore lo actualiza). Solo vacía lo
     * propio que se restaura aparte: materias y responsabilidades tributarias
     * asociadas. Ubicación y tipo de persona se reemplazan en el restore.
     */
    @Override
    public void purge(UUID enterpriseId) {
        EnterpriseEntity entity = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new EnterpriseNotFoundException(enterpriseId));
        entity.setSubjects(new ArrayList<>());
        entity.setTaxLiabilities(new ArrayList<>());
        enterpriseRepository.save(entity);
    }

    /* ------------------------------------------------------------------ */
    /* Verificación                                                       */
    /* ------------------------------------------------------------------ */

    @Override
    public Map<String, Long> count(UUID enterpriseId) {
        long n = enterpriseRepository.existsById(enterpriseId) ? 1L : 0L;
        return Map.of(EnterpriseSnapshotSupport.ENTRY_V2, n, EnterpriseSnapshotSupport.ENTRY_V1, n);
    }

    @Override
    public List<String> verifyIntegrity(UUID enterpriseId) {
        List<String> problems = new ArrayList<>();
        if (!enterpriseRepository.existsById(enterpriseId)) {
            problems.add("la empresa restaurada " + enterpriseId + " no existe en el tenant destino");
        }
        return problems;
    }
}
