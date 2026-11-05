package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class InvoiceSummaryEventDto {
    private Long id;
    private String factCode;
    private Long totalValue;
    private Long pendingValue;
    private LocalDate expirationDate;
    private Long accountingAccount;
}
