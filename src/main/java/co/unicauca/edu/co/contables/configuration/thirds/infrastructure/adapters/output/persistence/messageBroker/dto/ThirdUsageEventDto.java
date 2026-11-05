package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.messageBroker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO para eventos de uso de terceros recibidos
 *
 * Contiene la información necesaria para actualizar el contador de uso
 */
@Getter 
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ThirdUsageEventDto {
    private Long thirdId;
    private String enterpriseId;
    private Integer quantityUsed;
}

