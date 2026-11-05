package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.scheduledReport;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EAuxiliaryBookType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EDeliveryWay;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EFrequency;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.EJobStatus;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.persistence.entity.AuxiliaryBookCriteriaEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * @brief Entidad JPA que representa un reporte programado.
 *
 * Mapea la configuración persistente de un job de reporte programado:
 * identidad, tipo de libro, criterios, frecuencia, ventana de ejecución,
 * próxima ejecución, estado, datos de auditoría y configuración de
 * entrega (vía y correo).
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "SCHEDULED_REPORT")
public class ScheduledReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String publicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EAuxiliaryBookType bookType;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "criteria_id", referencedColumnName = "id")
    private AuxiliaryBookCriteriaEntity criteria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EFrequency frequency;

    @Column(nullable = false)
    private Instant startAt;

    @Column
    private Instant endAt;

    @Column
    private Instant nextRunAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EJobStatus status;

    @Column(nullable = false)
    private String entId;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 128)
    private String ownerSub;

    @Column
    private String createdBy;

    @Enumerated(EnumType.STRING)
    @Column
    private EDeliveryWay deliveryWay;

    @Column
    private String emailTo;

    @Column
    private String emailSubject;

    @Column
    private String emailBody;

    // -------- Template del reporte --------
    @Column(length = 128)
    private String templateName;

    @Column(columnDefinition = "TEXT")
    private String templatePathLogotype;

    @Column(length = 16)
    private String templateAlignment;

    @Column(length = 64)
    private String templateFont;

    @Column
    private Integer templateFontSize;

    @Column(length = 16)
    private String templateMainColor;

    @CreationTimestamp
    @Column
    private LocalDateTime createdAt;
}
