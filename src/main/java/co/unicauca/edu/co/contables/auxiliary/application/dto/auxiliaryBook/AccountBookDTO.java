package co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief DTO para filas del Libro Auxiliar de Cuentas.
 *
 * Representa una línea del libro auxiliar por cuenta, transportando
 * datos del movimiento contable (débito, crédito y saldo) junto con
 * información del tercero y del comprobante entre capas de la aplicación.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountBookDTO {
    private String date;
    private AccountDTO account;
    private BigDecimal debitMovement;
    private BigDecimal creditMovement;
    private BigDecimal balanceMovement;
    private String thirdPartyId;
    private String thirdPartyName;
    private String voucherCostCenter;
    private String voucherNumber;

    public Long getAccountCode() {
        return account != null ? account.getAccountCode() : null;
    }

    public String getAccountDescription() {
        return account != null ? account.getAccountDescription() : null;
    }
}
