package co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TenantId;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;

/**
 * @brief Entidad que representa un impuesto en el sistema
 *
 * Almacena la información de impuestos con índices optimizados para
 * consultas por empresa, código y estado. Incluye relaciones con
 * cuentas contables de venta y compra, y soporte para multi-tenancy.
 */
@Entity
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
@Table(name="Tax",
    indexes = {
        @Index(name = "idx_tax_id_enterprise", columnList = "idEnterprise"),
        @Index(name = "idx_tax_code", columnList = "code"),
        @Index(name = "idx_tax_status", columnList = "status"),
        @Index(name = "idx_tax_enterprise_status", columnList = "idEnterprise, status")
    })
public class TaxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String code;

    private String description;
    
    @Positive
    @Column(nullable = false)
    private Double interest;

    @ManyToOne
    @JoinColumn(name="salesTax_code")
    private AccountCatalogueEntity salesTax;

    @ManyToOne
    @JoinColumn(name="purchaseTax_code")
    private AccountCatalogueEntity purchaseTax;

    private String idEnterprise;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private Boolean status = true;

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    @TenantId
    String tenantId;
}
