package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.utils;

import org.apache.poi.ss.usermodel.*;

/**
 * @brief Clase auxiliar para crear estilos de celdas en Excel
 *
 * Centraliza la creación de estilos reutilizables para archivos Excel,
 * incluyendo estilos para encabezados, datos y fechas.
 */
public class ExcelStyleHelper {

    private final Workbook workbook;
    private final CellStyle headerStyle;
    private final CellStyle optionalHeaderStyle;
    private final CellStyle dataStyle;
    private final CellStyle dateStyle;

    public ExcelStyleHelper(Workbook workbook) {
        this.workbook = workbook;
        this.headerStyle = createHeaderStyle();
        this.optionalHeaderStyle = createOptionalHeaderStyle();
        this.dataStyle = createDataStyle();
        this.dateStyle = createDateStyle();
    }

    public CellStyle getHeaderStyle() {
        return headerStyle;
    }

    public CellStyle getOptionalHeaderStyle() {
        return optionalHeaderStyle;
    }

    public CellStyle getDataStyle() {
        return dataStyle;
    }

    public CellStyle getDateStyle() {
        return dateStyle;
    }

    private CellStyle createHeaderStyle() {
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

    private CellStyle createOptionalHeaderStyle() {
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

    private CellStyle createDataStyle() {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDateStyle() {
        CellStyle style = createDataStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
        return style;
    }
}

