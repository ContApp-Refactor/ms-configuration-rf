package co.unicauca.edu.co.contables.inventory.products.unit.application.service.importExport;

import co.unicauca.edu.co.contables.inventory.products.application.service.importExport.ProductExcelParsingService;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductExcelParsingServiceUnitTest {

    @InjectMocks
    private ProductExcelParsingService productExcelParsingService;

    private String testEntId;

    @BeforeEach
    void setUp() {
        testEntId = "ENT123";
    }

    @Test
    @DisplayName("Debe parsear archivo Excel con datos válidos")
    void testParseExcelFileSuccess() throws Exception {
        MockMultipartFile file = createValidExcelFile();

        ProductExcelParsingService.ExcelParsingResult result = 
            productExcelParsingService.parseExcelFile(file, testEntId);

        assertNotNull(result);
        assertEquals(1, result.getProductsData().size());
        assertEquals(1, result.getTotalRows());
        assertFalse(result.getColumnMap().isEmpty());
    }

    @Test
    @DisplayName("Debe parsear archivo Excel desde bytes")
    void testParseExcelFileFromBytesSuccess() throws Exception {
        byte[] fileBytes = createValidExcelFileBytes();

        ProductExcelParsingService.ExcelParsingResult result = 
            productExcelParsingService.parseExcelFileFromBytes(fileBytes, testEntId);

        assertNotNull(result);
        assertEquals(1, result.getProductsData().size());
        assertEquals(1, result.getTotalRows());
    }

    @Test
    @DisplayName("Debe detectar archivo vacío")
    void testParseExcelFileEmpty() throws Exception {
        MockMultipartFile file = createEmptyExcelFile();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            productExcelParsingService.parseExcelFile(file, testEntId)
        );

        assertTrue(exception.getMessage().contains("vacío") || exception.getMessage().contains("datos"));
    }

    @Test
    @DisplayName("Debe detectar encabezados faltantes")
    void testParseExcelFileMissingHeaders() throws Exception {
        MockMultipartFile file = createExcelFileWithInvalidHeaders();

        ProductExcelParsingService.ExcelParsingResult result = 
            productExcelParsingService.parseExcelFile(file, testEntId);

        assertNotNull(result);
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Debe ignorar filas vacías")
    void testParseExcelFileSkipEmptyRows() throws Exception {
        MockMultipartFile file = createExcelFileWithEmptyRows();

        ProductExcelParsingService.ExcelParsingResult result = 
            productExcelParsingService.parseExcelFile(file, testEntId);

        assertNotNull(result);
        assertEquals(1, result.getProductsData().size());
    }

    @Test
    @DisplayName("Debe manejar valores numéricos inválidos en cantidad")
    void testParseExcelFileInvalidQuantity() throws Exception {
        MockMultipartFile file = createExcelFileWithInvalidQuantity();

        ProductExcelParsingService.ExcelParsingResult result = 
            productExcelParsingService.parseExcelFile(file, testEntId);

        assertNotNull(result);
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Debe manejar valores numéricos inválidos en costo")
    void testParseExcelFileInvalidCost() throws Exception {
        MockMultipartFile file = createExcelFileWithInvalidCost();

        ProductExcelParsingService.ExcelParsingResult result = 
            productExcelParsingService.parseExcelFile(file, testEntId);

        assertNotNull(result);
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Debe procesar cantidad dentro del rango máximo de Long")
    void testParseExcelFileVeryLargeQuantity() throws Exception {
        MockMultipartFile file = createExcelFileWithExcessiveQuantity();

        ProductExcelParsingService.ExcelParsingResult result = 
            productExcelParsingService.parseExcelFile(file, testEntId);

        assertNotNull(result);
        assertEquals(1, result.getProductsData().size());
        assertNotNull(result.getProductsData().get(0).getQuantity());
    }

    @Test
    @DisplayName("Debe detectar cantidad que excede límite máximo configurado")
    void testParseExcelFileQuantityExceedsMaxLimit() throws Exception {
        MockMultipartFile file = createExcelFileWithQuantityOverLimit();

        ProductExcelParsingService.ExcelParsingResult result = 
            productExcelParsingService.parseExcelFile(file, testEntId);

        assertNotNull(result);
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().stream()
            .anyMatch(e -> e.getErrorMessage().contains("solo números") || 
                          e.getErrorMessage().contains("excede el valor máximo")));
    }

    @Test
    @DisplayName("Debe manejar cantidad negativa")
    void testParseExcelFileNegativeQuantity() throws Exception {
        MockMultipartFile file = createExcelFileWithNegativeQuantity();

        ProductExcelParsingService.ExcelParsingResult result = 
            productExcelParsingService.parseExcelFile(file, testEntId);

        assertNotNull(result);
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().stream()
            .anyMatch(e -> e.getErrorMessage().contains("valor positivo")));
    }

    @Test
    @DisplayName("Debe procesar costo muy grande sin errores")
    void testParseExcelFileVeryLargeCost() throws Exception {
        MockMultipartFile file = createExcelFileWithExcessiveCost();

        ProductExcelParsingService.ExcelParsingResult result = 
            productExcelParsingService.parseExcelFile(file, testEntId);

        assertNotNull(result);
        assertEquals(1, result.getProductsData().size());
        assertNotNull(result.getProductsData().get(0).getCost());
    }

    @Test
    @DisplayName("Debe manejar costo negativo")
    void testParseExcelFileNegativeCost() throws Exception {
        MockMultipartFile file = createExcelFileWithNegativeCost();

        ProductExcelParsingService.ExcelParsingResult result = 
            productExcelParsingService.parseExcelFile(file, testEntId);

        assertNotNull(result);
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().stream()
            .anyMatch(e -> e.getErrorMessage().contains("valor positivo")));
    }

    @Test
    @DisplayName("Debe manejar cantidad y costo vacíos")
    void testParseExcelFileEmptyNumericFields() throws Exception {
        MockMultipartFile file = createExcelFileWithEmptyNumericFields();

        ProductExcelParsingService.ExcelParsingResult result = 
            productExcelParsingService.parseExcelFile(file, testEntId);

        assertNotNull(result);
        assertEquals(1, result.getProductsData().size());
        assertNull(result.getProductsData().get(0).getQuantity());
        assertNull(result.getProductsData().get(0).getCost());
    }

    @Test
    @DisplayName("Debe manejar celda con tipo booleano")
    void testParseExcelFileBooleanCell() throws Exception {
        MockMultipartFile file = createExcelFileWithBooleanCell();

        ProductExcelParsingService.ExcelParsingResult result = 
            productExcelParsingService.parseExcelFile(file, testEntId);

        assertNotNull(result);
        assertEquals(1, result.getProductsData().size());
    }

    @Test
    @DisplayName("Debe detectar falta de headers requeridos")
    void testParseExcelFileNoHeaderRow() throws Exception {
        MockMultipartFile file = createExcelFileWithoutHeaders();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            productExcelParsingService.parseExcelFile(file, testEntId)
        );

        assertTrue(exception.getMessage().contains("encabezados") || 
                   exception.getMessage().contains("headers"));
    }

    @Test
    @DisplayName("Debe manejar celda con tipo numérico en campos de texto")
    void testParseExcelFileNumericCellInTextFields() throws Exception {
        MockMultipartFile file = createExcelFileWithNumericInTextFields();

        ProductExcelParsingService.ExcelParsingResult result = 
            productExcelParsingService.parseExcelFile(file, testEntId);

        assertNotNull(result);
        assertEquals(1, result.getProductsData().size());
        assertNotNull(result.getProductsData().get(0).getReference());
    }

    @Test
    @DisplayName("Debe manejar archivo sin fila de headers (headerRow null)")
    void testParseExcelFileHeaderRowNull() throws Exception {
        MockMultipartFile file = createExcelFileWithNoRowAtAll();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            productExcelParsingService.parseExcelFile(file, testEntId)
        );

        assertTrue(exception.getMessage().contains("encabezados") || 
                   exception.getMessage().contains("headers"));
    }

    @Test
    @DisplayName("Debe manejar celda con valor null explícito")
    void testParseExcelFileNullCells() throws Exception {
        MockMultipartFile file = createExcelFileWithNullCells();

        ProductExcelParsingService.ExcelParsingResult result = 
            productExcelParsingService.parseExcelFile(file, testEntId);

        assertNotNull(result);
        assertEquals(1, result.getProductsData().size());
    }

    @Test
    @DisplayName("Debe manejar cantidad con espacios en blanco")
    void testParseExcelFileQuantityWithSpaces() throws Exception {
        MockMultipartFile file = createExcelFileWithSpacesInNumericFields();

        ProductExcelParsingService.ExcelParsingResult result = 
            productExcelParsingService.parseExcelFile(file, testEntId);

        assertNotNull(result);
        assertEquals(1, result.getProductsData().size());
        assertNotNull(result.getProductsData().get(0).getQuantity());
    }

    @Test
    @DisplayName("Debe manejar celda con tipo fórmula")
    void testParseExcelFileFormulaCell() throws Exception {
        MockMultipartFile file = createExcelFileWithFormulaCell();

        ProductExcelParsingService.ExcelParsingResult result = 
            productExcelParsingService.parseExcelFile(file, testEntId);

        assertNotNull(result);
        assertEquals(1, result.getProductsData().size());
    }

    @Test
    @DisplayName("Debe manejar fila con todas las celdas en blanco")
    void testParseExcelFileRowWithBlankCells() throws Exception {
        MockMultipartFile file = createExcelFileWithBlankRow();

        ProductExcelParsingService.ExcelParsingResult result = 
            productExcelParsingService.parseExcelFile(file, testEntId);

        assertNotNull(result);
        assertEquals(1, result.getProductsData().size());
    }

    @Test
    @DisplayName("Debe manejar columnIndex null en validación")
    void testParseExcelFileNullColumnIndex() throws Exception {
        MockMultipartFile file = createExcelFileWithMissingColumn();

        ProductExcelParsingService.ExcelParsingResult result = 
            productExcelParsingService.parseExcelFile(file, testEntId);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Debe manejar cantidad solo con espacios")
    void testParseExcelFileQuantityOnlySpaces() throws Exception {
        MockMultipartFile file = createExcelFileWithOnlySpaces();

        ProductExcelParsingService.ExcelParsingResult result = 
            productExcelParsingService.parseExcelFile(file, testEntId);

        assertNotNull(result);
        assertEquals(1, result.getProductsData().size());
        assertNull(result.getProductsData().get(0).getQuantity());
    }

    @Test
    @DisplayName("Debe manejar celda con tipo error")
    void testParseExcelFileErrorCell() throws Exception {
        MockMultipartFile file = createExcelFileWithErrorCell();

        ProductExcelParsingService.ExcelParsingResult result = 
            productExcelParsingService.parseExcelFile(file, testEntId);

        assertNotNull(result);
    }

    private MockMultipartFile createValidExcelFile() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Código");
        createCell(headerRow, 1, "Nombre");
        createCell(headerRow, 2, "Referencia/SKU");
        createCell(headerRow, 3, "Presentación");
        createCell(headerRow, 4, "Descripción");
        createCell(headerRow, 5, "Costo");
        createCell(headerRow, 6, "Cantidad");
        createCell(headerRow, 7, "Unidad de Medida");
        createCell(headerRow, 8, "Categoría");
        createCell(headerRow, 9, "Tipo de Producto");
        createCell(headerRow, 10, "Estado");

        Row dataRow = sheet.createRow(1);
        createCell(dataRow, 0, "");
        createCell(dataRow, 1, "Producto Test");
        createCell(dataRow, 2, "REF001");
        createCell(dataRow, 3, "Caja x12");
        createCell(dataRow, 4, "Descripción");
        createCell(dataRow, 5, "150.50");
        createCell(dataRow, 6, "100");
        createCell(dataRow, 7, "Unidad");
        createCell(dataRow, 8, "Categoría");
        createCell(dataRow, 9, "Tipo");
        createCell(dataRow, 10, "ACTIVO");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private byte[] createValidExcelFileBytes() throws Exception {
        return createValidExcelFile().getBytes();
    }

    private MockMultipartFile createEmptyExcelFile() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        workbook.createSheet("Productos");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private MockMultipartFile createExcelFileWithInvalidHeaders() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Columna Inválida");
        createCell(headerRow, 1, "Otra Columna");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private MockMultipartFile createExcelFileWithEmptyRows() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Código");
        createCell(headerRow, 1, "Nombre");
        createCell(headerRow, 2, "Referencia/SKU");
        createCell(headerRow, 3, "Presentación");
        createCell(headerRow, 4, "Descripción");
        createCell(headerRow, 5, "Costo");
        createCell(headerRow, 6, "Cantidad");
        createCell(headerRow, 7, "Unidad de Medida");
        createCell(headerRow, 8, "Categoría");
        createCell(headerRow, 9, "Tipo de Producto");
        createCell(headerRow, 10, "Estado");

        sheet.createRow(1);

        Row dataRow = sheet.createRow(2);
        createCell(dataRow, 0, "");
        createCell(dataRow, 1, "Producto Test");
        createCell(dataRow, 2, "REF001");
        createCell(dataRow, 3, "Caja x12");
        createCell(dataRow, 4, "Descripción");
        createCell(dataRow, 5, "150.50");
        createCell(dataRow, 6, "100");
        createCell(dataRow, 7, "Unidad");
        createCell(dataRow, 8, "Categoría");
        createCell(dataRow, 9, "Tipo");
        createCell(dataRow, 10, "ACTIVO");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private MockMultipartFile createExcelFileWithInvalidQuantity() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Código");
        createCell(headerRow, 1, "Nombre");
        createCell(headerRow, 2, "Referencia/SKU");
        createCell(headerRow, 3, "Presentación");
        createCell(headerRow, 4, "Descripción");
        createCell(headerRow, 5, "Costo");
        createCell(headerRow, 6, "Cantidad");
        createCell(headerRow, 7, "Unidad de Medida");
        createCell(headerRow, 8, "Categoría");
        createCell(headerRow, 9, "Tipo de Producto");
        createCell(headerRow, 10, "Estado");

        Row dataRow = sheet.createRow(1);
        createCell(dataRow, 0, "");
        createCell(dataRow, 1, "Producto Test");
        createCell(dataRow, 2, "REF001");
        createCell(dataRow, 3, "Caja x12");
        createCell(dataRow, 4, "Descripción");
        createCell(dataRow, 5, "150.50");
        createCell(dataRow, 6, "abc");
        createCell(dataRow, 7, "Unidad");
        createCell(dataRow, 8, "Categoría");
        createCell(dataRow, 9, "Tipo");
        createCell(dataRow, 10, "ACTIVO");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private MockMultipartFile createExcelFileWithInvalidCost() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Código");
        createCell(headerRow, 1, "Nombre");
        createCell(headerRow, 2, "Referencia/SKU");
        createCell(headerRow, 3, "Presentación");
        createCell(headerRow, 4, "Descripción");
        createCell(headerRow, 5, "Costo");
        createCell(headerRow, 6, "Cantidad");
        createCell(headerRow, 7, "Unidad de Medida");
        createCell(headerRow, 8, "Categoría");
        createCell(headerRow, 9, "Tipo de Producto");
        createCell(headerRow, 10, "Estado");

        Row dataRow = sheet.createRow(1);
        createCell(dataRow, 0, "");
        createCell(dataRow, 1, "Producto Test");
        createCell(dataRow, 2, "REF001");
        createCell(dataRow, 3, "Caja x12");
        createCell(dataRow, 4, "Descripción");
        createCell(dataRow, 5, "xyz");
        createCell(dataRow, 6, "100");
        createCell(dataRow, 7, "Unidad");
        createCell(dataRow, 8, "Categoría");
        createCell(dataRow, 9, "Tipo");
        createCell(dataRow, 10, "ACTIVO");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private void createCell(Row row, int columnIndex, String value) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
    }

    private MockMultipartFile createExcelFileWithExcessiveQuantity() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Código");
        createCell(headerRow, 1, "Nombre");
        createCell(headerRow, 2, "Referencia/SKU");
        createCell(headerRow, 3, "Presentación");
        createCell(headerRow, 4, "Descripción");
        createCell(headerRow, 5, "Costo");
        createCell(headerRow, 6, "Cantidad");
        createCell(headerRow, 7, "Unidad de Medida");
        createCell(headerRow, 8, "Categoría");
        createCell(headerRow, 9, "Tipo de Producto");
        createCell(headerRow, 10, "Estado");

        Row dataRow = sheet.createRow(1);
        createCell(dataRow, 0, "");
        createCell(dataRow, 1, "Producto Test");
        createCell(dataRow, 2, "REF001");
        createCell(dataRow, 3, "Caja x12");
        createCell(dataRow, 4, "Descripción");
        createCell(dataRow, 5, "100.50");
        createCell(dataRow, 6, "9223372036854775806");
        createCell(dataRow, 7, "Unidad");
        createCell(dataRow, 8, "Categoría");
        createCell(dataRow, 9, "Tipo");
        createCell(dataRow, 10, "ACTIVO");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private MockMultipartFile createExcelFileWithNegativeQuantity() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Código");
        createCell(headerRow, 1, "Nombre");
        createCell(headerRow, 2, "Referencia/SKU");
        createCell(headerRow, 3, "Presentación");
        createCell(headerRow, 4, "Descripción");
        createCell(headerRow, 5, "Costo");
        createCell(headerRow, 6, "Cantidad");
        createCell(headerRow, 7, "Unidad de Medida");
        createCell(headerRow, 8, "Categoría");
        createCell(headerRow, 9, "Tipo de Producto");
        createCell(headerRow, 10, "Estado");

        Row dataRow = sheet.createRow(1);
        createCell(dataRow, 0, "");
        createCell(dataRow, 1, "Producto Test");
        createCell(dataRow, 2, "REF001");
        createCell(dataRow, 3, "Caja x12");
        createCell(dataRow, 4, "Descripción");
        createCell(dataRow, 5, "100.50");
        createCell(dataRow, 6, "-50");
        createCell(dataRow, 7, "Unidad");
        createCell(dataRow, 8, "Categoría");
        createCell(dataRow, 9, "Tipo");
        createCell(dataRow, 10, "ACTIVO");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private MockMultipartFile createExcelFileWithExcessiveCost() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Código");
        createCell(headerRow, 1, "Nombre");
        createCell(headerRow, 2, "Referencia/SKU");
        createCell(headerRow, 3, "Presentación");
        createCell(headerRow, 4, "Descripción");
        createCell(headerRow, 5, "Costo");
        createCell(headerRow, 6, "Cantidad");
        createCell(headerRow, 7, "Unidad de Medida");
        createCell(headerRow, 8, "Categoría");
        createCell(headerRow, 9, "Tipo de Producto");
        createCell(headerRow, 10, "Estado");

        Row dataRow = sheet.createRow(1);
        createCell(dataRow, 0, "");
        createCell(dataRow, 1, "Producto Test");
        createCell(dataRow, 2, "REF001");
        createCell(dataRow, 3, "Caja x12");
        createCell(dataRow, 4, "Descripción");
        createCell(dataRow, 5, "999999999999999");
        createCell(dataRow, 6, "100");
        createCell(dataRow, 7, "Unidad");
        createCell(dataRow, 8, "Categoría");
        createCell(dataRow, 9, "Tipo");
        createCell(dataRow, 10, "ACTIVO");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private MockMultipartFile createExcelFileWithNegativeCost() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Código");
        createCell(headerRow, 1, "Nombre");
        createCell(headerRow, 2, "Referencia/SKU");
        createCell(headerRow, 3, "Presentación");
        createCell(headerRow, 4, "Descripción");
        createCell(headerRow, 5, "Costo");
        createCell(headerRow, 6, "Cantidad");
        createCell(headerRow, 7, "Unidad de Medida");
        createCell(headerRow, 8, "Categoría");
        createCell(headerRow, 9, "Tipo de Producto");
        createCell(headerRow, 10, "Estado");

        Row dataRow = sheet.createRow(1);
        createCell(dataRow, 0, "");
        createCell(dataRow, 1, "Producto Test");
        createCell(dataRow, 2, "REF001");
        createCell(dataRow, 3, "Caja x12");
        createCell(dataRow, 4, "Descripción");
        createCell(dataRow, 5, "-100.50");
        createCell(dataRow, 6, "100");
        createCell(dataRow, 7, "Unidad");
        createCell(dataRow, 8, "Categoría");
        createCell(dataRow, 9, "Tipo");
        createCell(dataRow, 10, "ACTIVO");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private MockMultipartFile createExcelFileWithEmptyNumericFields() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Código");
        createCell(headerRow, 1, "Nombre");
        createCell(headerRow, 2, "Referencia/SKU");
        createCell(headerRow, 3, "Presentación");
        createCell(headerRow, 4, "Descripción");
        createCell(headerRow, 5, "Costo");
        createCell(headerRow, 6, "Cantidad");
        createCell(headerRow, 7, "Unidad de Medida");
        createCell(headerRow, 8, "Categoría");
        createCell(headerRow, 9, "Tipo de Producto");
        createCell(headerRow, 10, "Estado");

        Row dataRow = sheet.createRow(1);
        createCell(dataRow, 0, "");
        createCell(dataRow, 1, "Producto Test");
        createCell(dataRow, 2, "REF001");
        createCell(dataRow, 3, "Caja x12");
        createCell(dataRow, 4, "Descripción");
        createCell(dataRow, 5, "");
        createCell(dataRow, 6, "");
        createCell(dataRow, 7, "Unidad");
        createCell(dataRow, 8, "Categoría");
        createCell(dataRow, 9, "Tipo");
        createCell(dataRow, 10, "ACTIVO");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private MockMultipartFile createExcelFileWithBooleanCell() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Código");
        createCell(headerRow, 1, "Nombre");
        createCell(headerRow, 2, "Referencia/SKU");
        createCell(headerRow, 3, "Presentación");
        createCell(headerRow, 4, "Descripción");
        createCell(headerRow, 5, "Costo");
        createCell(headerRow, 6, "Cantidad");
        createCell(headerRow, 7, "Unidad de Medida");
        createCell(headerRow, 8, "Categoría");
        createCell(headerRow, 9, "Tipo de Producto");
        createCell(headerRow, 10, "Estado");

        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("");
        dataRow.createCell(1).setCellValue("Producto Test");
        dataRow.createCell(2).setCellValue("REF001");
        dataRow.createCell(3).setCellValue("Caja x12");
        dataRow.createCell(4).setCellValue("Descripción");
        dataRow.createCell(5).setCellValue("100.50");
        dataRow.createCell(6).setCellValue("100");
        dataRow.createCell(7).setCellValue("Unidad");
        dataRow.createCell(8).setCellValue("Categoría");
        dataRow.createCell(9).setCellValue("Tipo");
        dataRow.createCell(10).setCellValue(true);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private MockMultipartFile createExcelFileWithoutHeaders() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        Row dataRow = sheet.createRow(0);
        dataRow.createCell(0);
        dataRow.createCell(1);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private MockMultipartFile createExcelFileWithNoRowAtAll() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");
        
        sheet.createRow(1);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private MockMultipartFile createExcelFileWithNumericInTextFields() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Código");
        createCell(headerRow, 1, "Nombre");
        createCell(headerRow, 2, "Referencia/SKU");
        createCell(headerRow, 3, "Presentación");
        createCell(headerRow, 4, "Descripción");
        createCell(headerRow, 5, "Costo");
        createCell(headerRow, 6, "Cantidad");
        createCell(headerRow, 7, "Unidad de Medida");
        createCell(headerRow, 8, "Categoría");
        createCell(headerRow, 9, "Tipo de Producto");
        createCell(headerRow, 10, "Estado");

        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("");
        dataRow.createCell(1).setCellValue("Producto Test");
        dataRow.createCell(2).setCellValue(123456);
        dataRow.createCell(3).setCellValue("Caja x12");
        dataRow.createCell(4).setCellValue("Descripción");
        dataRow.createCell(5).setCellValue("100.50");
        dataRow.createCell(6).setCellValue("100");
        dataRow.createCell(7).setCellValue("Unidad");
        dataRow.createCell(8).setCellValue("Categoría");
        dataRow.createCell(9).setCellValue("Tipo");
        dataRow.createCell(10).setCellValue("ACTIVO");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private MockMultipartFile createExcelFileWithNullCells() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Código");
        createCell(headerRow, 1, "Nombre");
        createCell(headerRow, 2, "Referencia/SKU");
        createCell(headerRow, 3, "Presentación");
        createCell(headerRow, 4, "Descripción");
        createCell(headerRow, 5, "Costo");
        createCell(headerRow, 6, "Cantidad");
        createCell(headerRow, 7, "Unidad de Medida");
        createCell(headerRow, 8, "Categoría");
        createCell(headerRow, 9, "Tipo de Producto");
        createCell(headerRow, 10, "Estado");

        Row dataRow = sheet.createRow(1);
        createCell(dataRow, 1, "Producto Test");
        createCell(dataRow, 2, "REF001");
        createCell(dataRow, 3, "Caja x12");
        createCell(dataRow, 4, "Descripción");
        createCell(dataRow, 7, "Unidad");
        createCell(dataRow, 8, "Categoría");
        createCell(dataRow, 9, "Tipo");
        createCell(dataRow, 10, "ACTIVO");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private MockMultipartFile createExcelFileWithSpacesInNumericFields() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Código");
        createCell(headerRow, 1, "Nombre");
        createCell(headerRow, 2, "Referencia/SKU");
        createCell(headerRow, 3, "Presentación");
        createCell(headerRow, 4, "Descripción");
        createCell(headerRow, 5, "Costo");
        createCell(headerRow, 6, "Cantidad");
        createCell(headerRow, 7, "Unidad de Medida");
        createCell(headerRow, 8, "Categoría");
        createCell(headerRow, 9, "Tipo de Producto");
        createCell(headerRow, 10, "Estado");

        Row dataRow = sheet.createRow(1);
        createCell(dataRow, 0, "");
        createCell(dataRow, 1, "Producto Test");
        createCell(dataRow, 2, "REF001");
        createCell(dataRow, 3, "Caja x12");
        createCell(dataRow, 4, "Descripción");
        createCell(dataRow, 5, " 150.50 ");
        createCell(dataRow, 6, " 100 ");
        createCell(dataRow, 7, "Unidad");
        createCell(dataRow, 8, "Categoría");
        createCell(dataRow, 9, "Tipo");
        createCell(dataRow, 10, "ACTIVO");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private MockMultipartFile createExcelFileWithFormulaCell() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Código");
        createCell(headerRow, 1, "Nombre");
        createCell(headerRow, 2, "Referencia/SKU");
        createCell(headerRow, 3, "Presentación");
        createCell(headerRow, 4, "Descripción");
        createCell(headerRow, 5, "Costo");
        createCell(headerRow, 6, "Cantidad");
        createCell(headerRow, 7, "Unidad de Medida");
        createCell(headerRow, 8, "Categoría");
        createCell(headerRow, 9, "Tipo de Producto");
        createCell(headerRow, 10, "Estado");

        Row dataRow = sheet.createRow(1);
        createCell(dataRow, 0, "");
        createCell(dataRow, 1, "Producto Test");
        createCell(dataRow, 2, "REF001");
        createCell(dataRow, 3, "Caja x12");
        createCell(dataRow, 4, "Descripción");
        createCell(dataRow, 5, "100.50");
        dataRow.createCell(6).setCellFormula("50+50");
        createCell(dataRow, 7, "Unidad");
        createCell(dataRow, 8, "Categoría");
        createCell(dataRow, 9, "Tipo");
        createCell(dataRow, 10, "ACTIVO");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private MockMultipartFile createExcelFileWithBlankRow() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Código");
        createCell(headerRow, 1, "Nombre");
        createCell(headerRow, 2, "Referencia/SKU");
        createCell(headerRow, 3, "Presentación");
        createCell(headerRow, 4, "Descripción");
        createCell(headerRow, 5, "Costo");
        createCell(headerRow, 6, "Cantidad");
        createCell(headerRow, 7, "Unidad de Medida");
        createCell(headerRow, 8, "Categoría");
        createCell(headerRow, 9, "Tipo de Producto");
        createCell(headerRow, 10, "Estado");

        Row blankRow = sheet.createRow(1);
        for (int i = 0; i <= 10; i++) {
            blankRow.createCell(i, CellType.BLANK);
        }

        Row dataRow = sheet.createRow(2);
        createCell(dataRow, 0, "");
        createCell(dataRow, 1, "Producto Test");
        createCell(dataRow, 2, "REF001");
        createCell(dataRow, 3, "Caja x12");
        createCell(dataRow, 4, "Descripción");
        createCell(dataRow, 5, "100.50");
        createCell(dataRow, 6, "100");
        createCell(dataRow, 7, "Unidad");
        createCell(dataRow, 8, "Categoría");
        createCell(dataRow, 9, "Tipo");
        createCell(dataRow, 10, "ACTIVO");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private MockMultipartFile createExcelFileWithMissingColumn() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Código");
        createCell(headerRow, 1, "Nombre");
        createCell(headerRow, 2, "Referencia/SKU");
        createCell(headerRow, 3, "Presentación");
        createCell(headerRow, 4, "Descripción");
        createCell(headerRow, 5, "Costo");
        createCell(headerRow, 6, "Cantidad");
        createCell(headerRow, 7, "Unidad de Medida");
        createCell(headerRow, 8, "Categoría");
        createCell(headerRow, 9, "Tipo de Producto");
        createCell(headerRow, 10, "Estado");

        Row dataRow = sheet.createRow(1);
        createCell(dataRow, 1, "Producto Test");
        createCell(dataRow, 2, "REF001");
        createCell(dataRow, 3, "Caja x12");
        createCell(dataRow, 4, "Descripción");
        createCell(dataRow, 5, "100.50");
        createCell(dataRow, 6, "100");
        createCell(dataRow, 7, "Unidad");
        createCell(dataRow, 8, "Categoría");
        createCell(dataRow, 9, "Tipo");
        createCell(dataRow, 10, "ACTIVO");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private MockMultipartFile createExcelFileWithOnlySpaces() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Código");
        createCell(headerRow, 1, "Nombre");
        createCell(headerRow, 2, "Referencia/SKU");
        createCell(headerRow, 3, "Presentación");
        createCell(headerRow, 4, "Descripción");
        createCell(headerRow, 5, "Costo");
        createCell(headerRow, 6, "Cantidad");
        createCell(headerRow, 7, "Unidad de Medida");
        createCell(headerRow, 8, "Categoría");
        createCell(headerRow, 9, "Tipo de Producto");
        createCell(headerRow, 10, "Estado");

        Row dataRow = sheet.createRow(1);
        createCell(dataRow, 0, "");
        createCell(dataRow, 1, "Producto Test");
        createCell(dataRow, 2, "REF001");
        createCell(dataRow, 3, "Caja x12");
        createCell(dataRow, 4, "Descripción");
        createCell(dataRow, 5, "   ");
        createCell(dataRow, 6, "   ");
        createCell(dataRow, 7, "Unidad");
        createCell(dataRow, 8, "Categoría");
        createCell(dataRow, 9, "Tipo");
        createCell(dataRow, 10, "ACTIVO");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private MockMultipartFile createExcelFileWithErrorCell() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Código");
        createCell(headerRow, 1, "Nombre");
        createCell(headerRow, 2, "Referencia/SKU");
        createCell(headerRow, 3, "Presentación");
        createCell(headerRow, 4, "Descripción");
        createCell(headerRow, 5, "Costo");
        createCell(headerRow, 6, "Cantidad");
        createCell(headerRow, 7, "Unidad de Medida");
        createCell(headerRow, 8, "Categoría");
        createCell(headerRow, 9, "Tipo de Producto");
        createCell(headerRow, 10, "Estado");

        Row dataRow = sheet.createRow(1);
        createCell(dataRow, 0, "");
        createCell(dataRow, 1, "Producto Test");
        createCell(dataRow, 2, "REF001");
        createCell(dataRow, 3, "Caja x12");
        createCell(dataRow, 4, "Descripción");
        createCell(dataRow, 5, "100.50");
        createCell(dataRow, 6, "100");
        createCell(dataRow, 7, "Unidad");
        createCell(dataRow, 8, "Categoría");
        createCell(dataRow, 9, "Tipo");
        Cell errorCell = dataRow.createCell(10, CellType.ERROR);
        errorCell.setCellErrorValue(FormulaError.VALUE.getCode());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    private MockMultipartFile createExcelFileWithQuantityOverLimit() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Código");
        createCell(headerRow, 1, "Nombre");
        createCell(headerRow, 2, "Referencia/SKU");
        createCell(headerRow, 3, "Presentación");
        createCell(headerRow, 4, "Descripción");
        createCell(headerRow, 5, "Costo");
        createCell(headerRow, 6, "Cantidad");
        createCell(headerRow, 7, "Unidad de Medida");
        createCell(headerRow, 8, "Categoría");
        createCell(headerRow, 9, "Tipo de Producto");
        createCell(headerRow, 10, "Estado");

        Row dataRow = sheet.createRow(1);
        createCell(dataRow, 0, "");
        createCell(dataRow, 1, "Producto Test");
        createCell(dataRow, 2, "REF001");
        createCell(dataRow, 3, "Caja x12");
        createCell(dataRow, 4, "Descripción");
        createCell(dataRow, 5, "100.50");
        createCell(dataRow, 6, "99999999999999999999999999999");
        createCell(dataRow, 7, "Unidad");
        createCell(dataRow, 8, "Categoría");
        createCell(dataRow, 9, "Tipo");
        createCell(dataRow, 10, "ACTIVO");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "productos.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }
}
