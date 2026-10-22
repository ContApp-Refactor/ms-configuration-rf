package co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.messageBroker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO para eventos de uso de cuentas bancarias
 *
 * Contiene la información necesaria para actualizar el contador de uso
 * cuando otros servicios notifican que han utilizado una cuenta bancaria.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountUsageDto {
    private Long bankAccountId;
    private String enterpriseId;
    private Integer quantityUsed;
}
