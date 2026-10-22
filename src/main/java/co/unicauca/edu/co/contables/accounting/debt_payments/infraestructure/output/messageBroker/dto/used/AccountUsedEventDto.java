package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto.used;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountUsedEventDto {
    private Long account; //Informacion de la cuenta (id/codigo)
    private String enterpriseId; //Id de empresa
    private String sourceAccountType; // = "ID" o "CODE"
}