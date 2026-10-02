package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.messageBroker.DTO;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptEventDTO {
    private Long id;
    private String receiptCode;
    private Long thirdPartyId;
    private String enterpriseId;  
    private Long receiptTypeId;   
    private Long paymentMethodId;
    private Long paymentMethodAccount; // Añadido para la cuenta del método de pago
    private String status;
    private LocalDate issueDate;
    private BigDecimal totalAmount;
    private String observations;
    private Long ledgerAccountId;
    private Long centerCostId;
    private List<ReceiptDetailEventDTO> details;
}