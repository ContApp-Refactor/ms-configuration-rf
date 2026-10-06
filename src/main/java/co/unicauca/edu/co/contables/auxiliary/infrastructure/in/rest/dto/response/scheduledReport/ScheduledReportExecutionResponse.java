package co.unicauca.edu.co.contables.auxiliary.infrastructure.in.rest.dto.response.scheduledReport;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryStatus;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * @brief DTO de respuesta con el detalle de una ejecución de reporte programado.
 *
 * Expone los datos de una ejecución concreta: identificadores, fechas
 * de programación, inicio y fin, estado de ejecución, estado de
 * entrega y mensaje de error (si lo hubo).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledReportExecutionResponse {
    private UUID executionId;
    private UUID jobId;
    private Instant scheduledAt;
    private Instant startedAt;
    private Instant finishedAt;
    private EExecutionStatus statusExecution;
    private EDeliveryStatus deliveryStatus;
    private String errorMessage;
}
