package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO that represents the details of a write-off request for a specific invoice
 */
@Getter
@Setter
public class WriteOffDetailRequest {
    @NotNull(message = "Invoice ID cannot be null.")
    @Positive(message = "Invoice ID must be a positive number.")
    private Long invoiceId;
}
