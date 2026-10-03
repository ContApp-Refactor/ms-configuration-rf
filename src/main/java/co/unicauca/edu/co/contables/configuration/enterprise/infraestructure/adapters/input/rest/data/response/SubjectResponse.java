package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase de respuesta que representa la información básica de una materia.
 * Mantiene exactamente la forma que consume el frontend
 * ({@code Subject { code, name }}).
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubjectResponse {

    /**
     * Código único de la materia.
     */
    private String code;

    /**
     * Nombre de la materia.
     */
    private String name;
}
