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
public class IncomeStatementGenerationStrategy implements FinancialStatementGenerationStrategy {

    private final IAccountingInfoClient accountingInfoClient;
    private final FinancialStatementGenerationSupport generationSupport;
    private final IncomeStatementStatementBuilder incomeStatementStatementBuilder;
    private final FinancialStatementRowMapper financialStatementRowMapper;

    @Override
    public EFinancialStatementType supports() {
        return EFinancialStatementType.INCOME_STATEMENT;
    }

    @Override
    public FinancialStatementDataPayload generate(FinancialStatementRequest request) {
        FinancialStatementCriteria criteria = request != null ? request.getCriteria() : null;
        LocalDate startDate = criteria != null ? criteria.getStartDate() : null;
        LocalDate endDate = criteria != null ? criteria.getEndDate() : null;
        LocalDate previousStartDate = criteria != null ? criteria.getPreviousStartDate() : null;
        LocalDate previousEndDate = criteria != null ? criteria.getPreviousEndDate() : null;

        List<AccountingEntry> accountingEntries = accountingInfoClient.findAccountingEntries(request.getEntId(), startDate, endDate);
        List<AccountingEntry> filteredAccountingEntries = generationSupport.applyCriteriaLevelFilter(accountingEntries, criteria, false);

        LocalDate resolvedPreviousStartDate = previousStartDate != null
                ? previousStartDate
                : (startDate != null ? startDate.minusYears(1) : null);
        LocalDate resolvedPreviousEndDate = previousEndDate != null
                ? previousEndDate
                : (endDate != null ? endDate.minusYears(1) : null);

        List<AccountingEntry> previousAccountingEntries = accountingInfoClient.findAccountingEntries(
                request.getEntId(),
                resolvedPreviousStartDate,
                resolvedPreviousEndDate
        );
        List<AccountingEntry> filteredPreviousAccountingEntries =
                generationSupport.applyCriteriaLevelFilter(previousAccountingEntries, criteria, false);

        return FinancialStatementDataPayload.builder()
                .rows(financialStatementRowMapper.toTypedRows(
                        incomeStatementStatementBuilder.buildRows(
                                filteredAccountingEntries,
                                filteredPreviousAccountingEntries,
                                criteria
                        )
                ))
                .build();
    }
}
