package co.unicauca.edu.co.contables.financial.infrastructure.out.export;

import java.math.BigDecimal;
import java.util.Map;

record EquityMatrixRow(
        String description,
        String rowType,
        Map<Integer, BigDecimal> valuesByYear
) {
}