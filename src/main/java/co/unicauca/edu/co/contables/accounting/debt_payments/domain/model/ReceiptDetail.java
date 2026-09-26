package co.unicauca.edu.co.contables.accounting.debt_payments.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Represents a detail line in a receipt, linking to an invoice and the amount paid.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptDetail {
    private Long id;
    private Long invoiceId;  
    private String invoiceCode;         
    private Long amountPaid;   
    private Long accountingAccount;
}
