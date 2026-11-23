package co.unicauca.edu.co.contables.financial.infrastructure.out.export;

import java.math.BigDecimal;

record FinancialPositionRow(
        String lineDescription,
        String note,
        BigDecimal currentAmount,
        BigDecimal currentPercentage,
        BigDecimal previousAmount,
        BigDecimal previousPercentage,
        BigDecimal variation,
        BigDecimal variationPercentage,
        String rowType
) {
}