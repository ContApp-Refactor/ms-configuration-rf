package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.context.annotation.RequestScope;

import java.time.LocalDate;

@RequestScope
@Getter
@Setter
public class InvoiceDetailEventDto {
    private Long invoiceId;
    private Long invoiceCode;
    private LocalDate expirationDate;
    private Long totalAmount;
    private Long pendingValue;
}
