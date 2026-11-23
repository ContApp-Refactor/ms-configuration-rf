package co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;

/**
 * @brief Modelo de dominio que representa un impuesto
 *
 * Contiene la información esencial de un impuesto incluyendo
 * cuentas contables de venta y compra asociadas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tax {
    private Long id;
    private String idEnterprise;
    private String code;
    private String description;
    private Double interest;
    @JsonIgnore
    private AccountCatalogueEntity salesTax;
    @JsonIgnore
    private AccountCatalogueEntity purchaseTax;
    private Boolean status;
    @Builder.Default
    private Integer usageCount = 0;

    /**
     * @brief Verifica si el impuesto está siendo usado
     * @return true si el impuesto tiene uso registrado
     */
    public boolean isInUse() {
        return this.usageCount != null && this.usageCount > 0;
    }
}
