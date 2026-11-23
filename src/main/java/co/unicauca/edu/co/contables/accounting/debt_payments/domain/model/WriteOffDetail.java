package co.unicauca.edu.co.contables.accounting.debt_payments.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @brief Represents a detail line for a write-off, linking to an invoice and the amount written off.
 */
@Getter
@Setter
@Builder
public class WriteOffDetail {
    private Long id;
    private Long invoiceId;
    private Long amountWrittenOff;
    private Long accountingAccount;
}
