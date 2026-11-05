package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.scheduledReport;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EFrequency;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EJobStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * @brief DTO de respuesta con la vista resumida de un reporte programado.
 *
 * Contiene solo los campos relevantes para listados: identificador
 * público, tipo de libro, frecuencia, próxima ejecución y estado
 * actual del job, optimizando el payload en endpoints de listado.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledReportListItemResponse {
    private String publicId;
    private EAuxiliaryBookType bookType;
    private EFrequency frequency;
    private Instant nextRunAt;
    private EJobStatus status;
}
