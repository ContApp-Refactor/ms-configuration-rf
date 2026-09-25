package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response;

import lombok.*;

/**
 * @brief DTO de respuesta para cambio de estado de tercero individual
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangeThirdStateResponse {
    private Boolean result;
}
