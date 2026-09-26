package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @brief DTO for response body when retrieving receipt information
 */

@Getter
@Setter
public class ReceiptResponse {
    private Long id;
    private String receiptCode;
    private Long thirdPartyId;
    private Long paymentMethodId;
    private Long paymentMethodAccount;
    private String enterpriseId;
    private Long receiptTypeId;
    private String status;
    private LocalDate issueDate;
    private BigDecimal totalAmount;
    private String observations;
    private Long ledgerAccountId;
    private List<ReceiptDetailResponse> details;
}
