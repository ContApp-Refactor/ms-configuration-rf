package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.WriteOffStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * @brief DTO for response body when retrieving portfolio write-off information
 * This DTO includes details about the write-off and its associated invoices
 */

@Getter
@Setter
@Builder
public class PortfolioWriteOffResponse {
    private Long id;
    private String code;
    private String justification;
    private Long totalAmount;
    private LocalDate writeOffDate;
    private Long debitAuxiliaryAccount;
    private Long debitAuxiliaryAccountId;
    private Long thirdId;
    private Long costCenterId;
    private WriteOffStatus status;
    private String enterpriseId;
    private List<WriteOffDetailResponse> details; 
}
