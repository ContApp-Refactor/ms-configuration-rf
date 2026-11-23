package co.unicauca.edu.co.contables.auxiliary.application.useCase.export.report;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.auxiliary.application.useCase.export.report.builders.ReportColumnsBuilder;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.export.report.builders.ReportDataBuilder;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.export.report.builders.ReportStyleBuilder;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.AuxiliaryBookTemplate;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.ExportInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;

/**
 * @brief Coordinador principal de la generación de reportes.
 *
 * Orquesta {@link ReportStyleBuilder}, {@link ReportColumnsBuilder} y
 * {@link ReportDataBuilder} para construir un {@link JasperReportBuilder}
 * completo a partir de la información de exportación, listo para ser
 * renderizado en PDF o Excel.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportGenerator {

    private final ReportColumnsBuilder reportColumnsBuilder;
    private final ReportDataBuilder reportDataBuilder;
    private final ReportStyleBuilder reportStyleBuilder;


    public JasperReportBuilder generate(ExportInfo exportInfo) {
        try{
            AuxiliaryBookTemplate template = exportInfo.getInfoReportTemplate();

            JasperReportBuilder report = DynamicReports.report();

            //Style
            this.reportStyleBuilder.templateBuilder(report, template, exportInfo);

            // Columns
            this.reportColumnsBuilder.setColumnsReport(report, exportInfo.getAuxiliaryBook().getType());

            // Data source
            this.reportDataBuilder.setDataSource(report, exportInfo);

            return report;
        } catch (Exception e) {
            log.error("Error generating report — caused by: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating report", e);
        }
    }
}
