package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.response;

import lombok.Getter;
import lombok.Setter;

/**
 * @brief DTO de respuesta para datos de tipos de producto
 *
 * Representa información básica de tipos de producto para respuestas de la API REST,
 * incluyendo estado de activación y pertenencia a empresa.
 */
@Getter
@Setter
public class ProductTypeResponse {
    private Long id;
    private String name;
    private String description;
    private String enterpriseId;
    private boolean state;
}
