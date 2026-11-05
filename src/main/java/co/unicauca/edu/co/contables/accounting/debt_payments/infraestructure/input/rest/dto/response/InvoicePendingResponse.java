package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * @brief DTO for response body when retrieving pending invoice information 
 */

@Getter
@Setter
public class InvoicePendingResponse {
    private Long id;
    private String factCode;
    private Long pendingValue;
    private Long thirdId;
    private Long totalValue;
    private LocalDate creationDate;
    private LocalDate expirationDate;
}
