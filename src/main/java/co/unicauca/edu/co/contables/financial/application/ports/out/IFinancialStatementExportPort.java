package co.unicauca.edu.co.contables.financial.application.ports.out;

import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementExportStyle;
import co.unicauca.edu.co.contables.financial.domain.models.core.FinancialStatementVisualSignature;
import co.unicauca.edu.co.contables.financial.domain.models.enums.EReportExportFormat;

import java.util.List;
import java.util.Map;

public interface IFinancialStatementExportPort {

    ExportedDocument export(
            EReportExportFormat format,
            String reportName,
            String enterpriseName,
            List<Map<String, Object>> rows
    );

    default ExportedDocument export(
            EReportExportFormat format,
            String reportName,
            String enterpriseName,
            List<Map<String, Object>> rows,
            Map<String, Object> financialStatement,
            FinancialStatementExportStyle exportStyle
    ) {
        return export(format, reportName, enterpriseName, rows);
    }

    default ExportedDocument export(
            EReportExportFormat format,
            String reportName,
            String enterpriseName,
            List<Map<String, Object>> rows,
            Map<String, Object> financialStatement,
            FinancialStatementExportStyle exportStyle,
            List<String> annotationTexts,
            List<FinancialStatementVisualSignature> visualSignatures
    ) {
        return export(format, reportName, enterpriseName, rows, financialStatement, exportStyle);
    }

    record ExportedDocument(
            byte[] content,
            String contentType,
            String fileName
    ) {
    }
}