package co.unicauca.edu.co.contables.financial.application.useCase.financialStatement;

import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementDataPayload;
import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementRequest;
import co.unicauca.edu.co.contables.financial.domain.models.enums.EFinancialStatementType;

public interface FinancialStatementGenerationStrategy {

    EFinancialStatementType supports();

    FinancialStatementDataPayload generate(FinancialStatementRequest request);
}
