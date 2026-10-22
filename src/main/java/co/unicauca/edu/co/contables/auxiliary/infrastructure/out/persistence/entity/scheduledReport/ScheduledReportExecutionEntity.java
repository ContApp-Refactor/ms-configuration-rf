package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.scheduledReport;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryStatus;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EExecutionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * @brief Entidad JPA que representa una ejecución de reporte programado.
 *
 * Mapea cada intento de ejecución de un job: identificadores, fechas
 * programadas y reales, estado de ejecución, estado de entrega,
 * información de error y contador de reintentos.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "SCHEDULED_REPORT_EXECUTION")
public class ScheduledReportExecutionEntity {

    @Id
    private UUID executionId;

    @Column(nullable = false)
    private UUID jobId;

    @Column(nullable = false)
    private Instant scheduledAt;

    @Column
    private Instant startedAt;

    @Column
    private Instant finishedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EExecutionStatus statusExecution;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EDeliveryStatus deliveryStatus;

    @Column
    private String errorCode;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column
    private String correlationId;

    @Column
    private int retryCount;
}
