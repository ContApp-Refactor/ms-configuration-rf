package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ImportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @brief Modelo que representa el estado de un trabajo de importación asíncrona
 *
 * Contiene información sobre el progreso, estado, métricas y errores
 * de una importación de catálogo de cuentas en proceso o completada.
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
    private Integer progress; // 0-100
    
    @Builder.Default
    private List<ImportErrorDetail> errors = new ArrayList<>();
}

