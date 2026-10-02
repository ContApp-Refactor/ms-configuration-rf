package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * @brief DTO de solicitud para creación de unidades de medida
 *
 * Contiene datos requeridos para crear nuevas unidades de medida,
 * incluyendo nombre completo, abreviatura y descripción.
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UnitOfMeasureCreateRequest {

    @JsonIgnore
    private Long id;

    @NotBlank(message = "Nombre es requerido")
    private String name;

    @NotBlank(message = "Descripción es requerida")
    private String description;

    @NotBlank(message = "Abreviación es requerida")
    private String abbreviation;

    @NotBlank(message = "Id de la empresa es requerido")
    private String enterpriseId;

    @JsonIgnore
    @Builder.Default
    private boolean state = true; // Por defecto activo

}
