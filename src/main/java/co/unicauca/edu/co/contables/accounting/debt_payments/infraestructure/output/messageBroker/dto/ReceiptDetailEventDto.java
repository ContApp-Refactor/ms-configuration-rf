package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ReceiptDetailEventDto {
    private Long invoiceId;           
    private BigDecimal amountPaid;   
    private String invoiceCode; 
    private Long accountingAccount;
}
