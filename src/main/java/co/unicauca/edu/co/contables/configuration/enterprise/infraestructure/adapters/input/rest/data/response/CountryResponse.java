package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.response;

import lombok.*;

/**
 * Clase de respuesta que representa la información básica de un país.
 * Se utiliza específicamente para respuestas REST cuando se necesita
 * información resumida de un país.
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
public class CountryResponse {

    /**
     * Identificador único del país.
     */
    private Long id;

    /**
     * Nombre del país.
     */
    private String name;
}