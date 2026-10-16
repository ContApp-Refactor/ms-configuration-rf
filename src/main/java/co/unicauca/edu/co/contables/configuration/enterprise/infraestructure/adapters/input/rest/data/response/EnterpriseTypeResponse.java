package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.response;

import lombok.*;

/**
 * Clase de respuesta que representa la información básica de un tipo de empresa.
 * Se utiliza específicamente para respuestas REST cuando se necesita
 * información resumida de un tipo de empresa.
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
public class EnterpriseTypeResponse {

    /**
     * Identificador único del tipo de empresa.
     */
    private Long id;

    /**
     * Nombre del tipo de empresa.
     */
    private String name;
}