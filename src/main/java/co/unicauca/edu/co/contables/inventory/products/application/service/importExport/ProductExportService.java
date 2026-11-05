package co.unicauca.edu.co.contables.inventory.products.application.service.importExport;

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductExportUseCase;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.ErrorCode;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ExcelValidationException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductExportException;
import co.unicauca.edu.co.contables.inventory.products.domain.model.ExportJobStatus;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.dto.request.ProductExportRequest;
import co.unicauca.edu.co.contables.inventory.products.infraestructure.utils.ExcelFileNameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

/**
 * @brief Servicio para exportar productos en formato Excel
 *
 * Maneja la exportación de productos existentes y generación de plantillas Excel
 * con validaciones de datos integradas. Implementa paginación automática para
 * grandes volúmenes de datos y resolución de entidades relacionadas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductExportService implements IProductExportUseCase {

    private final ProductExcelValidationService excelValidationService;
    private final ProductExportJobTracker exportJobTracker;
    private final ProductAsyncExportProcessor asyncExportProcessor;
    private final ExcelFileNameGenerator fileNameGenerator;

   
    private static final String SELECT_PLACEHOLDER = "Seleccionar...";

    /**
     * @brief Crea estilo para encabezados con fondo azul oscuro y texto blanco
     *
     * Configura un estilo visual profesional para encabezados de columnas con
     * fondo azul oscuro, texto blanco en negrita, bordes delgados y alineación centrada
     * para mejorar la legibilidad y apariencia profesional del documento Excel.
     *
     * @param workbook Libro de trabajo Excel donde crear el estilo
     * @return Estilo configurado para encabezados de columnas
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    /**
     * @brief Crea estilo para encabezados opcionales con fondo gris claro
     *
     * Configura un estilo visual distintivo para encabezados de campos opcionales
     * con fondo gris claro, texto negro en negrita y bordes delgados. Este estilo
     * ayuda a diferenciar visualmente los campos obligatorios de los opcionales.
     *
     * @param workbook Libro de trabajo Excel donde crear el estilo
     * @return Estilo configurado para encabezados de campos opcionales
     */
    private CellStyle createOptionalHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    /**
     * @brief Crea fila de encabezados con estilos diferenciados para campos requeridos/opcionales
     *
     * Genera la fila de encabezados completa del Excel con 11 columnas específicas
     * para productos, aplicando estilos visuales diferenciados para distinguir
     * campos obligatorios (azul oscuro) de opcionales (gris claro). Incluye
     * indicadores textuales de requerimiento en cada encabezado.
     *
     * @param sheet Hoja de trabajo Excel donde crear los encabezados
     * @param requiredHeaderStyle Estilo para encabezados de campos requeridos
     * @param optionalHeaderStyle Estilo para encabezados de campos opcionales
     */
    private void createHeaders(Sheet sheet, CellStyle requiredHeaderStyle, CellStyle optionalHeaderStyle) {
        Row headerRow = sheet.createRow(0);
        int colIndex = 0;

        createHeaderCell(headerRow, colIndex++, "Código\n(No se requiere)", optionalHeaderStyle);
        createHeaderCell(headerRow, colIndex++, "Nombre\n(Requerido)", requiredHeaderStyle);
        createHeaderCell(headerRow, colIndex++, "Referencia/SKU\n(Requerido)", requiredHeaderStyle);
        createHeaderCell(headerRow, colIndex++, "Presentación\n(Requerido)", requiredHeaderStyle);
        createHeaderCell(headerRow, colIndex++, "Descripción\n(Requerido)", requiredHeaderStyle);
        createHeaderCell(headerRow, colIndex++, "Costo\n(Opcional)", optionalHeaderStyle);
        createHeaderCell(headerRow, colIndex++, "Cantidad\n(Opcional)", optionalHeaderStyle);
        createHeaderCell(headerRow, colIndex++, "Unidad de Medida\n(Requerido)", requiredHeaderStyle);
        createHeaderCell(headerRow, colIndex++, "Categoría\n(Requerido)", requiredHeaderStyle);
        createHeaderCell(headerRow, colIndex++, "Tipo de Producto\n(Requerido)", requiredHeaderStyle);
        createHeaderCell(headerRow, colIndex++, "Estado\n(No se requiere)", optionalHeaderStyle);

        // Ajustar altura de la fila de encabezados para mostrar múltiples líneas
        headerRow.setHeightInPoints(35);
    }

    /**
     * @brief Crea celda de encabezado con valor y estilo especificados
     *
     * Método auxiliar para crear celdas de encabezado de manera consistente,
     * aplicando el valor de texto y el estilo visual correspondiente a cada
     * celda de encabezado en la fila de títulos.
     *
     * @param row Fila donde crear la celda de encabezado
     * @param colIndex Índice de columna donde ubicar la celda
     * @param value Valor de texto para el encabezado
     * @param style Estilo visual a aplicar a la celda
     */
    private void createHeaderCell(Row row, int colIndex, String value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }


    @Override
    public Resource exportProductTemplateWithValidations(String entId) {
        try {
            byte[] templateData = generateTemplateWithValidations(entId);
            return new ByteArrayResource(templateData);
        } catch (Exception e) {
            throw new ExcelValidationException(ErrorCode.EXCEL_VALIDATION_ERROR,
                    "Error al generar plantilla", e);
        }
    }

    /**
     * @brief Inicia exportación asíncrona de productos
     * @param exportRequest solicitud de exportación con filtros
     * @return jobId único para consultar el estado
     */
    @Override
    public String exportProductsAsync(ProductExportRequest exportRequest) {
        try {
            // Generar nombre de archivo descriptivo
            String fileName = fileNameGenerator.generateExportFileName(
                    exportRequest.getEntId(), 
                    exportRequest.getCompanyName(), 
                    exportRequest.getStatus());
            
            // Crear job de exportación y obtener ID (síncrono, retorna inmediatamente)
            String jobId = exportJobTracker.createJob(exportRequest.getEntId(), fileName);

            // Ejecutar exportación de forma asíncrona usando servicio separado
            asyncExportProcessor.processExportAsync(exportRequest, jobId);

            return jobId;

        } catch (Exception e) {
            throw new ProductExportException(ErrorCode.PRODUCT_EXPORT_ERROR,
                    "Error al iniciar la exportación: " + e.getMessage(), e);
        }
    }

    /**
     * @brief Obtiene el estado de un job de exportación
     * @param jobId identificador único del job
     * @return estado del job si existe
     */
    @Override
    public Optional<ExportJobStatus> getExportStatus(String jobId) {
        return exportJobTracker.getJobStatus(jobId);
    }

    /**
     * @brief Genera archivo Excel de plantilla con filas de ejemplo y validaciones aplicadas
     *
     * Crea un archivo Excel completo con estructura de plantilla: encabezados con estilos,
     * 10 filas de ejemplo para guiar al usuario, validaciones de datos aplicadas
     * y ajuste automático de columnas para una experiencia óptima de uso.
     *
     * @param entId ID de la empresa para obtener datos de validación de entidades relacionadas
     * @return Arreglo de bytes con el archivo Excel de plantilla completo
     * @throws IOException si ocurre error al escribir el archivo Excel
     */
    private byte[] generateTemplateWithValidations(String entId) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Plantilla_Productos");

            // Crear estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle templateStyle = createTemplateStyle(workbook);

            createHeaders(sheet, headerStyle, createOptionalHeaderStyle(workbook));

            // Crear filas de ejemplo con estilos
            createTemplateRows(sheet, templateStyle, 10); // 10 filas de ejemplo

            // Aplicar validaciones de datos
            applyValidationsToTemplate(sheet, entId);

            // Ajustar ancho de columnas
            for (int i = 0; i < 11; i++) {
                sheet.autoSizeColumn(i);
                int autoWidth = sheet.getColumnWidth(i);
                int minWidth = 1500;
                int maxWidth = 25000;
                if (autoWidth < minWidth) {
                    sheet.setColumnWidth(i, minWidth);
                } else if (autoWidth > maxWidth) {
                    sheet.setColumnWidth(i, maxWidth);
                }
                int finalWidth = Math.min(Math.max(autoWidth + 500, minWidth), maxWidth);
                sheet.setColumnWidth(i, finalWidth);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * @brief Crea estilo para celdas de plantilla con bordes delgados
     *
     * Configura un estilo básico para celdas de plantilla con bordes delgados
     * y alineación vertical centrada, adecuado para filas de ejemplo y celdas
     * vacías que el usuario debe completar.
     *
     * @param workbook Libro de trabajo Excel donde crear el estilo
     * @return Estilo configurado para celdas de plantilla
     */
    private CellStyle createTemplateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * @brief Crea filas de ejemplo en la plantilla con datos de muestra
     *
     * Genera filas de ejemplo en la plantilla Excel para guiar al usuario:
     * la primera fila contiene datos de ejemplo completos, mientras que las
     * filas adicionales se crean vacías para que el usuario las complete.
     *
     * @param sheet Hoja de trabajo Excel donde crear las filas de ejemplo
     * @param templateStyle Estilo visual para las celdas de plantilla
     * @param numberOfRows Número total de filas de ejemplo a crear
     */
    private void createTemplateRows(Sheet sheet, CellStyle templateStyle, int numberOfRows) {
        for (int i = 1; i <= numberOfRows; i++) {
            Row row = sheet.createRow(i);
            int colIndex = 0;

            if (i == 1) {
                // Primera fila con indicadores (estilo normal)
                createHeaderCell(row, colIndex++, "", templateStyle);
                createHeaderCell(row, colIndex++, "Producto de Ejemplo", templateStyle);
                createHeaderCell(row, colIndex++, "REF001", templateStyle);
                createHeaderCell(row, colIndex++, "Caja x 12", templateStyle);
                createHeaderCell(row, colIndex++, "Descripción del producto", templateStyle);
                createHeaderCell(row, colIndex++, "0", templateStyle); // Costo
                createHeaderCell(row, colIndex++, "0", templateStyle); // Cantidad
                createHeaderCell(row, colIndex++, SELECT_PLACEHOLDER, templateStyle); // Unidad de Medida
                createHeaderCell(row, colIndex++, SELECT_PLACEHOLDER, templateStyle); // Categoría
                createHeaderCell(row, colIndex++, SELECT_PLACEHOLDER, templateStyle); // Tipo de Producto
                createHeaderCell(row, colIndex++, "", templateStyle); // Estado
            } else {
                // Filas adicionales vacías con el mismo estilo
                createEmptyTemplateRow(row, templateStyle);
            }
        }
    }

    /**
     * @brief Crea fila vacía en plantilla con todas las columnas inicializadas
     *
     * Inicializa una fila completa de plantilla con 11 columnas vacías,
     * aplicando el estilo de plantilla a cada celda para mantener consistencia
     * visual en filas que el usuario debe completar.
     *
     * @param row Fila de Excel a inicializar con celdas vacías
     * @param templateStyle Estilo visual a aplicar a todas las celdas de la fila
     */
    private void createEmptyTemplateRow(Row row, CellStyle templateStyle) {
        for (int i = 0; i < 11; i++) {
            createHeaderCell(row, i, "", templateStyle);
        }
    }



    /**
     * @brief Aplica validaciones de datos Excel a la plantilla usando servicio especializado
     *
     * Delega la aplicación de todas las validaciones de datos (listas desplegables,
     * formatos numéricos, textos obligatorios) al servicio especializado de validaciones,
     * configurando un amplio rango de filas para permitir extensas plantillas.
     *
     * @param sheet Hoja de trabajo Excel donde aplicar las validaciones
     * @param entId ID de la empresa para obtener datos de entidades relacionadas
     */
    private void applyValidationsToTemplate(Sheet sheet, String entId) {
        int startRow = 1; // Después del encabezado
        int endRow = 1000; // Permitir muchas filas para la plantilla

        excelValidationService.applyProductValidations(sheet, entId, startRow, endRow);
    }

}