package co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * @brief DTO de solicitud para crear registros del centro de ayuda
 *
 * DTO que representa la solicitud para crear un nuevo registro del centro de ayuda,
 * con validaciones de campos obligatorios y restricciones de tamaño.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpCenterCreateReq {

    @NotNull(message = "El ID del módulo es obligatorio")
    private Integer moduleId;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String name;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;
}
