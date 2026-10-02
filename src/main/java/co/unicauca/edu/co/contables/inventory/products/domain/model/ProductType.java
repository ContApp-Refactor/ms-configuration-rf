package co.unicauca.edu.co.contables.inventory.products.domain.model;


import lombok.*;

/**
 * @brief Entidad que representa un tipo de producto
 *
 * Clasifica productos según su naturaleza o características específicas,
 * permitiendo agrupaciones lógicas para gestión y reporting.
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductType {

    private Long id;
    private String name;
    private String description;
    private String enterpriseId;

    @Builder.Default
    private boolean state = true;

}
