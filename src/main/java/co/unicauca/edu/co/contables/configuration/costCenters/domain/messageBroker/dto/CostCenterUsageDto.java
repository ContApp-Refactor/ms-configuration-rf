package co.unicauca.edu.co.contables.configuration.costCenters.domain.messageBroker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO para uso de centros de costo
 *
 * DTO que representa información sobre el uso de un centro de costo,
 * incluyendo ID, empresa y cantidad utilizada.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CostCenterUsageDto {

    private Long costCenterId;
    private String enterpriseId;
    private Integer quantityUsed;

}
