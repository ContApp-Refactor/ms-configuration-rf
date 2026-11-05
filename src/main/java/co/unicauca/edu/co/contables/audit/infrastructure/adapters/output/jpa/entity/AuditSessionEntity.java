package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.entity;

import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import co.unicauca.edu.co.contables.audit.domain.enums.UserAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_session", indexes = {
        @Index(name = "idx_audit_action_date", columnList = "action, action_at DESC"),
        @Index(name = "idx_audit_session_action", columnList = "session_id, action"),
        @Index(name = "idx_audit_user_name", columnList = "user_name"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "user_role", columnDefinition = "jsonb", nullable = false)
    private List<String> userRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private UserAction action;

    @Column(name = "action_at", nullable = false)
    private Instant actionAt;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
