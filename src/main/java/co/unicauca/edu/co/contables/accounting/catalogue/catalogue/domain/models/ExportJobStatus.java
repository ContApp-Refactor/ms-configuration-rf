package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ImportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @brief Modelo de dominio para el estado de trabajos de exportación asíncronos
 *
 * Rastrea el progreso y resultados de exportaciones de catálogo de cuentas,
 * permitiendo consultas de estado en tiempo real y descarga del archivo generado.
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
    private Integer progress;
    private byte[] fileData;
    private String errorMessage;
}

