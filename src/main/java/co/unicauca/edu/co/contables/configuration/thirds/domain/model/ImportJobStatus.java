package co.unicauca.edu.co.contables.configuration.thirds.domain.model;

import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ImportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @brief Modelo de estado de una tarea de importación asíncrona
 *
 * Representa el estado completo de un job de importación en ejecución o finalizado,
 * incluyendo métricas, progreso y errores acumulados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportJobStatus {

    private String jobId;

    private String entId;

    private String fileName;

    private ImportStatus status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer totalRecords;

    private Integer successfulImports;

    private Integer failedImports;

    private Integer duplicatesSkipped;

    @Builder.Default
    private List<ImportErrorDetail> errors = new ArrayList<>();

    private Integer progress;

    /**
     * @brief Actualiza las métricas del job con los resultados del procesamiento
     * @param totalRecords total de registros procesados
     * @param successfulImports registros exitosos
     * @param failedImports registros fallidos
     * @param duplicatesSkipped duplicados omitidos
     */
    public void updateMetrics(Integer totalRecords, Integer successfulImports, 
                             Integer failedImports, Integer duplicatesSkipped) {
        this.totalRecords = totalRecords;
        this.successfulImports = successfulImports;
        this.failedImports = failedImports;
        this.duplicatesSkipped = duplicatesSkipped;
    }

    /**
     * @brief Agrega errores a la lista de errores del job
     * @param newErrors lista de errores a agregar
     */
    public void addErrors(List<ImportErrorDetail> newErrors) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        if (newErrors != null) {
            this.errors.addAll(newErrors);
        }
    }
}

