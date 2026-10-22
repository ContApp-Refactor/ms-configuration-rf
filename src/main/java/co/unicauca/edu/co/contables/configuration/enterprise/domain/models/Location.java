package co.unicauca.edu.co.contables.configuration.enterprise.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief Modelo de dominio para la ubicación geográfica de una empresa
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {
    private Long id;
    private String address;
    private City city;
    private Country country;
    private Department department;
}
