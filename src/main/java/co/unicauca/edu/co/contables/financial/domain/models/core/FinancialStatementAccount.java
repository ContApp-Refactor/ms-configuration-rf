package co.unicauca.edu.co.contables.financial.domain.models.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinancialStatementAccount {
    private String accountCode;
    private String accountDescription;
    private String nature;
}