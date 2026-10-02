package co.unicauca.edu.co.contables.inventory.products.unit.infraestructure.utils;

import co.unicauca.edu.co.contables.inventory.products.infraestructure.utils.ExcelUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExcelUtilsUnitTest {

    private Workbook workbook;
    private Sheet sheet;

    @BeforeEach
    void setUp() {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Test Sheet");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (workbook != null) {
            workbook.close();
        }
    }

    // ==================== Tests de constructor ====================

    @Test
    @DisplayName("No debe permitir instanciar la clase de utilidad")
    void testConstructor_ThrowsException() {
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            var constructor = ExcelUtils.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
        
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
    }

    // ==================== Tests de isValidExcelExtension ====================

    @Test
    @DisplayName("Debe validar extensión .xlsx correctamente")
    void testIsValidExcelExtension_WithXlsx_ReturnsTrue() {
        // Act
        boolean result = ExcelUtils.isValidExcelExtension("archivo.xlsx");

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Debe validar extensión .xls correctamente")
    void testIsValidExcelExtension_WithXls_ReturnsTrue() {
        // Act
        boolean result = ExcelUtils.isValidExcelExtension("archivo.xls");

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Debe validar extensión .XLSX en mayúsculas")
    void testIsValidExcelExtension_WithUppercaseXlsx_ReturnsTrue() {
        // Act
        boolean result = ExcelUtils.isValidExcelExtension("archivo.XLSX");

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Debe rechazar extensión inválida")
    void testIsValidExcelExtension_WithInvalidExtension_ReturnsFalse() {
        // Act
        boolean result = ExcelUtils.isValidExcelExtension("archivo.pdf");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Debe rechazar nombre de archivo null")
    void testIsValidExcelExtension_WithNullFileName_ReturnsFalse() {
        // Act
        boolean result = ExcelUtils.isValidExcelExtension(null);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Debe validar archivo con nombre complejo")
    void testIsValidExcelExtension_WithComplexFileName_ReturnsTrue() {
        // Act
        boolean result = ExcelUtils.isValidExcelExtension("reporte_productos_2024.xlsx");

        // Assert
        assertTrue(result);
    }

    // ==================== Tests de openWorkbook ====================

    @Test
    @DisplayName("Debe abrir workbook desde MultipartFile correctamente")
    void testOpenWorkbook_WithValidFile_ReturnsWorkbook() throws IOException {
        // Arrange
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Workbook testWorkbook = new XSSFWorkbook();
        testWorkbook.createSheet("Test");
        testWorkbook.write(bos);
        testWorkbook.close();

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray()
        );

        // Act
        Workbook result = ExcelUtils.openWorkbook(file);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getNumberOfSheets());
        result.close();
    }

    @Test
    @DisplayName("Debe lanzar excepción con archivo inválido")
    void testOpenWorkbook_WithInvalidFile_ThrowsException() {
        // Arrange
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "contenido inválido".getBytes()
        );

        // Act & Assert
        assertThrows(Exception.class, () -> ExcelUtils.openWorkbook(file));
    }

    // ==================== Tests de getFirstSheet ====================

    @Test
    @DisplayName("Debe obtener primera hoja del workbook")
    void testGetFirstSheet_WithValidWorkbook_ReturnsFirstSheet() {
        // Arrange
        Workbook testWorkbook = new XSSFWorkbook();
        testWorkbook.createSheet("Primera");
        testWorkbook.createSheet("Segunda");

        // Act
        Sheet result = ExcelUtils.getFirstSheet(testWorkbook);

        // Assert
        assertNotNull(result);
        assertEquals("Primera", result.getSheetName());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando workbook no tiene hojas")
    void testGetFirstSheet_WithEmptyWorkbook_ThrowsException() {
        // Arrange
        Workbook emptyWorkbook = new XSSFWorkbook();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ExcelUtils.getFirstSheet(emptyWorkbook));
    }

    // ==================== Tests de detectColumnMapping ====================

    @Test
    @DisplayName("Debe detectar mapeo de columnas correctamente")
    void testDetectColumnMapping_WithValidHeaders_ReturnsMap() {
        // Arrange
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Nombre");
        headerRow.createCell(1).setCellValue("Precio");
        headerRow.createCell(2).setCellValue("Cantidad");

        // Act
        Map<String, Integer> result = ExcelUtils.detectColumnMapping(headerRow);

        // Assert
        assertEquals(3, result.size());
        assertEquals(0, result.get("Nombre"));
        assertEquals(1, result.get("Precio"));
        assertEquals(2, result.get("Cantidad"));
    }

    @Test
    @DisplayName("Debe ignorar celdas vacías en encabezados")
    void testDetectColumnMapping_WithEmptyCells_IgnoresEmptyCells() {
        // Arrange
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Nombre");
        headerRow.createCell(1).setCellValue("");
        headerRow.createCell(2).setCellValue("Precio");

        // Act
        Map<String, Integer> result = ExcelUtils.detectColumnMapping(headerRow);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.containsKey("Nombre"));
        assertTrue(result.containsKey("Precio"));
        assertFalse(result.containsKey(""));
    }

    @Test
    @DisplayName("Debe retornar mapa vacío con fila null")
    void testDetectColumnMapping_WithNullRow_ReturnsEmptyMap() {
        // Act
        Map<String, Integer> result = ExcelUtils.detectColumnMapping(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe eliminar espacios en blanco de encabezados")
    void testDetectColumnMapping_TrimsWhitespace_FromHeaders() {
        // Arrange
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("  Nombre  ");
        headerRow.createCell(1).setCellValue(" Precio ");

        // Act
        Map<String, Integer> result = ExcelUtils.detectColumnMapping(headerRow);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.containsKey("Nombre"));
        assertTrue(result.containsKey("Precio"));
    }

    // ==================== Tests de validateRequiredHeaders ====================

    @Test
    @DisplayName("Debe validar que todos los encabezados requeridos estén presentes")
    void testValidateRequiredHeaders_WithAllPresent_ReturnsEmptyList() {
        // Arrange
        Map<String, Integer> columnMap = Map.of(
                "Nombre", 0,
                "Precio", 1,
                "Cantidad", 2
        );
        String[] requiredHeaders = {"Nombre", "Precio", "Cantidad"};

        // Act
        List<String> result = ExcelUtils.validateRequiredHeaders(columnMap, requiredHeaders);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe retornar encabezados faltantes")
    void testValidateRequiredHeaders_WithMissing_ReturnsMissingHeaders() {
        // Arrange
        Map<String, Integer> columnMap = Map.of(
                "Nombre", 0,
                "Precio", 1
        );
        String[] requiredHeaders = {"Nombre", "Precio", "Cantidad", "Descripción"};

        // Act
        List<String> result = ExcelUtils.validateRequiredHeaders(columnMap, requiredHeaders);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains("Cantidad"));
        assertTrue(result.contains("Descripción"));
    }

    @Test
    @DisplayName("Debe retornar todos los encabezados cuando ninguno está presente")
    void testValidateRequiredHeaders_WithNonePresent_ReturnsAllHeaders() {
        // Arrange
        Map<String, Integer> columnMap = Map.of();
        String[] requiredHeaders = {"Nombre", "Precio", "Cantidad"};

        // Act
        List<String> result = ExcelUtils.validateRequiredHeaders(columnMap, requiredHeaders);

        // Assert
        assertEquals(3, result.size());
    }

    // ==================== Tests de getCellValueAsString ====================

    @Test
    @DisplayName("Debe obtener valor de celda tipo String")
    void testGetCellValueAsString_WithStringCell_ReturnsString() {
        // Arrange
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("Texto");

        // Act
        String result = ExcelUtils.getCellValueAsString(cell);

        // Assert
        assertEquals("Texto", result);
    }

    @Test
    @DisplayName("Debe obtener valor de celda tipo numérico entero")
    void testGetCellValueAsString_WithIntegerCell_ReturnsString() {
        // Arrange
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue(100.0);

        // Act
        String result = ExcelUtils.getCellValueAsString(cell);

        // Assert
        assertEquals("100", result);
    }

    @Test
    @DisplayName("Debe obtener valor de celda tipo numérico con decimales")
    void testGetCellValueAsString_WithDecimalCell_ReturnsString() {
        // Arrange
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue(99.99);

        // Act
        String result = ExcelUtils.getCellValueAsString(cell);

        // Assert
        assertEquals("99.99", result);
    }

    @Test
    @DisplayName("Debe obtener valor de celda tipo booleano")
    void testGetCellValueAsString_WithBooleanCell_ReturnsString() {
        // Arrange
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue(true);

        // Act
        String result = ExcelUtils.getCellValueAsString(cell);

        // Assert
        assertEquals("true", result);
    }

    @Test
    @DisplayName("Debe retornar null para celda null")
    void testGetCellValueAsString_WithNullCell_ReturnsNull() {
        // Act
        String result = ExcelUtils.getCellValueAsString(null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Debe retornar null para celda vacía")
    void testGetCellValueAsString_WithBlankCell_ReturnsNull() {
        // Arrange
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setBlank();

        // Act
        String result = ExcelUtils.getCellValueAsString(cell);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Debe eliminar espacios en blanco de valores String")
    void testGetCellValueAsString_TrimsWhitespace_FromStrings() {
        // Arrange
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("  Texto con espacios  ");

        // Act
        String result = ExcelUtils.getCellValueAsString(cell);

        // Assert
        assertEquals("Texto con espacios", result);
    }

    // ==================== Tests de getCellValueAsLong ====================

    @Test
    @DisplayName("Debe convertir valor numérico a Long")
    void testGetCellValueAsLong_WithNumericCell_ReturnsLong() {
        // Arrange
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue(12345.0);

        // Act
        Long result = ExcelUtils.getCellValueAsLong(cell);

        // Assert
        assertEquals(12345L, result);
    }

    @Test
    @DisplayName("Debe retornar null para celda vacía")
    void testGetCellValueAsLong_WithEmptyCell_ReturnsNull() {
        // Arrange
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("");

        // Act
        Long result = ExcelUtils.getCellValueAsLong(cell);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Debe retornar null para valor no numérico")
    void testGetCellValueAsLong_WithNonNumericCell_ReturnsNull() {
        // Arrange
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("texto");

        // Act
        Long result = ExcelUtils.getCellValueAsLong(cell);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Debe retornar null para celda null")
    void testGetCellValueAsLong_WithNullCell_ReturnsNull() {
        // Act
        Long result = ExcelUtils.getCellValueAsLong(null);

        // Assert
        assertNull(result);
    }

    // ==================== Tests de getCellValueByColumnName ====================

    @Test
    @DisplayName("Debe obtener valor de celda por nombre de columna")
    void testGetCellValueByColumnName_WithValidColumn_ReturnsValue() {
        // Arrange
        Map<String, Integer> columnMap = Map.of("Nombre", 0, "Precio", 1);
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("Producto");
        row.createCell(1).setCellValue("100.5");

        // Act
        String result = ExcelUtils.getCellValueByColumnName(row, "Nombre", columnMap);

        // Assert
        assertEquals("Producto", result);
    }

    @Test
    @DisplayName("Debe retornar null cuando columna no existe en mapa")
    void testGetCellValueByColumnName_WithInvalidColumn_ReturnsNull() {
        // Arrange
        Map<String, Integer> columnMap = Map.of("Nombre", 0);
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("Producto");

        // Act
        String result = ExcelUtils.getCellValueByColumnName(row, "Precio", columnMap);

        // Assert
        assertNull(result);
    }

    // ==================== Tests de getCellValueAsLongByColumnName ====================

    @Test
    @DisplayName("Debe obtener valor Long por nombre de columna")
    void testGetCellValueAsLongByColumnName_WithValidColumn_ReturnsLong() {
        // Arrange
        Map<String, Integer> columnMap = Map.of("ID", 0, "Cantidad", 1);
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(12345.0);
        row.createCell(1).setCellValue(100.0);

        // Act
        Long result = ExcelUtils.getCellValueAsLongByColumnName(row, "ID", columnMap);

        // Assert
        assertEquals(12345L, result);
    }

    @Test
    @DisplayName("Debe retornar null cuando columna no existe")
    void testGetCellValueAsLongByColumnName_WithInvalidColumn_ReturnsNull() {
        // Arrange
        Map<String, Integer> columnMap = Map.of("ID", 0);
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(12345.0);

        // Act
        Long result = ExcelUtils.getCellValueAsLongByColumnName(row, "Precio", columnMap);

        // Assert
        assertNull(result);
    }

    // ==================== Tests de isRowEmpty ====================

    @Test
    @DisplayName("Debe detectar fila vacía")
    void testIsRowEmpty_WithEmptyRow_ReturnsTrue() {
        // Arrange
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("");
        row.createCell(1).setCellValue("");

        // Act
        boolean result = ExcelUtils.isRowEmpty(row);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Debe detectar fila con datos")
    void testIsRowEmpty_WithDataRow_ReturnsFalse() {
        // Arrange
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("Dato");
        row.createCell(1).setCellValue("");

        // Act
        boolean result = ExcelUtils.isRowEmpty(row);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Debe retornar true para fila null")
    void testIsRowEmpty_WithNullRow_ReturnsTrue() {
        // Act
        boolean result = ExcelUtils.isRowEmpty(null);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Debe considerar espacios en blanco como fila vacía")
    void testIsRowEmpty_WithWhitespaceOnly_ReturnsTrue() {
        // Arrange
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("   ");
        row.createCell(1).setCellValue("  ");

        // Act
        boolean result = ExcelUtils.isRowEmpty(row);

        // Assert
        assertTrue(result);
    }

    // ==================== Tests de countDataRows ====================

    @Test
    @DisplayName("Debe contar filas con datos correctamente")
    void testCountDataRows_WithMultipleRows_ReturnsCorrectCount() {
        // Arrange
        sheet.createRow(0).createCell(0).setCellValue("Header");
        sheet.createRow(1).createCell(0).setCellValue("Dato 1");
        sheet.createRow(2).createCell(0).setCellValue("");
        sheet.createRow(3).createCell(0).setCellValue("Dato 2");

        // Act
        int result = ExcelUtils.countDataRows(sheet, 1);

        // Assert
        assertEquals(2, result);
    }

    @Test
    @DisplayName("Debe retornar cero cuando no hay filas con datos")
    void testCountDataRows_WithNoData_ReturnsZero() {
        // Arrange
        sheet.createRow(0).createCell(0).setCellValue("Header");
        sheet.createRow(1).createCell(0).setCellValue("");
        sheet.createRow(2).createCell(0).setCellValue("");

        // Act
        int result = ExcelUtils.countDataRows(sheet, 1);

        // Assert
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Debe contar todas las filas después del encabezado")
    void testCountDataRows_SkipsHeaderRow_CountsDataRows() {
        // Arrange
        sheet.createRow(0).createCell(0).setCellValue("Header");
        sheet.createRow(1).createCell(0).setCellValue("Dato 1");
        sheet.createRow(2).createCell(0).setCellValue("Dato 2");
        sheet.createRow(3).createCell(0).setCellValue("Dato 3");

        // Act
        int result = ExcelUtils.countDataRows(sheet, 1);

        // Assert
        assertEquals(3, result);
    }

    // ==================== Tests de closeWorkbookSafely ====================

    @Test
    @DisplayName("Debe cerrar workbook correctamente")
    void testCloseWorkbookSafely_WithValidWorkbook_ClosesSuccessfully() {
        // Arrange
        Workbook testWorkbook = new XSSFWorkbook();
        testWorkbook.createSheet("Test");

        // Act
        ExcelUtils.closeWorkbookSafely(testWorkbook);

        // Assert - No debe lanzar excepción
        assertTrue(true);
    }

    @Test
    @DisplayName("No debe lanzar excepción con workbook null")
    void testCloseWorkbookSafely_WithNullWorkbook_DoesNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> ExcelUtils.closeWorkbookSafely(null));
    }

    @Test
    @DisplayName("No debe propagar excepción al cerrar workbook")
    void testCloseWorkbookSafely_WithIOException_DoesNotPropagateException() throws IOException {
        // Arrange
        Workbook testWorkbook = new XSSFWorkbook();
        testWorkbook.close(); // Cerrar primero para forzar IOException en segundo close

        // Act & Assert
        assertDoesNotThrow(() -> ExcelUtils.closeWorkbookSafely(testWorkbook));
    }
}
