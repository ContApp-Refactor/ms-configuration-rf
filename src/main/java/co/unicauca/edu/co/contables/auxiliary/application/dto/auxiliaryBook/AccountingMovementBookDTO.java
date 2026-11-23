package co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief DTO para filas del Libro Auxiliar de Movimientos Contables.
 *
 * Representa una línea del libro de movimientos contables, incluyendo
 * el comprobante, la cuenta, el tercero, los saldos iniciales y los
 * movimientos de débito, crédito y neto asociados al periodo consultado.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountingMovementBookDTO {
    private String voucherType;
    private String date;
    private String state;
    private String thirdPartyId;
    private String thirdPartyName;
    private AccountDTO account;
    private BigDecimal initialBalance;
    private BigDecimal debitMovement;
    private BigDecimal creditMovement;
    private BigDecimal netMovement;

    public Long getAccountCode() {
        return account != null ? account.getAccountCode() : null;
    }

    public String getAccountDescription() {
        return account != null ? account.getAccountDescription() : null;
    }
}
