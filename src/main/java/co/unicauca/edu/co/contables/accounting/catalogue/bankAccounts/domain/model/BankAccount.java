package co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.model;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.enums.AccountType;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.model.Bank;
import lombok.*;

/**
 * @brief Modelo de dominio que representa una cuenta bancaria
 *
 * Contiene la información esencial de una cuenta bancaria incluyendo
 * número de cuenta, banco asociado, tipo y cuenta contable relacionada.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccount {
    private Long id;
    private Long accountNumber;
    private Bank bank;
    private AccountType accountType;
    private Long accountingAccountId;
    @Builder.Default
    private Boolean status = true;
    private String idEnterprise;
    @Builder.Default
    private Integer usageCount = 0;

    /**
     * @brief Verifica si la cuenta bancaria está siendo usada
     * @return true si la cuenta bancaria tiene uso registrado
     */
    public boolean isInUse() {
        return this.usageCount != null && this.usageCount > 0;
    }
}
