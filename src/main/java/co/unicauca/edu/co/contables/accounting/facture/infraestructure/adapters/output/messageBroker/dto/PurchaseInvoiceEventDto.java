package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.messageBroker.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PurchaseInvoiceEventDto {
    private String eventId;
    private String eventType;
    private Long invoiceId;
    private String reference;
    private String enterpriseId;
    private Long supplierId;
    private BigDecimal originalAmount;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private Long payableAccountId;
    private String payableAccountCode;
    private boolean active;
    private String tenantId;
}
