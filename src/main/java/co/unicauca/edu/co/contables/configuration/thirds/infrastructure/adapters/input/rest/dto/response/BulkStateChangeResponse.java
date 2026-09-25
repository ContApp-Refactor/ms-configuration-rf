package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief DTO de respuesta para cambio masivo de estado de terceros
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkStateChangeResponse {

    private int updatedCount;

    private Boolean newState;

    private String message;
}
