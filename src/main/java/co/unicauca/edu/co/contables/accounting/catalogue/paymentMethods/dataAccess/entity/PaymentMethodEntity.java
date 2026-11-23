package co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.dataAccess.entity;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;

/**
 * @brief Entidad que representa un método de pago en el sistema
 *
 * Almacena la información de métodos de pago con índices optimizados para
 * consultas por empresa, nombre y estado. Incluye soporte para
 * multi-tenancy y relación con cuentas contables.
 */
@Entity
@Table(
    name = "payment_methods",
    indexes = {
        @Index(name = "idx_payment_method_id_enterprise", columnList = "id_enterprise"),
        @Index(name = "idx_payment_method_name", columnList = "name"),
        @Index(name = "idx_payment_method_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "accounting_account_id", referencedColumnName = "id")
    private AccountCatalogueEntity accountingAccount;

    @Column(name = "status", nullable = false)
    private Boolean status;

    @Column(name = "id_enterprise", nullable = false)
    private String idEnterprise;

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    @TenantId
    @Column(name = "tenant_id")
    private String tenantId;
}
