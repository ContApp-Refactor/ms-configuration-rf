package co.unicauca.edu.co.contables.configuration.thirds.application.service.importExport;

import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.ExportJobStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @brief Servicio para rastrear el estado de trabajos de exportación asíncronos
 *
 * Gestiona el ciclo de vida de los trabajos de exportación en memoria,
 * permitiendo crear, actualizar y consultar el estado de las exportaciones en progreso.
 */
@Slf4j
@Service
public class ExportJobTracker {

    private final Map<String, ExportJobStatus> jobStatuses = new ConcurrentHashMap<>();

    /**
     * @brief Crea un nuevo trabajo de exportación
     * @param entId identificador de la entidad
     * @param fileName nombre del archivo a generar
     * @return jobId único generado para este trabajo
     */
    public String createJob(String entId, String fileName) {
        String jobId = UUID.randomUUID().toString();
        ExportJobStatus jobStatus = ExportJobStatus.builder()
                .jobId(jobId)
                .entId(entId)
                .fileName(fileName)
                .status(ImportStatus.PENDING)
                .startTime(LocalDateTime.now())
                .progress(0)
                .build();
        jobStatuses.put(jobId, jobStatus);
        return jobId;
    }

    /**
     * @brief Obtiene el estado actual de un trabajo de exportación
     * @param jobId identificador del trabajo
     * @return Optional con el estado del trabajo si existe
     */
    public Optional<ExportJobStatus> getJobStatus(String jobId) {
        return Optional.ofNullable(jobStatuses.get(jobId));
    }

    /**
     * @brief Actualiza el estado de un trabajo
     * @param jobId identificador del trabajo
     * @param status nuevo estado
     */
    public void updateJobStatus(String jobId, ImportStatus status) {
        getJobStatus(jobId).ifPresent(job -> {
            job.setStatus(status);
            if (status == ImportStatus.COMPLETED || status == ImportStatus.FAILED) {
                job.setEndTime(LocalDateTime.now());
            }
        });
    }

    /**
     * @brief Actualiza el progreso de un trabajo
     * @param jobId identificador del trabajo
     * @param progress progreso del 0 al 100
     */
    public void updateProgress(String jobId, Integer progress) {
        getJobStatus(jobId).ifPresent(job -> {
            job.setProgress(progress);
        });
    }

    /**
     * @brief Actualiza el total de registros a exportar
     * @param jobId identificador del trabajo
     * @param totalRecords total de registros
     */
    public void updateTotalRecords(String jobId, Integer totalRecords) {
        getJobStatus(jobId).ifPresent(job -> {
            job.setTotalRecords(totalRecords);
        });
    }

    /**
     * @brief Almacena los datos del archivo generado
     * @param jobId identificador del trabajo
     * @param fileData datos del archivo en bytes
     */
    public void setFileData(String jobId, byte[] fileData) {
        getJobStatus(jobId).ifPresent(job -> {
            job.setFileData(fileData);
        });
    }

    /**
     * @brief Almacena un mensaje de error
     * @param jobId identificador del trabajo
     * @param errorMessage mensaje de error
     */
    public void setErrorMessage(String jobId, String errorMessage) {
        getJobStatus(jobId).ifPresent(job -> {
            job.setErrorMessage(errorMessage);
        });
    }

    /**
     * @brief Elimina un trabajo del tracker (para liberar memoria)
     * @param jobId identificador del trabajo a eliminar
     */
    public void removeJob(String jobId) {
        jobStatuses.remove(jobId);
    }
}

