package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @brief DTO for response body when retrieving receipt detail information
 */

@Getter
@Setter
public class ReceiptDetailResponse {
    private Long invoiceId;           
    private BigDecimal amountPaid;   
    private String invoiceCode; 
    private Long accountingAccount;
}
