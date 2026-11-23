package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.input.data.response;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientPortfolioSummaryResponse {
    private Long clientId;
    private BigDecimal totalDebt;
    private BigDecimal overdueAmount;
    private BigDecimal dueToAmount;
}
