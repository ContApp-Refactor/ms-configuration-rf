package co.unicauca.edu.co.contables.configuration.enterprise.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo de dominio que representa el tipo de persona.
 * Define las características básicas que identifican a una persona,
 * ya sea natural o jurídica.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonType {

    /**
     * Identificador único del tipo de persona.
     */
    private Long id;

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
