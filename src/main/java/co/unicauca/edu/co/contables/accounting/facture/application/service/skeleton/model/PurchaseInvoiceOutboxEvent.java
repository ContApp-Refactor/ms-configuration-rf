package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "purchase_invoice_outbox", uniqueConstraints =
        @UniqueConstraint(name = "uk_purchase_outbox_tenant_event", columnNames = {"tenant_id", "event_id"}))
@Getter @Setter @NoArgsConstructor
public class PurchaseInvoiceOutboxEvent {
    public enum Status { PENDING, PUBLISHED, FAILED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="event_id",nullable=false,length=80) private String eventId;
    @Column(name="event_type",nullable=false,length=80) private String eventType;
    @Column(nullable=false,columnDefinition="TEXT") private String payload;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status=Status.PENDING;
    @Column(nullable=false) private int attempts;
    @Column(name="next_attempt_at",nullable=false) private Instant nextAttemptAt;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    @Column(name="published_at") private Instant publishedAt;
    @Column(name="last_error",length=500) private String lastError;
    @TenantId @Column(name="tenant_id",nullable=false,length=80) private String tenantId;
    @PrePersist void created(){createdAt=Instant.now();if(nextAttemptAt==null)nextAttemptAt=createdAt;}
}
