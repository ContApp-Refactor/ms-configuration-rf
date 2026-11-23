package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptSalesDtoRequest {
    private Long factCode;
    private String entId;
    private Long thirdId;
    private Long totalPay;
    private Long totalValue;
    private Long pendingValue;
    private LocalDate creationDate;
    private LocalDate expirationDate;
    private boolean active;
    private Long accountingAccount;
}
