package co.unicauca.edu.co.contables.inventory.products.domain.model;

import lombok.*;

/**
 * @brief Entidad que representa una unidad de medida
 *
 * Define las unidades estándar para medir cantidades de productos,
 * incluyendo nombre completo, abreviatura y descripción para uso en la interfaz.
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UnitOfMeasure {

    private Long id;
    private String name;
    private String description;
    private String abbreviation;
    private String enterpriseId;

    @Builder.Default
    private boolean state = true;
}
