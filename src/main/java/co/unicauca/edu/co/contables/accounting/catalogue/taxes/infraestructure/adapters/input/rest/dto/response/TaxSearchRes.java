package co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.dto.response;

import lombok.*;

/**
 * @brief DTO de respuesta para consultas de impuestos
 *
 * Contiene la información completa de un impuesto para respuestas
 * de la API REST con cuentas contables formateadas.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class TaxSearchRes {
    private  Long id;
    private String idEnterprise;
    private String code;
    private String description;
    private double interest;
    private String purchaseTax;
    private String salesTax;
    private Boolean status;
    private Integer usageCount;
}
