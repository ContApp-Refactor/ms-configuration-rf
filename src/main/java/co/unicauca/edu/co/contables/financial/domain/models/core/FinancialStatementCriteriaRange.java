package co.unicauca.edu.co.contables.financial.domain.models.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinancialStatementCriteriaRange {
    private Long from;
    private Long to;
}