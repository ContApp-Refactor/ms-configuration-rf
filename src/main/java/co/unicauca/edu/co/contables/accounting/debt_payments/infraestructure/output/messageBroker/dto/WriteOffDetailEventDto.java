package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.messageBroker.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WriteOffDetailEventDto {
    private Long amountWrittenOff;
    private InvoiceSummaryEventDto invoice;
}
