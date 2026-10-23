package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

/**
 * Ultimo consecutivo emitido por (tenant, empresa, tipo de documento) (D11). La fila se bloquea
 * pesimistamente al emitir; la unicidad evita dos filas para la misma clave si dos emisiones
 * concurrentes intentan crearla a la vez. {@code ddl-auto=update} no corrige despues: todo explicito.
 */
@Entity(name = "BillingDocumentSequenceEntity")
@Table(name = "billing_document_sequences",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_billing_seq", columnNames = {"tenant_id", "enterprise_id", "document_type"})
        })
@Getter
@Setter
@NoArgsConstructor
public class BillingDocumentSequenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "tenant_id", nullable = false, length = 80)
    @Setter(AccessLevel.NONE)
    private String tenantId;

    @Column(name = "enterprise_id", nullable = false, length = 50)
    private String enterpriseId;

    @Column(name = "document_type", nullable = false, length = 20)
    private String documentType;

    @Column(name = "prefix", nullable = false, length = 4)
    private String prefix;

    @Column(name = "last_number", nullable = false)
    private long lastNumber;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
