package co.unicauca.edu.co.contables.configuration.enterprise.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Modelo de dominio para el tipo de contribuyente
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxPayerType {
    private Long id;
    private String name;
}
