package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO;

import java.time.LocalDate;
import java.util.List;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.WriteOffStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.input.rest.dto.response.WriteOffDetailResponse;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
    private WriteOffStatus status;
    private String enterpriseId;
    private Long centerCostId;
    private List<WriteOffDetailResponse> details; // Lista de detalles enriquecidos
}
