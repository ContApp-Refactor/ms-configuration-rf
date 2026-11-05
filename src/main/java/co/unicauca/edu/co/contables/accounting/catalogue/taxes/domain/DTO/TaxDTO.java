package co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief DTO para transferencia de datos de impuestos
 *
 * Contiene la información esencial de un impuesto para operaciones
 * de creación, actualización y transferencia entre capas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxDTO {
    private Long id;
    private String idEnterprise;
    private String code;
    private String description;
    private Double interest;
    private Long salesTaxId;
    private Long purchaseTaxId;
}
