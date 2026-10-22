package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.SkeletonFactureType;
import co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model.PurchaseInvoiceStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkeletonFactureDetailDto {
    private Long id;
    private Long factCode;
    private String entId;
    private Long thId;
    private Set<SkeletonProductDto> products;
    private String totalValue;
    private String totalPay;
    private String pendingValue;
    private LocalDate issueDate;
    private LocalDate expirationDate;
    private Long accountingAccount;
    private SkeletonFactureType factureType;
    private LocalDateTime createdAt;
    private PurchaseInvoiceStatus purchaseStatus;
    private long version;
}
