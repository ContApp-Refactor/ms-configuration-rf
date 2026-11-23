package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.output.jpaAdapters.entity.TaxEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Modelo de dominio para cuentas contables del catálogo
 *
 * Representa una cuenta contable completa con toda su información jerárquica,
 * asociaciones con impuestos y campos opcionales para funcionalidad avanzada.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AccountCatalogue {
    private Long id;
    private String idEnterprise;
    private String code;
    private String description;
    private NatureEnum nature;
    private FinancialStatusEnum financialStatus;
    private ClassificationEnum classification;
    private AccountCatalogue  parent;
    private List<AccountCatalogue> children;
    @Builder.Default
    private List<TaxEntity> salesTaxes = new ArrayList<>();
    @Builder.Default
    private List<TaxEntity>  purchaseTaxes = new ArrayList<>();
    private Boolean crossing;
    private Boolean costCenter;
    private Boolean status;
    private Boolean isDeleted;
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;
    @Builder.Default
    private Integer usageCount = 0;

    /**
     * @brief Verifica si la cuenta contable está siendo usada
     * @return true si la cuenta tiene uso registrado
     */
    public boolean isInUse() {
        return this.usageCount != null && this.usageCount > 0;
    }
}
