package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import co.unicauca.edu.co.contables.commons.backup.RestoreActor;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.CopyProcessEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.CopyProcessDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.CopyProcessRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.dto.BackupReferenceDto;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.CopyProcessEstadoEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.CopyProcessTipoEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.CopyProcess;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.BackupModuleInfo;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.BackupProgressListener;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.IEnterpriseBackupCoordinator;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.PreparedRestore;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.RestoreOptions;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.RestoreResult;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.BackupNotFoundException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.CopyProcessNotFoundException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.CopyProcessNotTerminalException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.InvalidRestoreRequestException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.RestoreFailedException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.RestoreInPlaceConflictException;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Procesos de copia (BACKUP/DUPLICATE/RESTORE) orientados al frontend.
 *
 * NO es {@code @Transactional}: cada {@code repository.save} del registro se
 * confirma solo, así el estado ERROR sobrevive al rollback del restore. El
 * avance por módulo se guarda en una transacción {@code REQUIRES_NEW} (el
 * restore corre dentro de la suya) para que un polling concurrente lo vea.
 *
 * Todo es síncrono. Si algún día se pasa a {@code @Async}: el
 * {@link RestoreActor} ya llega capturado desde el controlador (lección
 * DEF-06: el SecurityContext no viaja al hilo del pool).
 *
 * Fases ({@code faseActual}, las que ya usa el front): 1 creado, 2 export,
 * 3 restore/verificación, 4 terminado.
 */
@Service
@Slf4j
public class CopyProcessServiceImpl implements ICopyProcessService {

    private final CopyProcessRepository repository;
    private final CopyProcessDataMapper mapper;
    private final IEnterpriseBackupService backupService;
    private final IEnterpriseRestoreService restoreService;
    private final IEnterpriseBackupCoordinator coordinator;
    private final TransactionTemplate progressTx;

