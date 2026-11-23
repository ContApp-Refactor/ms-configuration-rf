package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Tabla PROPIA, no la compartida `message_processing_errors` de debt_payments y
// notifications. El plan inicial (D-11.2) dejaba audit sobre la tabla compartida
// y renombraba solo la entidad; `ddl-auto: update` agregó `error_at NOT NULL` a
// esa tabla y rompió los inserts de kardex, que no poblan esa columna. El listener
// revienta, la cola se atasca y los GETs asíncronos devuelven 404 (36 fallos
// nuevos en newman). Cada módulo lleva su propio registro de errores, igual que
// en los orígenes, donde cada microservicio tenía su propia base.
@Entity(name = "AuditMessageErrorEntity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "audit_message_processing_errors")
@Builder
public class MessageErrorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "error_description", columnDefinition = "TEXT")
    private String errorDescription;

    @Column(name = "message_data", columnDefinition = "TEXT")
    private String messageData;

    @Column(name = "error_stage")
    private String errorStage;

    @Column(name = "error_at", nullable = false)
    private Instant errorAt;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    /**
     * @brief Sets error timestamp before entity persistence
     */
    @PrePersist
    protected void onCreate() {
        if (errorAt == null) {
            errorAt = Instant.now();
        }
    }

}
