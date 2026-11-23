package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.used;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ThirdUsedEvenDto {
    private Long thirdId;
    private String enterpriseId;
    private Integer quantityUsed;
}
