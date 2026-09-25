package co.unicauca.edu.co.contables.configuration.enterprise.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo de dominio que representa el tipo de una empresa.
 * Define las categorías o clasificaciones a las que puede pertenecer una empresa.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnterpriseType {

    /**
     * Identificador único del tipo de empresa.
     */
    private Long id;

    /**
     * Nombre del tipo de empresa.
     */
    private String name;
}
