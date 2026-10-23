package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

/**
 * Fila del outbox transaccional (EVT-1, EVT-6, design §6). Se escribe en la misma transaccion que el
 * documento y la publica el relay (H6). La unicidad (tenant, event_id) y las longitudes son explicitas
 * porque {@code ddl-auto=update} no las corrige despues. El estado es String plano (D10).
 */
@Entity(name = "BillingOutboxEventEntity")
@Table(name = "billing_outbox_events",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_billing_outbox_event", columnNames = {"tenant_id", "event_id"})
        },
        indexes = {
                @Index(name = "ix_billing_outbox_due", columnList = "status, next_attempt_at, id"),
                @Index(name = "ix_billing_outbox_aggregate", columnList = "tenant_id, aggregate_id")
        })
@Getter
@Setter
@NoArgsConstructor
public class BillingOutboxEventEntity {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PUBLISHED = "PUBLISHED";
    public static final String STATUS_FAILED = "FAILED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "tenant_id", nullable = false, length = 80)
    @Setter(AccessLevel.NONE)
    private String tenantId;

    @Column(name = "event_id", nullable = false, length = 36)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 60)
    private String eventType;

    @Column(name = "event_version", nullable = false)
    private int eventVersion;

    @Column(name = "aggregate_type", nullable = false, length = 20)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "enterprise_id", nullable = false, length = 50)
    private String enterpriseId;

    @Column(name = "exchange_name", nullable = false, length = 100)
    private String exchangeName;

    @Column(name = "routing_key", nullable = false, length = 100)
    private String routingKey;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "status", nullable = false, length = 12)
    private String status;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;
}
