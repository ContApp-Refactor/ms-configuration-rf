package co.unicauca.edu.co.contables.financial.application.useCase.financialStatement;

import co.unicauca.edu.co.contables.financial.application.ports.out.IAccountingInfoClient;
import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementCriteria;
import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementDataPayload;
import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementRequest;
import co.unicauca.edu.co.contables.financial.domain.models.enums.EFinancialStatementType;
import co.unicauca.edu.co.contables.financial.domain.models.external.accountingInfo.AccountingEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EquityChangesGenerationStrategy implements FinancialStatementGenerationStrategy {

    private final IAccountingInfoClient accountingInfoClient;
    private final FinancialStatementGenerationSupport generationSupport;
    private final EquityChangesStatementBuilder equityChangesStatementBuilder;
    private final FinancialStatementRowMapper financialStatementRowMapper;

    @Override
    public EFinancialStatementType supports() {
        return EFinancialStatementType.STATEMENT_CHANGES_EQUITY;
    }

    @Override
    public FinancialStatementDataPayload generate(FinancialStatementRequest request) {
        FinancialStatementCriteria criteria = request != null ? request.getCriteria() : null;
        LocalDate endDate = criteria != null ? criteria.getEndDate() : null;
        LocalDate startDate = criteria != null ? criteria.getStartDate() : null;

        List<AccountingEntry> sourceEntries = accountingInfoClient.findAccountingEntries(request.getEntId(), null, endDate);
        List<AccountingEntry> currentAccountingEntries = generationSupport.filterEntriesUpToCutoff(sourceEntries, endDate);
        List<AccountingEntry> previousAccountingEntries = generationSupport.filterEntriesUpToCutoff(sourceEntries, startDate);

        return FinancialStatementDataPayload.builder()
                .rows(financialStatementRowMapper.toTypedRows(
                        equityChangesStatementBuilder.buildRows(
                                sourceEntries,
                                currentAccountingEntries,
                                previousAccountingEntries,
                                endDate,
                                startDate
                        )
                ))
                .build();
    }
}
