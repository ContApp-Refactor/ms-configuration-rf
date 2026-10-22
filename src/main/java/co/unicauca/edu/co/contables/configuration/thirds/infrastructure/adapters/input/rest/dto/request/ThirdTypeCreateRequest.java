package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * @brief DTO para solicitud de creación de tipo de tercero
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ThirdTypeCreateRequest {
    @NotNull(message = "El ID de la empresa no puede estar vacío")
    private String entId;

    private Long thirdTypeId;

    @NotNull(message = "El nombre del tipo de tercero no puede estar vacío")
    private String thirdTypeName;

    @Builder.Default
    private Boolean status = true;
}
