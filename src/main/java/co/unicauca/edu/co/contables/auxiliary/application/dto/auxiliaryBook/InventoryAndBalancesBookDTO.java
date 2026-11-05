package co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @brief DTO para filas del Libro de Inventarios y Balances.
 *
 * Representa la estructura de una entrada del libro de inventarios y
 * balances con la cuenta contable asociada, una descripción adicional y
 * el valor correspondiente, usado para transferir datos entre capas.
 */
@Data
@AllArgsConstructor @NoArgsConstructor
public class InventoryAndBalancesBookDTO {
    /**
     * @brief Account related with the book
     */
    private AccountDTO account;

    /**
     * @brief Additional description for the output
     */
    private String description;

    /**
     * @brief Value associated with the output
     */
    private BigDecimal value;

    public Long getAccountCode() {
        return account != null ? account.getAccountCode() : null;
    }

    public String getAccountDescription() {
        return account != null ? account.getAccountDescription() : null;
    }
}
