package co.unicauca.edu.co.contables.configuration.enterprise.domain.services;

import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.CopyProcessTipoEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.CopyProcess;

/**
 * @brief Puerto de salida para los procesos de copia (BACKUP/DUPLICATE/
 * RESTORE) orientados al frontend (Hito 6).
 *
 * A diferencia de {@code IEnterpriseBackupService}/
 * {@code IEnterpriseRestoreService}/{@code IEnterpriseDuplicateService}
 * (que hacen el trabajo real de export/import), este servicio es la capa de
 * orquestación: crea un registro {@link CopyProcess}, delega en esos
 * servicios y lo deja en estado terminal (COMPLETADO/ERROR), todo de forma
 * síncrona (sin hilos en segundo plano).
 */
public interface ICopyProcessService {

    /** Lista todos los procesos, más reciente primero. */
    List<CopyProcess> findAll();

    /** Obtiene un proceso por su id. */
    CopyProcess findById(UUID idProceso);

    /**
     * Crea y ejecuta de forma síncrona un proceso BACKUP o DUPLICATE.
     *
     * @param nombreDestino nombre de la empresa destino; solo aplica (y es
     * requerido) para DUPLICATE, se ignora para BACKUP.
     */
    CopyProcess create(CopyProcessTipoEnum tipo, UUID empresaOrigen, String nombreDestino, String iniciadoPor);

    /** Carga el ZIP de backup asociado a un proceso, para descarga. */
    Resource loadBackupFile(UUID idProceso);

    /**
     * Elimina el registro del proceso (y, si tiene, su backup asociado).
     * Lanza {@code CopyProcessNotTerminalException} si el proceso no está en
     * un estado terminal.
     */
    void delete(UUID idProceso);

    /** Crea y ejecuta de forma síncrona un proceso RESTORE desde un backupRef existente. */
    CopyProcess restoreFromBackupRef(String backupRef, String empresaDestino, boolean inplace, String iniciadoPor);

    /** Crea y ejecuta de forma síncrona un proceso RESTORE desde un archivo subido. */
    CopyProcess restoreFromUpload(MultipartFile file, String empresaDestino, boolean inplace, String nombreDestino,
            String iniciadoPor);
}
