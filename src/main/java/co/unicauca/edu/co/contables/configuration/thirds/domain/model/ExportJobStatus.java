package co.unicauca.edu.co.contables.configuration.thirds.domain.model;

import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ImportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @brief Modelo que representa el estado de un trabajo de exportación asíncrona
 *
 * Contiene información sobre el progreso, estado, métricas y resultado
 * de una exportación de terceros en proceso o completada.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportJobStatus {
    private String jobId;
    private String entId;
    private String fileName;
    private ImportStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalRecords;
    private Integer progress; // 0-100
    private byte[] fileData; // Datos del archivo generado
    private String errorMessage;
}

