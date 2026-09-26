package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for request body when creating a portfolio write-off
 */
@Getter
@Setter
public class CreateWriteOffRequest {
    @NotBlank(message = "Justification is required.")
    @Size(max = 500, message = "Justification cannot exceed 500 characters.")
    private String justification;

    @NotNull(message = "Write-off date is required.")
    @FutureOrPresent(message = "Write-off date cannot be in the past.")
    private LocalDate writeOffDate;

    @NotNull(message = "Debit auxiliary account is required.")
    private Long debitAuxiliaryAccount;

    @NotNull(message = "Debit auxiliary account ID is required.")
    private Long debitAuxiliaryAccountId;

    @NotNull(message = "Third ID is required.")
    private Long thirdId;
    
    @NotBlank(message = "Enterprise ID is required.")
    private String enterpriseId;

    private Long costCenterId;

    @NotEmpty(message = "At least one invoice detail must be provided.")
    @Valid 
    private List<WriteOffDetailRequest> details;
}
