package co.unicauca.edu.co.contables.financial.infrastructure.out.export;

import co.unicauca.edu.co.contables.financial.domain.models.enums.EReportExportFormat;

public interface FinancialStatementDocumentRenderer {

    FinancialStatementExportService.ExportedFile export(
            EReportExportFormat format,
            FinancialStatementTableModel model,
            FinancialStatementExportService.ExportStyle exportStyle
    );
}