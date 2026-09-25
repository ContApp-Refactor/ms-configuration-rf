package co.unicauca.edu.co.contables.configuration.enterprise.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo de dominio que representa una responsabilidad fiscal.
 * Define las obligaciones tributarias que puede tener una empresa
 * ante las autoridades fiscales.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxLiability {
    
    /**
     * Identificador único de la responsabilidad fiscal.
     */
    private Long id;

    /**
     * Nombre o descripción de la responsabilidad fiscal.
     */
    private String name;
}
