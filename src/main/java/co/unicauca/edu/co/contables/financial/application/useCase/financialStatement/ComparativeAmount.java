package co.unicauca.edu.co.contables.financial.application.useCase.financialStatement;

import java.math.BigDecimal;

public record ComparativeAmount(
        BigDecimal current,
        BigDecimal previous
) {
}
