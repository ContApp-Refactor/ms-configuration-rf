package co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @brief DTO para filas del Libro Auxiliar por Tercero.
 *
 * Representa una fila del libro auxiliar por tercero con los datos de
 * la cuenta, el tercero, el comprobante y los movimientos de débito,
 * crédito y saldo, usada para transferir los datos entre capas.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThirdPartyBookDTO {
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
