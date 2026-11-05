package co.unicauca.edu.co.contables.inventory.products.infraestructure.utils;

import lombok.experimental.UtilityClass;
import org.apache.poi.ss.usermodel.*;

/**
 * @brief Utilidad para crear estilos de celdas Excel reutilizables
 *
 * Centraliza la creación de estilos de Excel para mantener consistencia
 * y evitar duplicación de código en diferentes servicios de exportación.
 */
@UtilityClass
public class ExcelStyleHelper {

    /**
     * @brief Crea estilo para encabezados principales de Excel
     * @param workbook Libro de Excel donde crear el estilo
     * @return estilo configurado para encabezados principales
     */
    public static CellStyle createHeaderStyle(Workbook workbook) {
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
     * @brief Crea estilo para encabezados opcionales de Excel
     * @param workbook Libro de Excel donde crear el estilo
     * @return estilo configurado para encabezados opcionales
     */
    public static CellStyle createOptionalHeaderStyle(Workbook workbook) {
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
     * @brief Crea estilo para celdas de datos de Excel
     * @param workbook Libro de Excel donde crear el estilo
     * @return estilo configurado para celdas de datos
     */
    public static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}
