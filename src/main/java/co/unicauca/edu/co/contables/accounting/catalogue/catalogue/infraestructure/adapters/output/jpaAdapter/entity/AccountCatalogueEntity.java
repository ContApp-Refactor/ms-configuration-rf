package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.TenantId;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.entity.TaxEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Entidad JPA para persistencia de cuentas contables en base de datos
 *
 * Representa la tabla Account con índices optimizados para consultas frecuentes.
 * Incluye relaciones jerárquicas padre-hijo y asociaciones con impuestos.
 */
@Entity
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
@Table(
    name="Account",
    indexes = {
        @Index(name = "idx_account_id_enterprise", columnList = "idEnterprise"),
        @Index(name = "idx_account_code", columnList = "code"),
        @Index(name = "idx_account_status", columnList = "status"),
        @Index(name = "idx_account_enterprise_status", columnList = "idEnterprise, status")
    }
)
public class AccountCatalogueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private String description;

    private NatureEnum  nature;
    private FinancialStatusEnum financialStatus;
    private ClassificationEnum classification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private AccountCatalogueEntity parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AccountCatalogueEntity> children = new ArrayList<>();

   @OneToMany(mappedBy = "salesTax", fetch = FetchType.LAZY)
   @Builder.Default
   private List<TaxEntity> salesTaxes = new ArrayList<>();

   @OneToMany(mappedBy = "purchaseTax", fetch = FetchType.LAZY)
   @Builder.Default
   private List<TaxEntity> purchaseTaxes = new ArrayList<>();

    private String idEnterprise;

    @TenantId
    String tenantId;

    private Boolean crossing;
    private Boolean costCenter;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private Boolean status = true;

    @Formula("LENGTH(code)")
    private Integer codeLength;
    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

}
