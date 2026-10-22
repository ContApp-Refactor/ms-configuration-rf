package co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.presentation.DTO.request;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.enums.AccountType;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * @brief DTO de solicitud para crear cuentas bancarias
 *
 * Contiene los campos requeridos para crear una nueva cuenta bancaria
 * con validaciones de formato y obligatoriedad.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountCreateReq {

    @NotBlank(message = "La empresa es obligatoria")
    private String idEnterprise;

    @NotNull(message = "El número de cuenta es obligatorio")
    @Min(value = 10000000L, message = "El número de cuenta debe tener al menos 8 dígitos")
    @Max(value = 9999999999999999L, message = "El número de cuenta debe tener máximo 16 dígitos")
    @Positive(message = "El número de cuenta debe ser positivo")
    private Long accountNumber;

    @NotNull(message = "El banco es obligatorio")
    private Long bankId;

    @NotNull(message = "El tipo de cuenta es obligatorio")
    private AccountType accountType;

    @NotNull(message = "La cuenta contable es obligatoria")
    private Long accountingAccountId;

    @Builder.Default
    private Boolean status = true;
}
