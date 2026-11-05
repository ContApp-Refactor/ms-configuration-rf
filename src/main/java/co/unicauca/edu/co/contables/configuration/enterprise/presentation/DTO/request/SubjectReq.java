package co.unicauca.edu.co.contables.configuration.enterprise.presentation.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO de solicitud para crear o actualizar una materia (subject).
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubjectReq {

    @NotBlank(message = "El código de la materia es obligatorio")
    private String code;

    @NotBlank(message = "El nombre de la materia es obligatorio")
    private String name;
}
