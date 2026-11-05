package co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.dto.response;

import lombok.*;

/**
 * @brief DTO de respuesta para creación de impuestos
 *
 * Contiene la información del impuesto creado para respuestas
 * de la API REST con IDs de cuentas contables.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaxCreateRes {

    private  Long id;
    private String idEnterprise;
    private String code;
    private String description;
    private double interest;
    private Long purchaseTaxId;
    private Long salesTaxId;
    private Boolean status;
    private Integer usageCount;
}
