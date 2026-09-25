package co.unicauca.edu.co.contables.configuration.enterprise.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Modelo de dominio que representa un departamento.
 * Contiene la información básica de un departamento y su relación
 * con el país al que pertenece, así como las ciudades asociadas.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    /**
     * Identificador único del departamento.
     */
    private Long id;

    /**
     * Nombre del departamento.
     */
    private String name;

    /**
     * País al que pertenece el departamento.
     */
    private Country country;

    /**
     * Lista de ciudades que pertenecen al departamento.
     */
    private List<City> cities;
}
