package co.unicauca.edu.co.contables.financial.infrastructure.out.export;

import java.util.List;
import java.util.Map;

record FlattenedFinancialStatementTable(
        List<Map<String, String>> rows,
        List<String> columns
) {
}