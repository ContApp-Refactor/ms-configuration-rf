package co.unicauca.edu.co.contables.financial.domain.models.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinancialStatementCriteria {
    private String criteriaType;
    private FinancialStatementCriteriaRange criteriaRange;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate previousStartDate;
    private LocalDate previousEndDate;
    private LocalDate currentCutoffDate;
    private LocalDate previousCutoffDate;
}