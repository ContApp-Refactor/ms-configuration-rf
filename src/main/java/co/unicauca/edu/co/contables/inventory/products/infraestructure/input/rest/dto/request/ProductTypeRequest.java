package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * @brief DTO de solicitud para operaciones con tipos de producto
 *
 * Define estructura de datos para crear o actualizar tipos de producto,
 * con validaciones requeridas para integridad de datos.
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductTypeRequest {

    @NotBlank
    @NotNull(message = "El nombre es requerido")
    private String name;

    @NotBlank
    @NotNull(message = "La descripcion es requerida")
    private String description;

    @NotBlank
    @NotNull(message = "El id de empresa es requerido")
    private String enterpriseId;
    
    @JsonIgnore
    @Builder.Default
    private boolean state = true; 
}
