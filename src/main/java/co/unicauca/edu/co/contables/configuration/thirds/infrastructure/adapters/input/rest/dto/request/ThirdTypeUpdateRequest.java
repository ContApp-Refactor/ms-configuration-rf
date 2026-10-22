package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * @brief DTO para solicitud de actualización de tipo de tercero
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThirdTypeUpdateRequest {

    @NotNull(message = "El ID del tipo de tercero es obligatorio")
    private Long thirdTypeId;

    @NotBlank(message = "El nombre del tipo de tercero es obligatorio")
    private String thirdTypeName;

    @NotBlank(message = "El ID de la entidad es obligatorio")
    private String entId;

    @Builder.Default
    private Boolean status = true;
}
