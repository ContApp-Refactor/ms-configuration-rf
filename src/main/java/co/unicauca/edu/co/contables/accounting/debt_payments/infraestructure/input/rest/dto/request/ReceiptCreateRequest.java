package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @brief DTO for request body when creating a receipt
 */

@Getter
@Setter
public class ReceiptCreateRequest {

    @NotNull(message = "Third party ID cannot be null")
    private Long thirdPartyId;

    @NotNull(message = "Payment method ID cannot be null")
    private Long paymentMethodId;

    @NotNull(message = "Payment method account cannot be null")
    private Long paymentMethodAccount;

    @NotNull(message = "Receipt type ID cannot be null")
    private Long receiptTypeId;

    private String observations;

    private Long ledgerAccountId;

    private Long centerCostId;

    private String enterpriseId;

    @NotNull(message = "Total amount cannot be null")
    @Positive(message = "Amount paid must be positive")
    private Long totalAmount;

    @Valid
    private List<ReceiptDetailRequest> details;
}
