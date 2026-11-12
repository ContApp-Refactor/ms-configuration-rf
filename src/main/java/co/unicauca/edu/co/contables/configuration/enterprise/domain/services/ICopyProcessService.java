package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import co.unicauca.edu.co.contables.commons.backup.RestoreActor;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.CopyProcessTipoEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.CopyProcess;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.services.coordination.BackupModuleInfo;

/**
 * @brief Puerto de los procesos de copia (BACKUP/DUPLICATE/RESTORE)
 * orientados al frontend (Hito 6).
 *
 * Crea un registro {@link CopyProcess}, delega el trabajo en el coordinador
 * de la copia completa y lo deja en estado terminal (COMPLETADO/ERROR), de
 * forma síncrona. Las validaciones del ZIP (estructura, checksums, versión,
 * módulos, NIT repetido) se hacen ANTES de crear el registro y salen como
 * error HTTP (4xx); lo que falla dentro de la transacción del restore deja el
 * proceso en ERROR con {@code errorResumen} legible.
 */
public interface ICopyProcessService {

    /**
     * Opciones de un restore pedido por el front.
     *
     * @param empresaDestino  id (UUID) de la empresa a sobrescribir si {@code inplace};
     *                        si no, el nombre de la empresa nueva (un UUID se ignora como nombre)
     * @param nombreDestino   nombre de la empresa nueva (gana sobre {@code empresaDestino})
     * @param nitDestino      NIT de la empresa nueva (D3)
     * @param actor           tenant y usuario capturados en el hilo de la petición
     * @param iniciadoPor     nombre visible de quien inicia el proceso
     */
    record RestoreRequest(String empresaDestino, boolean inplace, String nombreDestino, String nitDestino,
            boolean skipUnknownModules, RestoreActor actor, String iniciadoPor) {
    }

    /** Lista todos los procesos, más reciente primero. */
    List<CopyProcess> findAll();

    /** Obtiene un proceso por su id. */
    CopyProcess findById(UUID idProceso);

    /**
     * Crea y ejecuta un proceso BACKUP o DUPLICATE (un solo proceso con estado
     * y progreso por módulo; DUPLICATE no deja ZIP).
     *
     * @param nombreDestino nombre de la empresa destino; requerido para DUPLICATE
     */
    CopyProcess create(CopyProcessTipoEnum tipo, UUID empresaOrigen, String nombreDestino, RestoreActor actor,
            String iniciadoPor);

    /** Carga el ZIP de backup asociado a un proceso, para descarga. */
    Resource loadBackupFile(UUID idProceso);

    /**
     * Elimina el registro del proceso (y su backup si ningún otro proceso lo
     * usa). Lanza {@code CopyProcessNotTerminalException} si no está en estado terminal.
     */
    void delete(UUID idProceso);

    /**
     * RESTORE desde un {@code backupRef} existente: solo se acepta si pertenece
     * a un proceso del tenant actual (si no, 404).
     */
    CopyProcess restoreFromBackupRef(String backupRef, RestoreRequest request);

    /** RESTORE desde un ZIP subido: se guarda bajo el tenant actual y queda como {@code backupRef} del proceso. */
    CopyProcess restoreFromUpload(MultipartFile file, RestoreRequest request);

    /** Módulos que esta versión exporta/restaura. */
    List<BackupModuleInfo> supportedModules();
}
