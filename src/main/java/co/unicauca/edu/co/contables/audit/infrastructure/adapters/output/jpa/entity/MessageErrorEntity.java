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

// El nombre JPA debe ser único en la unidad de persistencia: debt_payments y
// notifications ya mapean la MISMA tabla con clases llamadas igual y Hibernate
// aborta con DuplicateMappingException si todos comparten el nombre de entidad.
// Las columnas difieren (audit declara error_at + error_stage; las otras usan
// error_timestamp), así que ddl-auto: update agrega las faltantes sin borrar
// nada (D-11.2). La tabla NO se renombra.
@Entity(name = "AuditMessageErrorEntity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "message_processing_errors")
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
