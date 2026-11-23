package co.unicauca.edu.co.contables.accounting.catalogue.taxes.infraestructure.adapters.input.rest.dto.response;

import lombok.*;

/**
 * @brief DTO de respuesta para cambio de estado de impuestos
 *
 * Contiene la información básica del impuesto y mensaje de confirmación
 * para respuestas de operaciones de cambio de estado.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaxChangeStateRes {
    private Long id;
    private String code;
    private String description;
    private Boolean status;
    private Integer usageCount;
    private String message;
}
