package co.unicauca.edu.co.contables.configuration.enterprise.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Modelo de dominio para el tipo de persona (natural o jurídica)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonType {
    private Long id;
    private String type;
    private String name;
    private String surname;
    private String bussinessName;
}
