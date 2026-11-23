package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity;

import lombok.*;

import java.io.Serializable;

/**
 * @brief Clave compuesta para StateEntity (stateCode + countryCode)
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class StateEntityId implements Serializable {

    private String stateCode;
    private String countryCode;
}
