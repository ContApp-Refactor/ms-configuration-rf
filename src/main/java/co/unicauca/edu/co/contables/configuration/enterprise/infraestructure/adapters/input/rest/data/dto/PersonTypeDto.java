package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO que representa la información de un tipo de persona.
 * Se utiliza para transferir datos de tipos de persona desde y hacia las interfaces REST,
 * permitiendo manejar tanto personas naturales como jurídicas.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class PersonTypeDto {

    /**
     * Tipo de persona (natural o jurídica).
     */
    private String type;

    /**
     * Nombre(s) de la persona natural.
     */
    private String name;

    /**
     * Apellido(s) de la persona natural.
     */
    private String surname;

    /**
     * Razón social para personas jurídicas.
     */
    private String bussinessName;
}
