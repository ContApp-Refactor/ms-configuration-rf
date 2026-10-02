package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFactureType;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.PurchaseInvoiceStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkeletonFactureSummaryDto {
    private Long id;
    private Long factCode;
    private String entId;
    private Long thId;
    private String totalValue;
    private SkeletonFactureType factureType;
    private LocalDate issueDate;
    private LocalDateTime createdAt;
    private PurchaseInvoiceStatus purchaseStatus;
    private long version;
}
