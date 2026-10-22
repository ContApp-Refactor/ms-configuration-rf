package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class InvoiceDueReminderEventDto {
    private Long thirdPartyId; 
    private List<InvoiceDetailEventDto> invoiceDetails;
}
