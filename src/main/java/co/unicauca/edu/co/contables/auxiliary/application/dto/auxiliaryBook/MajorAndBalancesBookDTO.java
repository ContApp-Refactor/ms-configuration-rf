package co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @brief DTO para filas del Libro Mayor y Balances.
 *
 * Representa una fila del libro mayor y balances con la cuenta contable,
 * el saldo inicial, los movimientos de débito y crédito del periodo y el
 * saldo final, usada para transferir los resultados entre capas.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MajorAndBalancesBookDTO {
    private AccountDTO account;
    private BigDecimal initialBalance;
    private BigDecimal debitMovement;
    private BigDecimal creditMovement;
    private BigDecimal finalBalance;

    public Long getAccountCode() {
        return account != null ? account.getAccountCode() : null;
    }

    public String getAccountDescription() {
        return account != null ? account.getAccountDescription() : null;
    }
}
