package co.unicauca.edu.co.contables.notifications.infrastructure.output.jpa.entity;

import java.time.Instant;

import org.hibernate.annotations.TenantId;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Entity class representing message processing errors.
 * Maps to the "message_processing_errors" table in the database.
 * Contains details about errors encountered during message processing.
 * Includes fields for event type, error description, message data, timestamp, and entity type.
 * Automatically sets the error timestamp on creation if not provided.
 */

// El nombre JPA debe ser único en la unidad de persistencia: debt_payments ya
// mapea la MISMA tabla con una clase llamada igual y Hibernate aborta con
// DuplicateMappingException si ambos comparten el nombre de entidad. Las
// columnas son idénticas, así que la tabla no cambia (D-10.2).
@Entity(name = "NotificationsMessageProcessingErrorEntity")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name = "message_processing_errors")
public class MessageProcessingErrorEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "error_description", columnDefinition = "TEXT")
    private String errorDescription;

    @Column(name = "message_data", columnDefinition = "TEXT")
    private String messageData;

    @Column(name = "error_timestamp", nullable = false)
    private Instant errorTimestamp;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @PrePersist
    protected void onCreate() {
        if (errorTimestamp == null) {
            errorTimestamp = Instant.now();
        }
    }

    @TenantId
    String tenantId;
}
