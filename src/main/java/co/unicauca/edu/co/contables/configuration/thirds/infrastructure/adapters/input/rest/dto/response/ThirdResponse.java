package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response;

import lombok.*;

/**
 * @brief DTO de respuesta para información básica de tercero
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ThirdResponse {

    private Long id;

    private String name;

    private String description;
}
