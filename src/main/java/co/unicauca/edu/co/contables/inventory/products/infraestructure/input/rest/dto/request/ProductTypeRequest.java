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

    @NotBlank(message = "Nombre es requerido")
    @NotNull(message = "El nombre es requerido")
    private String name;

    @NotBlank(message = "Descripción es requerida")
    @NotNull(message = "La descripcion es requerida")
    private String description;

    @NotBlank(message = "Id de la empresa es requerido")
    @NotNull(message = "El id de empresa es requerido")
    private String enterpriseId;
    
    @JsonIgnore
    @Builder.Default
    private boolean state = true;

    /**
     * Vendible. Null en el request = default true (ver IProductTypeRestMapper).
     */
    private Boolean sellable;

    /**
     * Comprable. Null en el request = default true (ver IProductTypeRestMapper).
     */
    private Boolean purchasable;

    /**
     * Inventariable (uso en kardex). Null en el request = default false.
     */
    private Boolean inventoryable;
}
