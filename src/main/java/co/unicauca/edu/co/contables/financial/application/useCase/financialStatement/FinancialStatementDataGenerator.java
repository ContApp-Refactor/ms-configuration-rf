package co.unicauca.edu.co.contables.financial.application.useCase.financialStatement;

import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementDataPayload;
import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FinancialStatementDataGenerator {

    private final FinancialStatementGenerationStrategyResolver strategyResolver;

    public FinancialStatementDataPayload generate(FinancialStatementRequest request) {
        if (request == null || request.getType() == null) {
            throw new IllegalArgumentException("El tipo de reporte de la solicitud es obligatorio.");
        }

        return strategyResolver.resolve(request.getType()).generate(request);
    }
}
