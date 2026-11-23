package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ImportStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.ImportErrorDetail;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.ImportJobStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @brief Servicio para rastrear el estado de trabajos de importación asíncronos
 *
 * Gestiona el ciclo de vida de los trabajos de importación de catálogo de cuentas en memoria,
 * permitiendo crear, actualizar y consultar el estado de las importaciones en progreso.
 */
@Slf4j
@Service
public class AccountCatalogueImportJobTracker {

    private final Map<String, ImportJobStatus> jobStatuses = new ConcurrentHashMap<>();

    /**
     * @brief Crea un nuevo trabajo de importación
     * @param entId identificador de la entidad
     * @param fileName nombre del archivo a importar
     * @return jobId único generado para este trabajo
     */
    public String createJob(String entId, String fileName) {
        String jobId = UUID.randomUUID().toString();
        ImportJobStatus jobStatus = ImportJobStatus.builder()
                .jobId(jobId)
                .entId(entId)
                .fileName(fileName)
                .status(ImportStatus.PENDING)
                .startTime(LocalDateTime.now())
                .progress(0)
                .totalRecords(0)
                .successfulImports(0)
                .failedImports(0)
                .duplicatesSkipped(0)
                .build();
        jobStatuses.put(jobId, jobStatus);
        return jobId;
    }

    /**
     * @brief Obtiene el estado actual de un trabajo de importación
     * @param jobId identificador del trabajo
     * @return Optional con el estado del trabajo si existe
     */
    public Optional<ImportJobStatus> getJobStatus(String jobId) {
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
            if (status == ImportStatus.COMPLETED || status == ImportStatus.COMPLETED_WITH_ERRORS ||
                status == ImportStatus.FAILED) {
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
     * @brief Actualiza las métricas del trabajo
     * @param jobId identificador del trabajo
     * @param totalRecords total de registros procesados
     * @param successfulImports importaciones exitosas
     * @param failedImports importaciones fallidas
     * @param duplicatesSkipped duplicados omitidos
     */
    public void updateJobMetrics(String jobId, Integer totalRecords, Integer successfulImports,
                                  Integer failedImports, Integer duplicatesSkipped) {
        getJobStatus(jobId).ifPresent(job -> {
            job.setTotalRecords(totalRecords);
            job.setSuccessfulImports(successfulImports);
            job.setFailedImports(failedImports);
            job.setDuplicatesSkipped(duplicatesSkipped);
        });
    }

    /**
     * @brief Agrega errores al trabajo
     * @param jobId identificador del trabajo
     * @param errors lista de errores a agregar
     */
    public void addErrors(String jobId, List<ImportErrorDetail> errors) {
        getJobStatus(jobId).ifPresent(job -> {
            job.getErrors().addAll(errors);
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

