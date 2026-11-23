package co.unicauca.edu.co.contables.financial.application.useCase.financialStatement;

import co.unicauca.edu.co.contables.financial.domain.models.enums.EFinancialStatementType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class FinancialStatementReportNameResolver {

    public String resolve(EFinancialStatementType type) {
        if (type == null) {
            return "Financial Statement";
        }

        return switch (type) {
            case STATEMENT_FINANCIAL_POSITION -> "Estado de Situacion Financiera";
            case INCOME_STATEMENT -> "Estado de Resultados";
            case STATEMENT_CHANGES_EQUITY -> "Estado de Cambios en el Patrimonio";
        };
    }

    public String resolve(String rawType) {
        if (!StringUtils.hasText(rawType)) {
            return "Financial Statement";
        }

        try {
            return resolve(EFinancialStatementType.valueOf(rawType.trim().toUpperCase()));
        } catch (IllegalArgumentException exception) {
            return rawType;
        }
    }
}
