package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueExportInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueExcelValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogueTemplateData;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.ExportJobStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.input.rest.dto.request.AccountCatalogueExportRequest;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.utils.ExcelStyleHelper;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.ExcelValidationException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.utils.ExcelFileNameGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * @brief Servicio para exportación del catálogo de cuentas a formato Excel
 *
 * Coordina el inicio de exportaciones asíncronas y gestiona el seguimiento
 * del estado de los trabajos de exportación.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountCatalogueExportService implements IAccountCatalogueExportInputPort {

    private final AccountCatalogueExcelValidationService excelValidationService;
    private final AccountCatalogueExportJobTracker jobTracker;
    private final AccountCatalogueAsyncExportProcessor asyncExportProcessor;
    private final ExcelFileNameGenerator fileNameGenerator;

    private static final String TEMPLATE_PLACEHOLDER_TEXT = "Seleccionar...";

    /**
     * @brief Genera plantilla Excel vacía con validaciones pre-aplicadas
     * @param entId ID de empresa para contexto de plantilla
     * @return archivo Excel con estructura y validaciones listas para uso
     */
    @Override
    public Resource exportAccountCatalogueTemplate(String entId) {
        try {
            byte[] templateData = generateTemplateWithValidations(entId);
            return new ByteArrayResource(templateData);
        } catch (ExcelValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al generar plantilla", e);
        }
    }

    /**
     * @brief Inicia la exportación asíncrona de catálogo de cuentas
     * @param request solicitud con parámetros de exportación (entId, companyName, status)
     * @return jobId único para rastrear el estado de la exportación
     */
    @Override
    public String exportAccountCatalogueAsync(AccountCatalogueExportRequest request) {
        String fileName = fileNameGenerator.generateExportFileName(
                request.getEntId(),
                request.getCompanyName(),
                request.getStatus()
        );

        String jobId = jobTracker.createJob(request.getEntId(), fileName);

        asyncExportProcessor.processExportAsync(request.getEntId(), request.getStatus(), jobId);

        return jobId;
    }

    /**
     * @brief Obtiene el estado actual de una exportación asíncrona
     * @param jobId identificador del trabajo de exportación
     * @return Optional con el estado del trabajo si existe
     */
    @Override
    public Optional<ExportJobStatus> getExportStatus(String jobId) {
        return jobTracker.getJobStatus(jobId);
    }

    /**
     * @brief Crea archivo Excel de plantilla con ejemplos y validaciones aplicadas
     * @param entId ID de empresa para contexto de plantilla
     * @return archivo Excel con estructura y validaciones listas para uso
     * @throws IOException si ocurre un error al crear el archivo Excel
     * @throws ExcelValidationException si ocurre un error al aplicar las validaciones
     */
    private byte[] generateTemplateWithValidations(String entId) throws IOException, ExcelValidationException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Plantilla_Catalogo_Cuentas");

            // Crear estilos
            CellStyle headerStyle = ExcelStyleHelper.createHeaderStyle(workbook);
            CellStyle templateStyle = ExcelStyleHelper.createTemplateStyle(workbook);

            // Crear encabezados
            createHeaders(sheet, headerStyle, ExcelStyleHelper.createOptionalHeaderStyle(workbook));

            // Crear filas de ejemplo con estilos
            List<AccountCatalogueTemplateData> exampleData = getHardcodedTemplateData();
            createTemplateRows(sheet, templateStyle, exampleData);

            // Aplicar validaciones de datos
            applyValidationsToTemplate(sheet);

            // Ajustar ancho de columnas
            autoSizeColumns(sheet);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }


    /**
     * @brief Crea encabezados en la hoja de Excel
     * @param sheet hoja de Excel donde crear los encabezados
     * @param requiredHeaderStyle estilo para encabezados requeridos
     * @param optionalHeaderStyle estilo para encabezados opcionales
     */
    private void createHeaders(Sheet sheet, CellStyle requiredHeaderStyle, CellStyle optionalHeaderStyle) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "Código\n(Requerido)",
            "Nombre\n(Requerido)",
            "Naturaleza\n(Requerido)",
            "Estado Financiero\n(Requerido)",
            "Clasificación\n(Requerido)",
            "Cruce\n(Opcional)",
            "Centro de Costo\n(Opcional)"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            if (i >= 5) { // Columnas 5 y 6 son opcionales
                cell.setCellStyle(optionalHeaderStyle);
            } else {
                cell.setCellStyle(requiredHeaderStyle);
            }
        }

        headerRow.setHeightInPoints(40);
    }

    /**
     * @brief Crea filas de ejemplo en la hoja de Excel
     * @param sheet hoja de Excel donde crear las filas de ejemplo
     * @param templateStyle estilo para celdas de plantilla
     * @param exampleData datos de ejemplo a exportar
     */
    private void createTemplateRows(Sheet sheet, CellStyle templateStyle, List<AccountCatalogueTemplateData> exampleData) {
        int rowIndex = 1;
        for (AccountCatalogueTemplateData data : exampleData) {
            Row row = sheet.createRow(rowIndex++);
            fillTemplateRow(row, data, templateStyle);
        }
    }

    /**
     * @brief Llena una fila de ejemplo en la hoja de Excel
     * @param row fila de Excel donde llenar la fila de ejemplo
     * @param data datos de ejemplo a exportar
     * @param style estilo para celdas de plantilla
     */
    private void fillTemplateRow(Row row, AccountCatalogueTemplateData data, CellStyle style) {
        int colIndex = 0;
        createTemplateCell(row, colIndex++, data.getCode(), style);
        createTemplateCell(row, colIndex++, data.getName(), style);
        createTemplateCell(row, colIndex++, data.getNature() != null ? data.getNature().getState() : TEMPLATE_PLACEHOLDER_TEXT, style);
        createTemplateCell(row, colIndex++, data.getFinancialStatus() != null ? data.getFinancialStatus().getState() : TEMPLATE_PLACEHOLDER_TEXT, style);
        createTemplateCell(row, colIndex++, data.getClassification() != null ? data.getClassification().getState() : TEMPLATE_PLACEHOLDER_TEXT, style);
        createTemplateCell(row, colIndex++, "", style);
        createTemplateCell(row, colIndex++, "", style);
    }

    /**
     * @brief Crea una celda en la hoja de Excel
     * @param row fila de Excel donde crear la celda
     * @param colIndex índice de la columna
     * @param value valor a colocar en la celda
     * @param style estilo a aplicar a la celda
     */
    private void createTemplateCell(Row row, int colIndex, String value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }


    /**
     * @brief Aplica validaciones a la hoja de Excel de plantilla
     * @param sheet hoja de Excel donde aplicar las validaciones
     * @throws ExcelValidationException si ocurre un error al aplicar las validaciones
     */
    private void applyValidationsToTemplate(Sheet sheet) throws ExcelValidationException {
        int startRow = 1; // Después del encabezado
        int endRow = 1000; // Permitir muchas filas para la plantilla

        excelValidationService.applyAccountCatalogueValidations(sheet, startRow, endRow);
    }

    /**
     * @brief Ajusta el ancho de las columnas en la hoja de Excel
     * @param sheet hoja de Excel donde ajustar el ancho de las columnas
     */
    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < 7; i++) {
            sheet.autoSizeColumn(i);
            if (sheet.getColumnWidth(i) < 3000) {
                sheet.setColumnWidth(i, 3000);
            }
            if (sheet.getColumnWidth(i) > 8000) {
                sheet.setColumnWidth(i, 8000);
            }
        }
    }



    /**
     * @brief Obtiene datos para la plantilla de Excel
     */
    private List<AccountCatalogueTemplateData> getHardcodedTemplateData() {
        return List.of(
            AccountCatalogueTemplateData.builder()
                .code("1")
                .name("Activos")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .classification(ClassificationEnum.NONCURRENTLIABILITIES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("11")
                .name("Efectivo y equivalentes de efectivo")
                .nature(NatureEnum.CREDIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("1105")
                .name("Caja")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTLIABILITIES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("110501")
                .name("Caja principal")
                .nature(NatureEnum.CREDIT)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .classification(ClassificationEnum.NONCURRENTLIABILITIES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("11050101")
                .name("Banco menor")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.OPERATINGEXPENSES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("11050102")
                .name("Banco")
                .nature(NatureEnum.CREDIT)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .classification(ClassificationEnum.NONCURRENTLIABILITIES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("110502")
                .name("Caja menor")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .classification(ClassificationEnum.NONCURRENTLIABILITIES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("1106")
                .name("Reservas internacionales")
                .nature(NatureEnum.CREDIT)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .classification(ClassificationEnum.OPERATINGEXPENSES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("12")
                .name("Inversiones e instrumentos derivados")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.NONCURRENTLIABILITIES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("1205")
                .name("Propiedades, planta y equipo")
                .nature(NatureEnum.CREDIT)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .classification(ClassificationEnum.NONCURRENTLIABILITIES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("1206")
                .name("Activos intangibles")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.OPERATINGEXPENSES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("2")
                .name("Pasivos")
                .nature(NatureEnum.CREDIT)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .classification(ClassificationEnum.NONCURRENTLIABILITIES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("21")
                .name("Operaciones de banca central e instituciones financieras")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.NONCURRENTLIABILITIES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("2105")
                .name("Cuentas por pagar")
                .nature(NatureEnum.CREDIT)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .classification(ClassificationEnum.NONCURRENTASSETS)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("2106")
                .name("Obligaciones financieras corrientes")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.OPERATINGREVENUES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("22")
                .name("Emisión y colocación de títulos de deuda")
                .nature(NatureEnum.CREDIT)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .classification(ClassificationEnum.NONCURRENTLIABILITIES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("2205")
                .name("Obligaciones financieras no corrientes")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.OPERATINGREVENUES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("2206")
                .name("Beneficios a empleados a largo plazo")
                .nature(NatureEnum.CREDIT)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .classification(ClassificationEnum.NONCURRENTLIABILITIES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("3")
                .name("Patrimonio")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.NONCURRENTLIABILITIES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("31")
                .name("Capital social")
                .nature(NatureEnum.CREDIT)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .classification(ClassificationEnum.OPERATINGREVENUES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("32")
                .name("Utilidades acumuladas")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.NONCURRENTLIABILITIES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("33")
                .name("Utilidades NO acumuladas")
                .nature(NatureEnum.CREDIT)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .classification(ClassificationEnum.OPERATINGREVENUES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("4")
                .name("Ingresos")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .classification(ClassificationEnum.NONCURRENTLIABILITIES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("5")
                .name("Gastos")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .classification(ClassificationEnum.NONCURRENTLIABILITIES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("6")
                .name("Costos de ventas")
                .nature(NatureEnum.CREDIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.OPERATINGREVENUES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("7")
                .name("Costos de transformación")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.OPERATINGREVENUES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("8")
                .name("Cuentas de orden deudoras")
                .nature(NatureEnum.CREDIT)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .classification(ClassificationEnum.OPERATINGREVENUES)
                .build(),
            AccountCatalogueTemplateData.builder()
                .code("9")
                .name("Cuentas de orden acreedoras")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.OPERATINGREVENUES)
                .build()
        );
    }


}