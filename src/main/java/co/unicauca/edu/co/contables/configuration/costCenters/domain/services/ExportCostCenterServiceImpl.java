package co.unicauca.edu.co.contables.configuration.costCenters.domain.services;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters.CostCenterExportException;
import co.unicauca.edu.co.contables.configuration.commons.exceptions.costCenters.CostCenterExportNoDataException;
import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.repository.CostCenterRepository;
import co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.entity.CostCenterEntity;
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

/**
 * @brief Implementación del servicio de exportación de centros de costo
 *
 * Implementación del servicio de exportación de centros de costo a Excel,
 * generando archivos XLSX con formato y estilos apropiados.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportCostCenterServiceImpl implements IExportCostCenterService {

    private final CostCenterRepository costCenterRepository;

    @Override
    public Resource exportCostCenters(String enterpriseId, Boolean status) {

        // Obtener centros de costo según el filtro de estado
        List<CostCenterEntity> costCenters;
        if (status != null) {
            costCenters = costCenterRepository.findAllByIdEnterpriseAndStatus(enterpriseId, status);
        } else {
            costCenters = costCenterRepository.findAllByIdEnterprise(enterpriseId);
        }

        if (costCenters.isEmpty()) {
            throw new CostCenterExportNoDataException(status);
        }

        return generateExcelFile(costCenters, enterpriseId, status);
    }

    /**
     * Genera el archivo Excel con los centros de costo.
     */
    private Resource generateExcelFile(List<CostCenterEntity> costCenters, String enterpriseId, Boolean status) {
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Centros de Costo");

            // Crear estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // Crear encabezados
            createHeaders(sheet, headerStyle);

            // Llenar datos
            fillData(sheet, costCenters, dataStyle);

            // Ajustar ancho de columnas
            autoSizeColumns(sheet);

            workbook.write(outputStream);

            return new ByteArrayResource(outputStream.toByteArray());

        } catch (IOException e) {
            throw new CostCenterExportException("Error al generar archivo de exportación de centros de costo", e);
        }
    }

    /**
     * Crea el estilo para los encabezados.
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
        return style;
    }

    /**
     * Crea el estilo para las celdas de datos.
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * Crea los encabezados de las columnas.
     */
    private void createHeaders(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);

        createHeaderCell(headerRow, 0, "Código", headerStyle);
        createHeaderCell(headerRow, 1, "Nombre", headerStyle);
        createHeaderCell(headerRow, 2, "Estado", headerStyle);
    }

    /**
     * Crea una celda de encabezado.
     */
    private void createHeaderCell(Row row, int colIndex, String value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    /**
     * Llena los datos en el Excel.
     */
    private void fillData(Sheet sheet, List<CostCenterEntity> costCenters, CellStyle dataStyle) {
        int rowIndex = 1;

        for (CostCenterEntity costCenter : costCenters) {
            Row row = sheet.createRow(rowIndex++);

            createDataCell(row, 0, costCenter.getCode(), dataStyle);
            createDataCell(row, 1, costCenter.getName(), dataStyle);
            createDataCell(row, 2, costCenter.getStatus() ? "ACTIVO" : "INACTIVO", dataStyle);
        }
    }

    /**
     * Crea una celda de datos.
     */
    private void createDataCell(Row row, int colIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(colIndex);

        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }

        cell.setCellStyle(style);
    }

    /**
     * Ajusta automáticamente el ancho de las columnas.
     */
    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < 3; i++) { // 3 columnas: Código, Nombre, Estado
            sheet.autoSizeColumn(i);

            // Obtener el ancho calculado
            int autoWidth = sheet.getColumnWidth(i);

            // Establecer límites razonables
            int minWidth = 2000;
            int maxWidth = 15000;

            // Aplicar los límites con padding
            int finalWidth = Math.min(Math.max(autoWidth + 500, minWidth), maxWidth);
            sheet.setColumnWidth(i, finalWidth);
        }
    }
}
