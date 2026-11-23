package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response;

import lombok.*;

/**
 * @brief DTO de respuesta para información de países
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CountryResponse {

    private String countryCode;
    private String countryName;
}
