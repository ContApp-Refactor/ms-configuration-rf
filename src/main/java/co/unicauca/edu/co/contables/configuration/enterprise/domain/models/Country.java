package co.unicauca.edu.co.contables.configuration.enterprise.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Modelo de dominio para un país
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Country {
    private Long id;
    private String name;
}
