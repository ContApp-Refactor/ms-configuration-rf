package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;
import lombok.*;
import org.springframework.data.domain.Page;

/**
 * @brief DTO de respuesta para lista paginada de terceros por empresa
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ThirdsEnterpriseListResponse {
    private Page<Third> results;
}
