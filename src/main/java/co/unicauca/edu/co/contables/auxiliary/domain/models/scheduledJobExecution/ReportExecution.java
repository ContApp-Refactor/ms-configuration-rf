package co.unicauca.edu.co.contables.auxiliary.domain.models.scheduledJobExecution;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookFormat;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryStatus;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EExecutionStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Modelo de dominio para una ejecución individual de un reporte programado.
 *
 * Registra tiempos (programado, inicio, fin), el estado de la ejecución
 * y de la entrega, referencias a los artefactos por formato, el
 * identificador de correlación, el contador de reintentos y los
 * códigos/mensajes de error en caso de falla.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportExecution {
    private UUID executionId;
    private UUID jobId;
    private Instant scheduledAt;
    private Instant startedAt;
    private Instant finishedAt;
    private EExecutionStatus statusExecution;
    private EDeliveryStatus deliveryStatus;
    private Map<EAuxiliaryBookFormat,String> artifactRefs;
    private String errorCode;
    private String errorMessage;
    private String correlationId;
    private int retryCount;
}
