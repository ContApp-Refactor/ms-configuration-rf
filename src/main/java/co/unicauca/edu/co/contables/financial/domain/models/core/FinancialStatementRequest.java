package co.unicauca.edu.co.contables.financial.domain.models.core;

import co.unicauca.edu.co.contables.financial.domain.models.enums.EFinancialStatementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinancialStatementRequest {
    private String entId;
    private EFinancialStatementType type;
    private FinancialStatementCriteria criteria;
}