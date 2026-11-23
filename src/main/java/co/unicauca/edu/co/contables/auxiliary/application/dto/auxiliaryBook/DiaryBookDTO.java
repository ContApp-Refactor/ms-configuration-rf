package co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @brief DTO para filas del Libro Diario (Diary Book).
 *
 * Representa la estructura de una entrada del libro diario con
 * fecha, cuenta contable, comprobante y los valores de débito y crédito
 * utilizada para transferir los datos entre las capas de la aplicación.
 */
@Data
@AllArgsConstructor @NoArgsConstructor
public class DiaryBookDTO {
    /**
     * @brief Date of the diary book output
     */
    private String date;

    /**
     * @brief Account realted with accounting info
     */
    private AccountDTO account;

    /**
     * @brief Name of the voucher associated with the output
     */
    private String voucherName;

    /**
     * @brief Number of the voucher associated with the output
     */
    private String voucherNumber;

    /**
     * @brief Debit amount for the output
     */
    private BigDecimal debit;

    /**
     * @brief Credit amount for the output
     */
    private BigDecimal credit;

    public Long getAccountCode() {
        return account != null ? account.getAccountCode() : null;
    }

    public String getAccountDescription() {
        return account != null ? account.getAccountDescription() : null;
    }
}
