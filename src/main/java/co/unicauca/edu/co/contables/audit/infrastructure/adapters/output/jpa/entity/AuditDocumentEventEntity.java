package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.jpa.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import co.unicauca.edu.co.contables.audit.domain.enums.DocumentOperationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.Index;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_document_event", indexes = {
        @Index(name = "idx_doc_event_enterprise", columnList = "enterprise_id"),
        @Index(name = "idx_doc_event_code_enterprise", columnList = "document_code, enterprise_id"),
        @Index(name = "idx_doc_event_operation_at", columnList = "operation_at"),
        @Index(name = "idx_doc_event_enterprise_opat", columnList = "enterprise_id, operation_at"),
        @Index(name = "idx_doc_event_doc_type", columnList = "document_type"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditDocumentEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enterprise_id", nullable = false, length = 100)
    private String enterpriseId;

    @Column(name = "document_id", nullable = false, length = 255)
    private String documentId;

    @Column(name = "document_code", nullable = false, length = 50)
    private String documentCode;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;

    @Column(name = "document_date", nullable = false)
    private LocalDate documentDate;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "user_roles", columnDefinition = "jsonb", nullable = false)
    private List<String> userRoles;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 20)
    private DocumentOperationType operationType;

    @Column(name = "third_party_id", length = 255)
    private String thirdPartyId;

    @Column(name = "third_party_name", length = 100)
    private String thirdPartyName;

    @Column(name = "module_name", nullable = false, length = 100)
    private String moduleName;

    @Column(name = "operation_at", nullable = false)
    private Instant operationAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "document_data", columnDefinition = "jsonb")
    private Map<String, Object> documentData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
