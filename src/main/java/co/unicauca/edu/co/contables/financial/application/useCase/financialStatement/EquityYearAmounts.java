package co.unicauca.edu.co.contables.financial.application.useCase.financialStatement;

import java.math.BigDecimal;

public record EquityYearAmounts(
        BigDecimal capital,
        BigDecimal netIncome,
        BigDecimal retainedEarnings,
        BigDecimal reserves,
        BigDecimal totalEquity
) {
}
