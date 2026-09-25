package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity;

import lombok.*;

import java.io.Serializable;

/**
 * @brief Clave compuesta para CityEntity (cityCode + stateCode + countryCode)
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CityEntityId implements Serializable {

    private String cityCode;
    private String stateCode;
    private String countryCode;
}
