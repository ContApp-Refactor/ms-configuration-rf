package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.CopyProcessEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper.CopyProcessDataMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.CopyProcessRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.repository.EnterpriseRepository;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.dto.BackupReferenceDto;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.CopyProcessEstadoEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.CopyProcessTipoEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.BackupManifest;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.CopyProcess;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.BackupNotFoundException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.CopyProcessNotFoundException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.CopyProcessNotTerminalException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.CorruptBackupException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.InvalidBackupFileException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.InvalidRestoreRequestException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.RestoreFailedException;
import co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.RestoreInPlaceConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Implementación de la orquestación de procesos de copia
 * (BACKUP/DUPLICATE/RESTORE) orientados al frontend (Hito 6).
 *
 * Todo proceso se ejecuta de forma síncrona (sin hilos en segundo plano):
 * se crea el registro, se delega en {@code IEnterpriseBackupService}/
 * {@code IEnterpriseRestoreService} (el trabajo real, ya implementado por un
 * agente anterior) y se deja el registro en un estado terminal antes de
 * devolver la respuesta.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CopyProcessServiceImpl implements ICopyProcessService {

    private final CopyProcessRepository repository;
    private final CopyProcessDataMapper mapper;

    private final IEnterpriseBackupService backupService;
    private final IEnterpriseRestoreService restoreService;
    private final EnterpriseRepository enterpriseRepository;

    @Override
    public List<CopyProcess> findAll() {
        return repository.findAllByOrderByIniciadoEnDesc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public CopyProcess findById(UUID idProceso) {
        return mapper.toDomain(findEntityOrThrow(idProceso));
    }

    @Override
    public CopyProcess create(CopyProcessTipoEnum tipo, UUID empresaOrigen, String nombreDestino,
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

        CopyProcessEntity entity = CopyProcessEntity.builder()
                .tipo(tipo)
                .estado(CopyProcessEstadoEnum.EN_PROCESO)
                .empresaOrigen(empresaOrigen)
                .empresaDestino(tipo == CopyProcessTipoEnum.DUPLICATE ? nombreDestino : null)
                .iniciadoPor(iniciadoPor)
                .iniciadoEn(LocalDateTime.now())
                .snapshotCorte(LocalDateTime.now())
                .faseActual(1)
                .build();
        entity = repository.save(entity);

        try {
            if (tipo == CopyProcessTipoEnum.BACKUP) {
                runBackup(entity);
            } else {
                runDuplicate(entity, nombreDestino);
            }
        } catch (RuntimeException e) {
            markAsError(entity, e);
        }

        return mapper.toDomain(repository.save(entity));
    }

    private void runBackup(CopyProcessEntity entity) {
        entity.setFaseActual(2);
        BackupReferenceDto reference = backupService.backup(entity.getEmpresaOrigen());
        entity.setFaseActual(3);
        entity.setBackupRef(reference.getBackupId());
        entity.setFaseActual(4);
        markAsCompleted(entity);
    }

    private void runDuplicate(CopyProcessEntity entity, String nombreDestino) {
        entity.setFaseActual(2);
        BackupReferenceDto reference = backupService.backup(entity.getEmpresaOrigen());

        entity.setFaseActual(3);
        UUID newEnterpriseId;
        try {
            newEnterpriseId = restoreService.restoreFromBackupId(reference.getBackupId(), false, null);
            // El restore recrea la empresa con el mismo nombre que la
            // origen (viene así en el backup); el nombre de destino pedido
            // por el frontend se aplica aparte, directo sobre la fila
            // recién creada, para no duplicar la lógica de serialización
            // del ZIP solo para cambiar un campo.
            renameEnterprise(newEnterpriseId, nombreDestino);
        } finally {
            // El ZIP de backup es un artefacto intermedio de DUPLICATE (no uno
            // que el usuario pidió conservar), igual que en
            // EnterpriseDuplicateServiceImpl: se limpia siempre.
            try {
                backupService.deleteBackup(reference.getBackupId());
            } catch (RuntimeException cleanupError) {
                log.warn("No se pudo limpiar el backup temporal '{}' usado por el proceso de copia '{}'",
                        reference.getBackupId(), entity.getIdProceso(), cleanupError);
            }
        }

        entity.setFaseActual(4);
        entity.setEmpresaDestino(newEnterpriseId.toString());
        markAsCompleted(entity);
    }

    private void renameEnterprise(UUID enterpriseId, String nombreDestino) {
        EnterpriseEntity entity = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception.EnterpriseNotFoundException(
                        enterpriseId));
        entity.setName(nombreDestino);
        enterpriseRepository.save(entity);
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
        if (entity.getBackupRef() != null && !entity.getBackupRef().isBlank()) {
            try {
                backupService.deleteBackup(entity.getBackupRef());
            } catch (RuntimeException e) {
                log.warn("No se pudo eliminar el backup '{}' del proceso de copia '{}'", entity.getBackupRef(),
                        idProceso, e);
            }
        }
        repository.delete(entity);
    }

    @Override
    public CopyProcess restoreFromBackupRef(String backupRef, String empresaDestino, boolean inplace,
            String iniciadoPor) {
        if (backupRef == null || backupRef.isBlank() || empresaDestino == null || empresaDestino.isBlank()) {
            throw new InvalidRestoreRequestException("Debe indicar 'backupRef' y 'empresaDestino'");
        }
        BackupManifest manifest = readManifestOrThrow(() -> backupService.readManifest(backupRef));

        CopyProcessEntity entity = startRestoreProcess(manifest, backupRef, empresaDestino, inplace, iniciadoPor);
        try {
            runRestore(entity, () -> doRestore(backupRef, empresaDestino, inplace), empresaDestino, inplace);
        } catch (RuntimeException e) {
            markAsError(entity, e);
        }
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public CopyProcess restoreFromUpload(MultipartFile file, String empresaDestino, boolean inplace,
            String nombreDestino, String iniciadoPor) {
        if (file == null || file.isEmpty() || empresaDestino == null || empresaDestino.isBlank()) {
            throw new InvalidRestoreRequestException("Debe indicar 'file' y 'empresaDestino'");
        }
        byte[] zipBytes = readBytesOrThrow(file);
        BackupManifest manifest = readManifestOrThrow(() -> backupService.readManifest(zipBytes));

        String destinoFinal = (nombreDestino != null && !nombreDestino.isBlank()) ? nombreDestino : empresaDestino;
        CopyProcessEntity entity = startRestoreProcess(manifest, null, destinoFinal, inplace, iniciadoPor);
        try {
            runRestore(entity, () -> doRestoreFromFile(file, empresaDestino, inplace), destinoFinal, inplace);
        } catch (RuntimeException e) {
            markAsError(entity, e);
        }
        return mapper.toDomain(repository.save(entity));
    }

    private CopyProcessEntity startRestoreProcess(BackupManifest manifest, String backupRef, String empresaDestino,
            boolean inplace, String iniciadoPor) {
        CopyProcessEntity entity = CopyProcessEntity.builder()
                .tipo(CopyProcessTipoEnum.RESTORE)
                .estado(CopyProcessEstadoEnum.EN_PROCESO)
                .empresaOrigen(manifest.getSourceEnterpriseId())
                .empresaDestino(empresaDestino)
                .backupRef(backupRef)
                .iniciadoPor(iniciadoPor)
                .iniciadoEn(LocalDateTime.now())
                .snapshotCorte(manifest.getExportedAt())
                .faseActual(1)
                .build();
        return repository.save(entity);
    }

    @FunctionalInterface
    private interface RestoreAction {
        UUID run();
    }

    private void runRestore(CopyProcessEntity entity, RestoreAction action, String empresaDestino, boolean inplace) {
        entity.setFaseActual(inplace ? 2 : 3);
        try {
            UUID resultId = action.run();
            entity.setFaseActual(4);
            entity.setEmpresaDestino(resultId.toString());
            markAsCompleted(entity);
        } catch (RestoreFailedException e) {
            // Integrity violation durante restore: en inplace es casi
            // siempre que no se pudo sobrescribir la empresa destino
            // (409); fuera de inplace es simplemente data inválida (400,
            // se deja propagar tal cual con su propia anotación).
            if (inplace) {
                throw new RestoreInPlaceConflictException(e.getMessage(), e);
            }
            throw e;
        }
    }

    private UUID doRestore(String backupRef, String empresaDestino, boolean inplace) {
        UUID targetEnterpriseId = inplace ? parseUuidOrThrow(empresaDestino) : null;
        return restoreService.restoreFromBackupId(backupRef, inplace, targetEnterpriseId);
    }

    private UUID doRestoreFromFile(MultipartFile file, String empresaDestino, boolean inplace) {
        UUID targetEnterpriseId = inplace ? parseUuidOrThrow(empresaDestino) : null;
        return restoreService.restoreFromFile(file, inplace, targetEnterpriseId);
    }

    private UUID parseUuidOrThrow(String empresaDestino) {
        try {
            return UUID.fromString(empresaDestino);
        } catch (IllegalArgumentException e) {
            throw new InvalidRestoreRequestException(
                    "inplace=true requiere que 'empresaDestino' sea el id (UUID) de la empresa a sobrescribir");
        }
    }

    private BackupManifest readManifestOrThrow(java.util.function.Supplier<BackupManifest> reader) {
        try {
            return reader.get();
        } catch (InvalidBackupFileException e) {
            throw new CorruptBackupException(e.getMessage(), e);
        }
    }

    private byte[] readBytesOrThrow(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (java.io.IOException e) {
            throw new InvalidBackupFileException("No se pudo leer el archivo de backup subido", e);
        }
    }

    private void markAsCompleted(CopyProcessEntity entity) {
        entity.setEstado(CopyProcessEstadoEnum.COMPLETADO);
        entity.setFinalizadoEn(LocalDateTime.now());
        entity.setErrorResumen(null);
    }

    private void markAsError(CopyProcessEntity entity, RuntimeException e) {
        log.error("El proceso de copia '{}' (tipo {}) terminó en error", entity.getIdProceso(), entity.getTipo(), e);
        entity.setEstado(CopyProcessEstadoEnum.ERROR);
        entity.setFinalizadoEn(LocalDateTime.now());
        entity.setErrorResumen(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
    }

    private boolean isTerminal(CopyProcessEstadoEnum estado) {
        return estado == CopyProcessEstadoEnum.COMPLETADO || estado == CopyProcessEstadoEnum.ERROR
                || estado == CopyProcessEstadoEnum.CANCELADO;
    }

    private CopyProcessEntity findEntityOrThrow(UUID idProceso) {
        return repository.findById(idProceso).orElseThrow(() -> new CopyProcessNotFoundException(idProceso));
    }
}
