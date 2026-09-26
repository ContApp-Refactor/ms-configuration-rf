package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO that contains details of a write-off operation
 * This DTO includes the amount written off and a summary of the associated invoice
 */
@Getter
@Setter
@Builder
public class WriteOffDetailResponse {
    private Long amountWrittenOff; 
    private InvoiceSummaryResponse invoice; 
}