    public CopyProcessServiceImpl(CopyProcessRepository repository, CopyProcessDataMapper mapper,
            IEnterpriseBackupService backupService, IEnterpriseRestoreService restoreService,
            IEnterpriseBackupCoordinator coordinator, PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.mapper = mapper;
        this.backupService = backupService;
        this.restoreService = restoreService;
        this.coordinator = coordinator;
        this.progressTx = new TransactionTemplate(transactionManager);
        this.progressTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public List<CopyProcess> findAll() {
        return repository.findAllByOrderByIniciadoEnDesc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public CopyProcess findById(UUID idProceso) {
        return mapper.toDomain(findEntityOrThrow(idProceso));
    }

    @Override
    public List<BackupModuleInfo> supportedModules() {
        return coordinator.supportedModules();
    }

    /* ------------------------------------------------------------------ */
    /* BACKUP / DUPLICATE                                                 */
    /* ------------------------------------------------------------------ */

    @Override
    public CopyProcess create(CopyProcessTipoEnum tipo, UUID empresaOrigen, String nombreDestino, RestoreActor actor,
            String iniciadoPor) {
        if (tipo == null || empresaOrigen == null) {
            throw new InvalidRestoreRequestException("Debe indicar 'tipo' y 'empresaOrigen'");
        }
        if (tipo == CopyProcessTipoEnum.RESTORE) {
            throw new InvalidRestoreRequestException(
                    "Un proceso RESTORE se crea mediante /api/enterprises/copy/restore, no aquí");
        }
        if (tipo == CopyProcessTipoEnum.DUPLICATE && (nombreDestino == null || nombreDestino.isBlank())) {
            throw new InvalidRestoreRequestException(
                    "DUPLICATE requiere 'empresaDestino.nombre' con el nombre de la empresa nueva");
        }
        if (tipo == CopyProcessTipoEnum.DUPLICATE && actor == null) {
            throw new InvalidRestoreRequestException("No hay un usuario autenticado para duplicar la empresa");
        }

        CopyProcessEntity entity = repository.save(CopyProcessEntity.builder()
                .tipo(tipo)
                .estado(CopyProcessEstadoEnum.EN_PROCESO)
                .empresaOrigen(empresaOrigen)
                .empresaDestino(tipo == CopyProcessTipoEnum.DUPLICATE ? nombreDestino : null)
                .iniciadoPor(iniciadoPor)
                .iniciadoEn(LocalDateTime.now())
                .snapshotCorte(LocalDateTime.now())
                .faseActual(1)
                .progreso(0)
                .build());

        try {
            ProcessProgress progress = new ProcessProgress(entity);
            if (tipo == CopyProcessTipoEnum.BACKUP) {
                BackupReferenceDto reference = backupService.backup(empresaOrigen, iniciadoPor, progress);
                entity.setBackupRef(reference.getBackupId());
                entity.setSnapshotCorte(reference.getCreatedAt());
            } else {
                // Un solo proceso: export a temporal + restore DUPLICATE en el coordinador.
                RestoreResult result = coordinator.duplicate(empresaOrigen, nombreDestino, actor, iniciadoPor,
                        progress);
                entity.setEmpresaDestino(result.enterpriseId().toString());
            }
            markAsCompleted(entity);
        } catch (RuntimeException e) {
            markAsError(entity, e);
        }
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Resource loadBackupFile(UUID idProceso) {
        CopyProcessEntity entity = findEntityOrThrow(idProceso);
        if (entity.getBackupRef() == null || entity.getBackupRef().isBlank()) {
            throw new BackupNotFoundException(idProceso.toString());
        }
        return backupService.loadBackupFile(entity.getBackupRef());
    }

    @Override
    public void delete(UUID idProceso) {
        CopyProcessEntity entity = findEntityOrThrow(idProceso);
        if (!isTerminal(entity.getEstado())) {
            throw new CopyProcessNotTerminalException(idProceso);
        }
        String ref = entity.getBackupRef();
        // Un RESTORE desde backupRef comparte el ZIP del BACKUP: solo se borra cuando nadie más lo usa.
        if (ref != null && !ref.isBlank() && repository.countByBackupRef(ref) <= 1) {
            try {
                backupService.deleteBackup(ref);
            } catch (RuntimeException e) {
                log.warn("No se pudo eliminar el backup '{}' del proceso de copia '{}'", ref, idProceso, e);
            }
        }
        repository.delete(entity);
    }

    /* ------------------------------------------------------------------ */
    /* RESTORE                                                            */
    /* ------------------------------------------------------------------ */

    @Override
    public CopyProcess restoreFromBackupRef(String backupRef, RestoreRequest request) {
        if (backupRef == null || backupRef.isBlank() || request == null || isBlank(request.empresaDestino())) {
            throw new InvalidRestoreRequestException("Debe indicar 'backupRef' y 'empresaDestino'");
        }
        // T5: el backupRef debe pertenecer a un proceso del tenant actual (consulta filtrada por @TenantId).
        if (!repository.existsByBackupRef(backupRef)) {
            throw new BackupNotFoundException(backupRef);
        }
        Path zip = backupService.resolveBackupFile(backupRef);
        String name = firstNonBlank(request.nombreDestino(), isUuid(request.empresaDestino()) ? null
                : request.empresaDestino());
        return runRestore(zip, backupRef, request, name);
    }

    @Override
    public CopyProcess restoreFromUpload(MultipartFile file, RestoreRequest request) {
        if (file == null || file.isEmpty() || request == null || isBlank(request.empresaDestino())) {
            throw new InvalidRestoreRequestException("Debe indicar 'file' y 'empresaDestino'");
        }
        // Se valida el destino in-place antes de guardar nada.
        if (request.inplace()) {
            parseUuidOrThrow(request.empresaDestino());
        }
        BackupReferenceDto stored = backupService.storeUpload(file);
        try {
            return runRestore(backupService.resolveBackupFile(stored.getBackupId()), stored.getBackupId(), request,
                    request.nombreDestino());
        } catch (RuntimeException e) {
            // Rechazado antes de crear el proceso: el ZIP subido no queda registrado.
            if (!repository.existsByBackupRef(stored.getBackupId())) {
                backupService.deleteBackup(stored.getBackupId());
            }
            throw e;
        }
    }

    private CopyProcess runRestore(Path zip, String backupRef, RestoreRequest request, String targetName) {
        RestoreOptions options;
        if (request.inplace()) {
            UUID target = parseUuidOrThrow(request.empresaDestino());
            // ZIP v1 (solo datos de la empresa): lógica histórica. v2: purga + restore en el coordinador (D4).
            if (restoreService.formatVersion(zip) == 1) {
                return runInPlaceV1(zip, backupRef, request, target);
            }
            options = RestoreOptions.inPlace(target, request.nombreDestino(), request.nitDestino(),
                    request.skipUnknownModules());
        } else {
            options = RestoreOptions.newEnterprise(targetName, request.nitDestino(), request.skipUnknownModules());
        }
        if (request.actor() == null) {
            throw new InvalidRestoreRequestException("No hay un usuario autenticado para restaurar la copia");
        }
        // prepare: checksums, versión, módulos y NIT ANTES de crear el proceso → errores 4xx al front.
        try (PreparedRestore prepared = coordinator.prepare(zip, options, request.actor())) {
            CopyProcessEntity entity = startRestoreProcess(prepared.sourceEnterpriseId(), prepared.exportedAt(),
                    backupRef, firstNonBlank(targetName, request.empresaDestino()), request.iniciadoPor());
            try {
                entity.setFaseActual(3);
                RestoreResult result = coordinator.execute(prepared, new ProcessProgress(entity));
                entity.setEmpresaDestino(result.enterpriseId().toString());
                markAsCompleted(entity);
            } catch (RuntimeException e) {
                markAsError(entity, e);
            }
            return mapper.toDomain(repository.save(entity));
        }
    }

    /** ZIP v1 en el lugar: la lógica histórica vive en {@link IEnterpriseRestoreService}. */
    private CopyProcess runInPlaceV1(Path zip, String backupRef, RestoreRequest request, UUID target) {
        BackupManifest manifest = restoreService.validateInPlace(zip, target);
        CopyProcessEntity entity = startRestoreProcess(manifest.getSourceEnterpriseId(), manifest.getExportedAt(),
                backupRef, request.empresaDestino(), request.iniciadoPor());
        try {
            entity.setFaseActual(2);
            UUID resultId;
            try {
                resultId = restoreService.restoreInPlace(zip, target);
            } catch (RestoreFailedException e) {
                // En inplace una violación de integridad es casi siempre que no se pudo sobrescribir (409).
                throw new RestoreInPlaceConflictException(e.getUserMessage(), e);
            }
            entity.setEmpresaDestino(resultId.toString());
            markAsCompleted(entity);
        } catch (RuntimeException e) {
            markAsError(entity, e);
        }
        return mapper.toDomain(repository.save(entity));
    }

    private CopyProcessEntity startRestoreProcess(UUID origen, LocalDateTime snapshot, String backupRef,
            String empresaDestino, String iniciadoPor) {
        return repository.save(CopyProcessEntity.builder()
                .tipo(CopyProcessTipoEnum.RESTORE)
                .estado(CopyProcessEstadoEnum.EN_PROCESO)
                .empresaOrigen(origen)
                .empresaDestino(empresaDestino)
                .backupRef(backupRef)
                .iniciadoPor(iniciadoPor)
                .iniciadoEn(LocalDateTime.now())
                .snapshotCorte(snapshot)
                .faseActual(1)
                .progreso(0)
                .build());
    }

    /* ------------------------------------------------------------------ */
    /* Progreso por módulo                                                */
    /* ------------------------------------------------------------------ */

    /**
     * Refleja el avance del coordinador en el registro y lo persiste en
     * {@code REQUIRES_NEW}. Nunca lanza: un fallo al guardar el progreso no
     * debe abortar la copia.
     */
    private final class ProcessProgress implements BackupProgressListener {
        private final CopyProcessEntity entity;
        private final List<String> completed = new ArrayList<>();
        private Stage stage;
        private int total;

        ProcessProgress(CopyProcessEntity entity) {
            this.entity = entity;
        }

        @Override
        public void onStage(Stage stage, int totalModules) {
            this.stage = stage;
            this.total = totalModules;
            completed.clear();
            entity.setFaseActual(stage == Stage.EXPORT ? 2 : 3);
            entity.setModulosTotales(totalModules);
            entity.setModulosCompletados(null);
            entity.setModuloActual(null);
            entity.setProgreso(percent());
            persist();
        }

        @Override
        public void onModuleStarted(Stage stage, String moduleKey) {
            entity.setModuloActual(moduleKey);
            persist();
        }

        @Override
        public void onModuleCompleted(Stage stage, String moduleKey) {
            completed.add(moduleKey);
            entity.setModulosCompletados(String.join(",", completed));
            entity.setProgreso(percent());
            persist();
        }

        /** Tramos: BACKUP export 5-95; RESTORE restore 5-90 + verificación 90-99; DUPLICATE export 5-45, restore 45-90. */
        private int percent() {
            double done = total == 0 ? 1.0 : (double) completed.size() / total;
            boolean duplicate = entity.getTipo() == CopyProcessTipoEnum.DUPLICATE;
            int[] range = switch (stage) {
                case EXPORT -> duplicate ? new int[] { 5, 45 } : new int[] { 5, 95 };
                case RESTORE -> duplicate ? new int[] { 45, 90 } : new int[] { 5, 90 };
                case VERIFY -> new int[] { 90, 99 };
            };
            return (int) Math.round(range[0] + (range[1] - range[0]) * done);
        }

        private void persist() {
            if (entity.getIdProceso() == null) {
                return;
            }
            try {
                progressTx.executeWithoutResult(status -> repository.save(entity));
            } catch (RuntimeException e) {
                log.warn("No se pudo guardar el avance del proceso de copia '{}'", entity.getIdProceso(), e);
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* Utilidades                                                         */
    /* ------------------------------------------------------------------ */

    private UUID parseUuidOrThrow(String empresaDestino) {
        try {
            return UUID.fromString(empresaDestino);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidRestoreRequestException(
                    "inplace=true requiere que 'empresaDestino' sea el id (UUID) de la empresa a sobrescribir");
        }
    }

    private static boolean isUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException | NullPointerException e) {
            return false;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String firstNonBlank(String a, String b) {
        return !isBlank(a) ? a : (!isBlank(b) ? b : null);
    }

    private void markAsCompleted(CopyProcessEntity entity) {
        entity.setEstado(CopyProcessEstadoEnum.COMPLETADO);
        entity.setFaseActual(4);
        entity.setProgreso(100);
        entity.setModuloActual(null);
        entity.setFinalizadoEn(LocalDateTime.now());
        entity.setErrorResumen(null);
    }

    private void markAsError(CopyProcessEntity entity, RuntimeException e) {
        log.error("El proceso de copia '{}' (tipo {}) terminó en error", entity.getIdProceso(), entity.getTipo(), e);
        entity.setEstado(CopyProcessEstadoEnum.ERROR);
        entity.setFinalizadoEn(LocalDateTime.now());
        entity.setErrorResumen(truncate(userMessage(e), 2000));
    }

    /** Texto legible para el front (sin el detalle técnico de la BD). */
    static String userMessage(RuntimeException e) {
        if (e instanceof RestoreFailedException rf) {
            return rf.getUserMessage();
        }
        if (e instanceof RestoreInPlaceConflictException && e.getMessage() != null) {
            return e.getMessage();
        }
        return e.getMessage() != null ? e.getMessage() : "Error inesperado (" + e.getClass().getSimpleName() + ")";
    }

    private static String truncate(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }

    private boolean isTerminal(CopyProcessEstadoEnum estado) {
        return estado == CopyProcessEstadoEnum.COMPLETADO || estado == CopyProcessEstadoEnum.ERROR
                || estado == CopyProcessEstadoEnum.CANCELADO;
    }

    private CopyProcessEntity findEntityOrThrow(UUID idProceso) {
        return repository.findById(idProceso).orElseThrow(() -> new CopyProcessNotFoundException(idProceso));
    }
}
