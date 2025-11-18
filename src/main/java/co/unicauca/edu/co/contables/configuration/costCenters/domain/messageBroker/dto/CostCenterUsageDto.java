package co.unicauca.edu.co.contables.configuration.costCenters.domain.messageBroker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CostCenterUsageDto {

    private Long costCenterId;
    private String enterpriseId;
    private Integer quantityUsed;

}
