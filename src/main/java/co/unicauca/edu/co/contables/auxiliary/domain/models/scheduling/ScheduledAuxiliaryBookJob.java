package co.unicauca.edu.co.contables.auxiliary.domain.models.scheduling;

import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.AuxiliaryBookCriteria;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.AuxiliaryBookTemplate;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EJobStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @brief Modelo de dominio de un job de reporte programado.
 *
 * Describe un reporte recurrente sobre un libro auxiliar: tipo de libro,
 * criterios de generación, especificación temporal ({@link ScheduleSpec}),
 * configuración de entrega ({@link DeliveryConfig}), autor y propietarios
 * (entId/userId), fechas de creación/fin y estado actual ({@link EJobStatus}).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledAuxiliaryBookJob {
    private Long jobId;
    private String publicId;
    private EAuxiliaryBookType bookType;
    private AuxiliaryBookCriteria criteria;
    private ScheduleSpec scheduleSpec;
    private DeliveryConfig deliveryConfig;
    private LocalDateTime createdAt;
    private LocalDateTime endAt;
    private String createdBy; // Public ID user
    private String entId;
    private Long userId;
    private String ownerSub;
    private EJobStatus status;
    private AuxiliaryBookTemplate template;
}
