package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.entity;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import co.unicauca.edu.co.contables.audit.domain.enums.OperationType;

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
@Table(name = "audit_operation", indexes = {
        @Index(name = "idx_audit_op_enterprise_date", columnList = "enterprise_id, operation_at"),
        @Index(name = "idx_audit_op_enterprise_table_date", columnList = "enterprise_id, affected_table, operation_at"),
        @Index(name = "idx_audit_op_register", columnList = "register_id, affected_table")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditOperationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enterprise_id", nullable = false, length = 50)
    private String enterpriseId;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "user_role", columnDefinition = "jsonb", nullable = false)
    private List<String> userRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 20)
    private OperationType operationType;

    @Column(name = "operation_at", nullable = false)
    private Instant operationAt;

    @Column(name = "module_name", nullable = false, length = 100)
    private String moduleName;

    @Column(name = "affected_table", nullable = false, length = 100)
    private String affectedTable;

    @Column(name = "register_id", nullable = false, length = 100)
    private String registerId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_object", columnDefinition = "jsonb")
    private Map<String, Object> dataObject;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
