package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de solicitud para crear o actualizar una materia (subject).
 * El código es el identificador único de negocio de la materia.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubjectRequest {

    /**
     * Código único de la materia.
     */
    @NotBlank(message = "El código de la materia es obligatorio")
    private String code;

    /**
     * Nombre de la materia.
     */
    @NotBlank(message = "El nombre de la materia es obligatorio")
    private String name;
}
