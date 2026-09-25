package co.unicauca.edu.co.contables.configuration.enterprise.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo de dominio que representa el tipo de contribuyente.
 * Define las diferentes categorías de contribuyentes reconocidas
 * por las autoridades fiscales.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxPayerType {
    
    /**
     * Identificador único del tipo de contribuyente.
     */
    private Long id;

    /**
     * Nombre o descripción del tipo de contribuyente.
     */
    private String name;
}
