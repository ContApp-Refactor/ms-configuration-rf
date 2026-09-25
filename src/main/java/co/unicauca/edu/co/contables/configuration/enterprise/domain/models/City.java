package co.unicauca.edu.co.contables.configuration.enterprise.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo de dominio que representa una ciudad.
 * Contiene la información básica de una ciudad y su relación
 * con el departamento al que pertenece.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City
{
    /**
     * Identificador único de la ciudad.
     */
    private Long id;

    /**
     * Nombre de la ciudad.
     */
    private String name;

    /**
     * Departamento al que pertenece la ciudad.
     */
    private Department department;
}
