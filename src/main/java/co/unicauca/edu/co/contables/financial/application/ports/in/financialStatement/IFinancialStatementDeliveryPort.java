package co.unicauca.edu.co.contables.financial.application.ports.in.financialStatement;

import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementDocument;
import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementExportCommand;
import co.unicauca.edu.co.contables.financial.domain.models.enums.EReportExportFormat;

import java.util.UUID;

public interface IFinancialStatementDeliveryPort {

    FinancialStatementDocument export(FinancialStatementExportCommand command);

    FinancialStatementDocument download(UUID reportId, EReportExportFormat format);
}