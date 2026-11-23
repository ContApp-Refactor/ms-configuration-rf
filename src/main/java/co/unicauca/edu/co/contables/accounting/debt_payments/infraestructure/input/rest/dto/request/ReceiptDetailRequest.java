package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * @brief DTO for request body when adding a receipt detail
 * Represents the details of a payment made towards a specific invoice
 */

@Getter
@Setter
public class ReceiptDetailRequest {
    @NotNull(message = "Invoice ID cannot be null")
    private Long invoiceId;
    
    @NotNull(message = "Amount paid cannot be null")
    @Positive(message = "Amount paid must be positive")
    private Long amountPaid;
}
