package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.used;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CostCenterUsedDto {
    private Long costCenterId;
    private String enterpriseId;
    private Integer quantityUsed;
}
