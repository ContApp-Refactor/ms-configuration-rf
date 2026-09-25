package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response;

import lombok.*;

/**
 * @brief DTO de respuesta para información de estados/departamentos
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StateResponse {

    private String stateCode;
    private String stateName;
    private String countryCode;
}
