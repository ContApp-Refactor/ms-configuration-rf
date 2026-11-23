package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response;

import lombok.*;

/**
 * @brief DTO de respuesta para información de tipo de tercero
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ThirdTypeResponse {
    private Long thirdTypeId;
    private String thirdTypeName;
    private Boolean status;
}
