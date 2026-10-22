package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO de solicitud para creación de productos
 *
 * Define estructura de datos para crear nuevos productos en el sistema,
 * incluyendo validaciones y referencias a entidades relacionadas.
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreateRequest {

    @JsonIgnore
    private Long id;

    @JsonIgnore
    private String code;

    @NotBlank(message = "Nombre es requerido")
    private String name;

    @NotBlank(message = "Descripción es requerida")
    private String description;

    private Integer quantity;

    @NotNull(message = "Id de la unidad de medida es requerido")
    private Long unitOfMeasureId;

    @NotNull(message = "Id de la categoría es requerido")
    private Long categoryId;

    @NotNull(message = "El tipo de producto es requerido")
    private Long productTypeId;

    @NotNull(message = "Id de la empresa es requerido")
    private String enterpriseId;

    private double cost;

    @JsonIgnore
    @Builder.Default
    private boolean state = true; // Por defecto activo

    @NotNull(message = "La referencia es requerida")
    private String reference;

    @NotNull(message = "La presentacion es requerida")
    private String presentation;
    

}
