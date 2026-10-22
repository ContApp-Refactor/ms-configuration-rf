package co.unicauca.edu.co.contables.financial.infrastructure.out.export;

import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementRow;

import java.util.List;
import java.util.Map;

public interface FinancialStatementTableModelResolver {

    FinancialStatementTableModel resolve(
            String reportName,
            String enterpriseName,
            List<Map<String, Object>> rawRows,
            List<FinancialStatementRow> typedRows,
            Map<String, Object> financialStatement,
            List<String> annotationTexts,
            List<FinancialStatementSignatureBlock> signatures
    );
}