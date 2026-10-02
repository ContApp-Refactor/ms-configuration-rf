package co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.presentation.DTO.response;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.enums.AccountType;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.presentation.DTO.response.BankRes;
import lombok.*;

/**
 * @brief DTO de respuesta para cuentas bancarias
 *
 * Contiene la información completa de una cuenta bancaria
 * para respuestas de la API REST.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountRes {
    private Long id;
    private Long accountNumber;
    private BankRes bank;
    private AccountType accountType;
    private Long accountingAccountId;
    private Boolean status;
    private String idEnterprise;
    private Integer usageCount;
}
