package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response;

import lombok.*;

/**
 * @brief DTO de respuesta para información de ciudades
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CityResponse {

    private String cityCode;
    private String cityName;
    private String stateCode;
    private String countryCode;
}
