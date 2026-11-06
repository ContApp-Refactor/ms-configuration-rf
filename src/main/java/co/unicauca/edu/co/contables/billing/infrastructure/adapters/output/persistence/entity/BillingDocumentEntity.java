package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

/**
 * Cabecera de un documento de facturacion (factura o devolucion, venta o compra). Todas las
 * longitudes, nulabilidades e indices son explicitos porque {@code ddl-auto=update} no corrige
 * despues (design §6). Los enums se guardan como String plano (D10): {@code @Enumerated(STRING)}
 * hace que Hibernate 6 genere CHECK constraints que {@code update} nunca refresca.
 *
 * <p>El tenant lo llena y valida Hibernate ({@link TenantId}); nunca se asigna a mano.
 */
@Entity(name = "BillingDocumentEntity")
@Table(name = "billing_documents",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_billing_doc_number",
                        columnNames = {"tenant_id", "enterprise_id", "document_type", "number_value"})
        },
        indexes = {
                @Index(name = "ix_billing_doc_list",
                        columnList = "tenant_id, enterprise_id, document_type, status, updated_at"),
                @Index(name = "ix_billing_doc_original",
                        columnList = "tenant_id, original_document_id, status")
        })
@Getter
@Setter
@NoArgsConstructor
public class BillingDocumentEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @TenantId
    @Column(name = "tenant_id", nullable = false, length = 80)
    @Setter(AccessLevel.NONE)
    private String tenantId;

    @Column(name = "enterprise_id", nullable = false, length = 50)
    private String enterpriseId;

    @Column(name = "document_type", nullable = false, length = 20)
    private String documentType;

    @Column(name = "status", nullable = false, length = 12)
    private String status;

    @Column(name = "number_prefix", length = 4)
    private String numberPrefix;

    @Column(name = "number_value")
    private Long numberValue;

    @Column(name = "number_display", length = 20)
    private String numberDisplay;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "counterparty_id")
    private Long counterpartyId;

    @Column(name = "counterparty_role", length = 10)
    private String counterpartyRole;

    @Column(name = "counterparty_name", length = 300)
    private String counterpartyName;

    @Column(name = "counterparty_id_type", length = 10)
    private String counterpartyIdType;

    @Column(name = "counterparty_id_number", length = 30)
    private String counterpartyIdNumber;

    @Column(name = "counterparty_verification", length = 5)
    private String counterpartyVerification;

    @Column(name = "counterparty_address", length = 300)
    private String counterpartyAddress;

    @Column(name = "counterparty_email", length = 150)
    private String counterpartyEmail;

    @Column(name = "counterparty_phone", length = 20)
    private String counterpartyPhone;

    @Column(name = "supplier_invoice_number", length = 50)
    private String supplierInvoiceNumber;

    @Column(name = "issuer_name", length = 200)
    private String issuerName;

    @Column(name = "issuer_nit", length = 20)
    private String issuerNit;

    @Column(name = "issuer_dv", length = 2)
    private String issuerDv;

    @Column(name = "original_document_id")
    private UUID originalDocumentId;

    @Column(name = "original_document_type", length = 20)
    private String originalDocumentType;

    @Column(name = "original_number_display", length = 20)
    private String originalNumberDisplay;

    @Column(name = "original_issue_date")
    private LocalDate originalIssueDate;

    @Column(name = "return_reason", length = 500)
    private String returnReason;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "subtotal", nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_total", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxTotal;

    @Column(name = "total", nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by", nullable = false, length = 100)
    private String updatedBy;

    @Column(name = "emitted_at")
    private Instant emittedAt;

    @Column(name = "emitted_by", length = 100)
    private String emittedBy;

    @Column(name = "annulled_at")
    private Instant annulledAt;

    @Column(name = "annulled_by", length = 100)
    private String annulledBy;

    @Column(name = "annul_reason", length = 500)
    private String annulReason;

    /** Wrapper {@code Long}: nulo = entidad nueva (Spring Data la persiste en vez de fusionarla). */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("lineNo ASC")
    private List<BillingDocumentLineEntity> lines = new ArrayList<>();
}
