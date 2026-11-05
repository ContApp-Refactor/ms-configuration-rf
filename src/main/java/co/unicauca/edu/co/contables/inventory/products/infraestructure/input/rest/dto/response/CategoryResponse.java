package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.response;

import java.util.List;

import lombok.*;

/**
 * @brief DTO de respuesta para datos de categorías
 *
 * Representa información completa de categorías para respuestas de la API REST,
 * incluyendo configuración contable y estado de activación.
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private String enterpriseId;
    private Long inventoryId;
    private Long costId;
    private Long saleId;
    private Long returnId;
    private List<Long> taxes;
    private boolean state;
}
