package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.response;

import lombok.*;

/**
 * @brief DTO de respuesta para datos de unidades de medida
 *
 * Representa información completa de unidades de medida para respuestas de la API REST,
 * incluyendo nombre completo, abreviatura y estado de activación.
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UnitOfMeasureResponse {

    private Long id;
    private String name;
    private String description;
    private String abbreviation;
    private String enterpriseId;
    private boolean state;
}
