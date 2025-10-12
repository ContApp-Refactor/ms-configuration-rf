package co.unicauca.edu.co.contables.configuration.helpCenter.presentation.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpCenterUpdateReq {

    @NotNull(message = "El ID es obligatorio")
    private Long id;

    @NotNull(message = "El ID del módulo es obligatorio")
    private Integer moduleId;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String name;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @NotBlank(message = "El ID de la empresa es obligatorio")
    private String idEnterprise;
}
