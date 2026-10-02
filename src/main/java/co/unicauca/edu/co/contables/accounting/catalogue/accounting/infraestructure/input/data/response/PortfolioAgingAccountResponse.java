package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PortfolioAgingAccountResponse {
    private String accountCode;
    private String accountName;

    @Builder.Default
    private BigDecimal totalAdeudado = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal corriente = BigDecimal.ZERO; // No vencido
    @Builder.Default
    private BigDecimal dias1a30 = BigDecimal.ZERO;  // 1 a 30 días vencido
    @Builder.Default
    private BigDecimal dias31a60 = BigDecimal.ZERO; // 31 a 60 días vencido
    @Builder.Default
    private BigDecimal dias61a90 = BigDecimal.ZERO; // 61 a 90 días vencido
    @Builder.Default
    private BigDecimal masDe90dias = BigDecimal.ZERO; // +90 días vencido
    
    @Builder.Default
    private List<PortfolioAgingAccountResponse> children = new ArrayList<>();

    @Builder.Default
    private List<InvoiceDetailResponse> documents = new ArrayList<>();
}
