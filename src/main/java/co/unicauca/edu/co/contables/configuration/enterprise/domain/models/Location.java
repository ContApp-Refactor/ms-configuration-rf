package co.unicauca.edu.co.contables.configuration.enterprise.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo de dominio que representa la ubicación geográfica.
 * Contiene la información detallada de una dirección, incluyendo su
 * jerarquía geográfica (ciudad, departamento y país).
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    /**
     * Identificador único de la ubicación.
     */
    private Long id;

    /**
     * Dirección específica de la ubicación.
     */
    private String address;

    /**
     * Ciudad a la que pertenece la ubicación.
     */
    City city;

    /**
     * País donde se encuentra la ubicación.
     */
    Country country;

    /**
     * Departamento al que pertenece la ubicación.
     */
    Department department;
}
