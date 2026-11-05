package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.scheduledReport;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryStatus;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * @brief DTO de respuesta con la vista resumida de una ejecución.
 *
 * Contiene los campos mínimos para listar ejecuciones de un reporte
 * programado: identificador, fecha programada, estado de ejecución,
 * estado de entrega y mensaje de error (si aplica).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledReportExecutionListItemResponse {
    private UUID executionId;
    private Instant scheduledAt;
    private EExecutionStatus statusExecution;
    private EDeliveryStatus deliveryStatus;
    private String errorMessage;
}
