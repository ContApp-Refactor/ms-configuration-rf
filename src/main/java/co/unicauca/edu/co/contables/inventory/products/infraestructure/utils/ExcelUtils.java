package co.unicauca.edu.co.contables.inventory.products.infraestructure.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import co.unicauca.edu.co.contables.inventory.products.domain.utils.ImportConstants;

import java.io.IOException;
import java.util.*;

/**
 * @brief Utilidades completas para manipulación de archivos Excel
 *
 * Centraliza funcionalidades de validación, lectura, escritura y manipulación
 * de archivos Excel usando Apache POI para reutilización en diferentes módulos.
 */
public final class ExcelUtils {

    private ExcelUtils() {
        throw new UnsupportedOperationException("ExcelUtils es una clase de utilidad y no debe ser instanciada");
    }

  
    /**
     * @brief Valida extensión de archivo Excel (.xlsx, .xls)
     * @param fileName nombre del archivo a validar
     * @return true si tiene extensión Excel válida
     */
    public static boolean isValidExcelExtension(String fileName) {
        if (fileName == null) {
            return false;
        }
        
        String lowerFileName = fileName.toLowerCase();
        return Arrays.stream(ImportConstants.SUPPORTED_EXTENSIONS)
                .anyMatch(lowerFileName::endsWith);
    }


    /**
     * @brief Abre workbook Excel desde MultipartFile
     * @param file archivo Excel subido
     * @return workbook XSSFWorkbook abierto
     * @throws IOException si hay error al leer el archivo
     */
    public static Workbook openWorkbook(MultipartFile file) throws IOException {
        return new XSSFWorkbook(file.getInputStream());
    }

    /**
     * @brief Obtiene primera hoja del workbook Excel
     * @param workbook workbook Excel
     * @return primera hoja del workbook
     * @throws IllegalArgumentException si workbook no tiene hojas
     */
    public static Sheet getFirstSheet(Workbook workbook) {
        if (workbook.getNumberOfSheets() == 0) {
            throw new IllegalArgumentException("El archivo Excel no contiene hojas");
        }
        return workbook.getSheetAt(0);
    }

    /**
     * @brief Detecta mapeo de columnas desde fila de encabezados
     * @param headerRow fila que contiene los encabezados
     * @return mapa que asocia nombre de columna con índice numérico
     */
    public static Map<String, Integer> detectColumnMapping(Row headerRow) {
        Map<String, Integer> columnMap = new HashMap<>();
        
        if (headerRow == null) {
            return columnMap;
        }

        for (Cell cell : headerRow) {
            if (cell != null) {
                String headerValue = getCellValueAsString(cell);
                if (headerValue != null && !headerValue.trim().isEmpty()) {
                    columnMap.put(headerValue.trim(), cell.getColumnIndex());
                }
            }
        }

        return columnMap;
    }

    /**
     * @brief Valida presencia de encabezados requeridos
     * @param columnMap mapa de columnas detectadas
     * @param requiredHeaders array de encabezados obligatorios
     * @return lista de encabezados faltantes (vacía si todos presentes)
     */
    public static List<String> validateRequiredHeaders(Map<String, Integer> columnMap, String[] requiredHeaders) {
        List<String> missingHeaders = new ArrayList<>();
        
        for (String requiredHeader : requiredHeaders) {
            if (!columnMap.containsKey(requiredHeader)) {
                missingHeaders.add(requiredHeader);
            }
        }
        
        return missingHeaders;
    }


    /**
     * @brief Obtiene valor de celda como String con manejo de tipos
     * @param cell celda Excel a leer
     * @return valor como String o null si celda vacía
     */
    public static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    // Manejar números enteros sin decimales
                    double numValue = cell.getNumericCellValue();
                    if (numValue == Math.floor(numValue)) {
                        yield String.valueOf((long) numValue);
                    } else {
                        yield String.valueOf(numValue);
                    }
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue().trim();
                } catch (IllegalStateException e) {
                    // Si la fórmula no es un string, intentar como número
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> null;
        };
    }

    /**
     * @brief Obtiene valor de celda como Long
     * @param cell celda Excel a leer
     * @return valor como Long o null si no se puede convertir
     */
    public static Long getCellValueAsLong(Cell cell) {
        String stringValue = getCellValueAsString(cell);
        if (stringValue == null || stringValue.isEmpty()) {
            return null;
        }

        try {
            return Long.valueOf(stringValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * @brief Obtiene valor de celda por nombre de columna
     * @param row fila que contiene la celda
     * @param columnName nombre de la columna
     * @param columnMap mapa de mapeo columna-nombre
     * @return valor como String o null si no se encuentra
     */
    public static String getCellValueByColumnName(Row row, String columnName, Map<String, Integer> columnMap) {
        Integer columnIndex = columnMap.get(columnName);
        if (columnIndex == null) {
            return null;
        }
        
        Cell cell = row.getCell(columnIndex);
        return getCellValueAsString(cell);
    }

    /**
     * @brief Obtiene valor de celda como Long por nombre de columna
     * @param row fila que contiene la celda
     * @param columnName nombre de la columna
     * @param columnMap mapa de mapeo columna-nombre
     * @return valor como Long o null si no se encuentra o no se puede convertir
     */
    public static Long getCellValueAsLongByColumnName(Row row, String columnName, Map<String, Integer> columnMap) {
        Integer columnIndex = columnMap.get(columnName);
        if (columnIndex == null) {
            return null;
        }
        
        Cell cell = row.getCell(columnIndex);
        return getCellValueAsLong(cell);
    }


    /**
     * @brief Verifica si fila Excel está completamente vacía
     * @param row fila Excel a verificar
     * @return true si fila está vacía, false si tiene datos
     */
    public static boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (Cell cell : row) {
            String cellValue = getCellValueAsString(cell);
            if (cellValue != null && !cellValue.trim().isEmpty()) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * @brief Cuenta filas con datos en hoja Excel
     * @param sheet hoja Excel a procesar
     * @param startRow índice de primera fila de datos (excluyendo encabezados)
     * @return número de filas que contienen datos
     */
    public static int countDataRows(Sheet sheet, int startRow) {
        int dataRowCount = 0;
        int lastRowNum = sheet.getLastRowNum();
        
        for (int i = startRow; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            if (!isRowEmpty(row)) {
                dataRowCount++;
            }
        }
        
        return dataRowCount;
    }

    /**
     * @brief Cierra workbook Excel de manera segura
     * @param workbook workbook Excel a cerrar
     */
    public static void closeWorkbookSafely(Workbook workbook) {
        if (workbook != null) {
            try {
                workbook.close();
            } catch (IOException e) {
                // Log warning pero no lanzar excepción
                System.err.println("Warning: No se pudo cerrar el workbook correctamente: " + e.getMessage());
            }
        }
    }
}
