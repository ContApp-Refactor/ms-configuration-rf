package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

import java.time.Instant;

/**
 * @brief JPA entity for tracking RabbitMQ message processing errors
 * 
 */
@Entity
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

    /**
     * @brief Sets error timestamp before entity persistence
     */
    @PrePersist
    protected void onCreate() {
        if (errorTimestamp == null) {
            errorTimestamp = Instant.now();
        }
    }

    @TenantId
    String tenantId;
}
