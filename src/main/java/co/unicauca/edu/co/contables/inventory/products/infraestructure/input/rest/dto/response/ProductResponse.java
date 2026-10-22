package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO de respuesta para datos de productos
 *
 * Representa información completa de productos para respuestas de la API REST,
 * incluyendo datos básicos, inventario y referencias a entidades relacionadas.
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer quantity;
    private Long unitOfMeasureId;
    private Long categoryId;
    private String enterpriseId;
    private double cost;
    private boolean state;
    private String reference;
    private String presentation;
    private Long productTypeId;
    private Integer usageCount;

}
