package co.unicauca.edu.co.contables.financial.application.useCase.financialStatement;

import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementExportStyle;
import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementTemplate;
import org.springframework.stereotype.Component;

@Component
public class FinancialStatementTemplateExportStyleMapper {

    public FinancialStatementExportStyle toExportStyle(FinancialStatementTemplate template) {
        if (template == null) {
            return null;
        }

        return FinancialStatementExportStyle.builder()
                .pathLogotype(template.getPathLogotype())
                .alignment(template.getAlignment())
                .font(template.getFont())
                .fontSize(template.getFontSize())
                .mainColor(template.getMainColor())
                .build();
    }
}
