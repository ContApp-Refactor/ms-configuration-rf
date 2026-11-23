package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.utils;

import co.unicauca.edu.co.contables.configuration.thirds.domain.utils.ImportConstants;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * @brief Utilidades estáticas para manipulación de archivos Excel con Apache POI
 *
 * Clase de utilidad que centraliza operaciones comunes de Excel como validación,
 * lectura, escritura y manipulación de celdas. Utiliza Apache POI para trabajar
 * con archivos .xlsx y proporciona métodos thread-safe para uso concurrente.
 */
public final class ExcelUtils {

    private ExcelUtils() {
        throw new UnsupportedOperationException("ExcelUtils es una clase de utilidad y no debe ser instanciada");
    }

    // ===== VALIDACIONES DE ARCHIVO =====
  
    /**
     * @brief Valida extensión de archivo Excel contra lista de extensiones soportadas
     * @details Verifica si el nombre del archivo termina con alguna de las extensiones
     * definidas en ImportConstants.SUPPORTED_EXTENSIONS (.xlsx, .xls).
     * @param fileName nombre del archivo a validar (puede ser null)
     * @return true si la extensión es válida, false en caso contrario
     */
    public static boolean isValidExcelExtension(String fileName) {
        if (fileName == null) {
            return false;
        }
        
        String lowerFileName = fileName.toLowerCase();
        return Arrays.stream(ImportConstants.SUPPORTED_EXTENSIONS)
                .anyMatch(lowerFileName::endsWith);
    }

    // ===== OPERACIONES DE LECTURA =====

    /**
     * @brief Abre archivo Excel como Workbook de Apache POI desde MultipartFile
     * @details Lee el contenido del archivo multipart y crea un Workbook XSSFWorkbook
     * para archivos .xlsx. El caller es responsable de cerrar el workbook.
     * @param file archivo Excel como MultipartFile de Spring
     * @return Workbook de Apache POI listo para manipulación
     * @throws IOException si hay error al leer el archivo o formato inválido
     */
    public static Workbook openWorkbook(MultipartFile file) throws IOException {
        return new XSSFWorkbook(file.getInputStream());
    }

    /**
     * @brief Obtiene la primera hoja de un workbook Excel
     * @details Accede a workbook.getSheetAt(0) con validación previa para asegurar
     * que el archivo Excel contiene al menos una hoja.
     * @param workbook workbook Excel ya abierto
     * @return primera hoja del workbook
     * @throws IllegalArgumentException si el workbook está vacío
     */
    public static Sheet getFirstSheet(Workbook workbook) {
        if (workbook.getNumberOfSheets() == 0) {
            throw new IllegalArgumentException("El archivo Excel no contiene hojas");
        }
        return workbook.getSheetAt(0);
    }

    /**
     * @brief Crea mapeo dinámico de nombres de columna a índices desde fila de encabezados
     * @details Itera por todas las celdas de la fila de encabezados, extrae valores de texto
     * y crea un mapa que asocia cada nombre de columna con su índice numérico.
     * @param headerRow fila que contiene los encabezados de columna
     * @return mapa donde key=nombre_columna, value=índice_columna (0-based)
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
     * @brief Valida presencia de encabezados requeridos en archivo Excel
     * @details Verifica que todos los encabezados especificados en requiredHeaders
     * estén presentes en el columnMap generado desde la fila de encabezados.
     * @param columnMap mapa columna->índice generado por detectColumnMapping
     * @param requiredHeaders array de nombres de encabezados obligatorios
     * @return lista de encabezados faltantes (vacía si todos están presentes)
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

    // ===== OPERACIONES DE CELDA =====

    /**
     * @brief Extrae valor de celda Excel como String con manejo de tipos
     * @details Convierte el contenido de la celda a String independientemente del tipo original
     * (STRING, NUMERIC, BOOLEAN, FORMULA). Maneja números enteros sin decimales, fechas formateadas
     * y fórmulas que retornan strings o números.
     * @param cell celda Excel a convertir (puede ser null)
     * @return valor como String o null si celda es null o vacía
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
     * @brief Convierte valor de celda Excel a Long con validación segura
     * @details Intenta convertir el contenido de la celda a Long. Primero obtiene el valor como String
     * y luego hace el parsing numérico. Retorna null si la celda está vacía o el contenido no es numérico.
     * @param cell celda Excel a convertir
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
     * @brief Obtiene valor de celda por nombre de columna usando mapeo dinámico
     * @details Busca el índice de columna en el columnMap y luego obtiene el valor de la celda
     * correspondiente en la fila especificada. Retorna null si la columna no existe en el mapeo.
     * @param row fila Excel que contiene los datos
     * @param columnName nombre de la columna según encabezados
     * @param columnMap mapeo nombre-columna -> índice generado por detectColumnMapping
     * @return valor de la celda como String o null si no se encuentra
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
     * @brief Obtiene el valor de una celda como Long por nombre de columna.
     * 
     * @param row la fila que contiene la celda
     * @param columnName el nombre de la columna
     * @param columnMap el mapa de columnas
     * @return el valor como Long o null si no se encuentra o no se puede convertir
     */
    public static Long getCellValueAsLongByColumnName(Row row, String columnName, Map<String, Integer> columnMap) {
        Integer columnIndex = columnMap.get(columnName);
        if (columnIndex == null) {
            return null;
        }
        
        Cell cell = row.getCell(columnIndex);
        return getCellValueAsLong(cell);
    }

    // ===== OPERACIONES DE FILA =====

    /**
     * @brief Verifica si una fila Excel está completamente vacía
     * @details Itera por todas las celdas de la fila verificando si contienen valores no vacíos.
     * Una fila se considera vacía si todas sus celdas están null, vacías o contienen solo espacios.
     * @param row fila Excel a verificar (puede ser null)
     * @return true si la fila está vacía o es null, false si contiene datos
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
     * @brief Cuenta filas con datos reales en hoja Excel excluyendo encabezados
     * @details Itera desde startRow hasta la última fila de la hoja, contando solo filas
     * que no están vacías según isRowEmpty. Útil para determinar cantidad de registros
     * en archivos de importación.
     * @param sheet hoja Excel a analizar
     * @param startRow índice de la primera fila de datos (normalmente 1, después de encabezados)
     * @return cantidad de filas que contienen datos reales
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

    // ===== UTILIDADES DE GENERACIÓN =====


    // ===== MANEJO DE ERRORES =====

    /**
     * @brief Cierra workbook Excel de forma segura sin lanzar excepciones
     * @details Intenta cerrar el workbook y maneja silenciosamente cualquier IOException
     * que pueda ocurrir. Evita que fallos de cierre interrumpan el flujo principal de la aplicación.
     * Registra warning en System.err si falla el cierre.
     * @param workbook instancia de Workbook a cerrar (puede ser null, en cuyo caso no hace nada)
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
