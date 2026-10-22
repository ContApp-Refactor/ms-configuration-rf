package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.application.services.importExport;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport.AccountCatalogueExcelParsingService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogueExcelData;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueImportException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueExcelParsingServiceUnitTest {

    private AccountCatalogueExcelParsingService excelParsingService;
    private String entId;

    @BeforeEach
    void setUp() {
        excelParsingService = new AccountCatalogueExcelParsingService();
        entId = "ENT-001";
    }

    @Test
    @DisplayName("Debe parsear archivo Excel válido correctamente")
    void testParseExcelFileSuccess() throws IOException {
        // Arrange
        byte[] excelBytes = createValidExcelFile();
        MockMultipartFile file = new MockMultipartFile(
                "file", "catalogo.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                excelBytes);

        // Act
        AccountCatalogueExcelParsingService.ExcelParsingResult result = 
                excelParsingService.parseExcelFile(file, entId);

        // Assert
        assertNotNull(result);
        assertFalse(result.getAccountsData().isEmpty());
        assertEquals(1, result.getTotalRows());
        assertNotNull(result.getColumnMap());
    }

    @Test
    @DisplayName("Debe parsear archivo Excel desde bytes correctamente")
    void testParseExcelFileFromBytesSuccess() throws IOException {
        // Arrange
        byte[] excelBytes = createValidExcelFile();

        // Act
        AccountCatalogueExcelParsingService.ExcelParsingResult result = 
                excelParsingService.parseExcelFileFromBytes(excelBytes, entId);

        // Assert
        assertNotNull(result);
        assertFalse(result.getAccountsData().isEmpty());
        assertEquals(1, result.getTotalRows());
    }

    @Test
    @DisplayName("Debe extraer código correctamente de la fila")
    void testParseExcelFileExtractsCode() throws IOException {
        // Arrange
        byte[] excelBytes = createValidExcelFile();

        // Act
        AccountCatalogueExcelParsingService.ExcelParsingResult result = 
                excelParsingService.parseExcelFileFromBytes(excelBytes, entId);

        // Assert
        AccountCatalogueExcelData data = result.getAccountsData().get(0);
        assertEquals("1105", data.getCode());
    }

    @Test
    @DisplayName("Debe extraer descripción correctamente de la fila")
    void testParseExcelFileExtractsDescription() throws IOException {
        // Arrange
        byte[] excelBytes = createValidExcelFile();

        // Act
        AccountCatalogueExcelParsingService.ExcelParsingResult result = 
                excelParsingService.parseExcelFileFromBytes(excelBytes, entId);

        // Assert
        AccountCatalogueExcelData data = result.getAccountsData().get(0);
        assertEquals("Caja General", data.getDescription());
    }

    @Test
    @DisplayName("Debe parsear enum Nature correctamente")
    void testParseExcelFileParsesNature() throws IOException {
        // Arrange
        byte[] excelBytes = createValidExcelFile();

        // Act
        AccountCatalogueExcelParsingService.ExcelParsingResult result = 
                excelParsingService.parseExcelFileFromBytes(excelBytes, entId);

        // Assert
        AccountCatalogueExcelData data = result.getAccountsData().get(0);
        assertEquals(NatureEnum.DEBIT, data.getNature());
    }

    @Test
    @DisplayName("Debe parsear enum FinancialStatus correctamente")
    void testParseExcelFileParsesFinancialStatus() throws IOException {
        // Arrange
        byte[] excelBytes = createValidExcelFile();

        // Act
        AccountCatalogueExcelParsingService.ExcelParsingResult result = 
                excelParsingService.parseExcelFileFromBytes(excelBytes, entId);

        // Assert
        AccountCatalogueExcelData data = result.getAccountsData().get(0);
        assertEquals(FinancialStatusEnum.STATEMENTFINANCIALPOSITION, data.getFinancialStatus());
    }

    @Test
    @DisplayName("Debe parsear enum Classification correctamente")
    void testParseExcelFileParsesClassification() throws IOException {
        // Arrange
        byte[] excelBytes = createValidExcelFile();

        // Act
        AccountCatalogueExcelParsingService.ExcelParsingResult result = 
                excelParsingService.parseExcelFileFromBytes(excelBytes, entId);

        // Assert
        AccountCatalogueExcelData data = result.getAccountsData().get(0);
        assertEquals(ClassificationEnum.CURRENTASSETS, data.getClassification());
    }

    @Test
    @DisplayName("Debe parsear campo booleano Cruce correctamente")
    void testParseExcelFileParsesCrossing() throws IOException {
        // Arrange
        byte[] excelBytes = createExcelFileWithBooleans("SI", "NO");

        // Act
        AccountCatalogueExcelParsingService.ExcelParsingResult result = 
                excelParsingService.parseExcelFileFromBytes(excelBytes, entId);

        // Assert
        AccountCatalogueExcelData data = result.getAccountsData().get(0);
        assertTrue(data.getCrossing());
    }

    @Test
    @DisplayName("Debe parsear campo booleano Centro de Costo correctamente")
    void testParseExcelFileParsesCostCenter() throws IOException {
        // Arrange
        byte[] excelBytes = createExcelFileWithBooleans("NO", "SI");

        // Act
        AccountCatalogueExcelParsingService.ExcelParsingResult result = 
                excelParsingService.parseExcelFileFromBytes(excelBytes, entId);

        // Assert
        AccountCatalogueExcelData data = result.getAccountsData().get(0);
        assertTrue(data.getCostCenter());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando archivo está vacío")
    void testParseExcelFileThrowsExceptionForEmptyFile() throws IOException {
        // Arrange
        byte[] emptyExcelBytes = createEmptyExcelFile();

        // Act & Assert
        assertThrows(AccountCatalogueImportException.class,
                () -> excelParsingService.parseExcelFileFromBytes(emptyExcelBytes, entId));
    }

    @Test
    @DisplayName("Debe registrar error cuando falta encabezado requerido")
    void testParseExcelFileRecordsErrorForMissingHeader() throws IOException {
        // Arrange
        byte[] excelBytes = createExcelFileWithMissingHeader();

        // Act
        AccountCatalogueExcelParsingService.ExcelParsingResult result = 
                excelParsingService.parseExcelFileFromBytes(excelBytes, entId);

        // Assert
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getErrorCode().equals("MISSING_REQUIRED_HEADER")));
    }

    @Test
    @DisplayName("Debe asignar entId a cada registro parseado")
    void testParseExcelFileAssignsEntId() throws IOException {
        // Arrange
        byte[] excelBytes = createValidExcelFile();

        // Act
        AccountCatalogueExcelParsingService.ExcelParsingResult result = 
                excelParsingService.parseExcelFileFromBytes(excelBytes, entId);

        // Assert
        AccountCatalogueExcelData data = result.getAccountsData().get(0);
        assertEquals(entId, data.getIdEnterprise());
    }

    @Test
    @DisplayName("Debe asignar número de fila correcto a cada registro")
    void testParseExcelFileAssignsRowNumber() throws IOException {
        // Arrange
        byte[] excelBytes = createValidExcelFile();

        // Act
        AccountCatalogueExcelParsingService.ExcelParsingResult result = 
                excelParsingService.parseExcelFileFromBytes(excelBytes, entId);

        // Assert
        AccountCatalogueExcelData data = result.getAccountsData().get(0);
        assertEquals(2, data.getRowNumber());
    }

    @Test
    @DisplayName("Debe ignorar filas vacías")
    void testParseExcelFileIgnoresEmptyRows() throws IOException {
        // Arrange
        byte[] excelBytes = createExcelFileWithEmptyRow();

        // Act
        AccountCatalogueExcelParsingService.ExcelParsingResult result = 
                excelParsingService.parseExcelFileFromBytes(excelBytes, entId);

        // Assert
        assertEquals(1, result.getTotalRows());
    }

    @Test
    @DisplayName("Debe registrar error para valor de enum inválido en Naturaleza")
    void testParseExcelFileRecordsErrorForInvalidNature() throws IOException {
        // Arrange
        byte[] excelBytes = createExcelFileWithInvalidNature();

        // Act
        AccountCatalogueExcelParsingService.ExcelParsingResult result = 
                excelParsingService.parseExcelFileFromBytes(excelBytes, entId);

        // Assert
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getErrorCode().equals("INVALID_ENUM_VALUE")));
    }

    @Test
    @DisplayName("Debe detectar mapeo de columnas dinámicamente")
    void testParseExcelFileDetectsColumnMapping() throws IOException {
        // Arrange
        byte[] excelBytes = createValidExcelFile();

        // Act
        AccountCatalogueExcelParsingService.ExcelParsingResult result = 
                excelParsingService.parseExcelFileFromBytes(excelBytes, entId);

        // Assert
        assertNotNull(result.getColumnMap());
        assertTrue(result.getColumnMap().containsKey("Código"));
        assertTrue(result.getColumnMap().containsKey("Nombre"));
    }

    @Test
    @DisplayName("Debe parsear múltiples filas correctamente")
    void testParseExcelFileParsesMultipleRows() throws IOException {
        // Arrange
        byte[] excelBytes = createExcelFileWithMultipleRows();

        // Act
        AccountCatalogueExcelParsingService.ExcelParsingResult result = 
                excelParsingService.parseExcelFileFromBytes(excelBytes, entId);

        // Assert
        assertEquals(2, result.getTotalRows());
        assertEquals(2, result.getAccountsData().size());
    }

    @Test
    @DisplayName("Debe manejar código numérico sin decimales")
    void testParseExcelFileHandlesNumericCode() throws IOException {
        // Arrange
        byte[] excelBytes = createExcelFileWithNumericCode();

        // Act
        AccountCatalogueExcelParsingService.ExcelParsingResult result = 
                excelParsingService.parseExcelFileFromBytes(excelBytes, entId);

        // Assert
        AccountCatalogueExcelData data = result.getAccountsData().get(0);
        assertNotNull(data.getCode());
        assertFalse(data.getCode().contains("."));
    }

    @Test
    @DisplayName("Debe manejar valores booleanos en diferentes formatos")
    void testParseExcelFileHandlesBooleanFormats() throws IOException {
        // Arrange
        byte[] excelBytes = createExcelFileWithBooleans("TRUE", "FALSE");

        // Act
        AccountCatalogueExcelParsingService.ExcelParsingResult result = 
                excelParsingService.parseExcelFileFromBytes(excelBytes, entId);

        // Assert
        AccountCatalogueExcelData data = result.getAccountsData().get(0);
        assertTrue(data.getCrossing());
        assertFalse(data.getCostCenter());
    }

    @Test
    @DisplayName("Debe retornar null para booleanos no reconocidos")
    void testParseExcelFileReturnsNullForUnrecognizedBoolean() throws IOException {
        // Arrange
        byte[] excelBytes = createExcelFileWithBooleans("MAYBE", "QUIZAS");

        // Act
        AccountCatalogueExcelParsingService.ExcelParsingResult result = 
                excelParsingService.parseExcelFileFromBytes(excelBytes, entId);

        // Assert
        AccountCatalogueExcelData data = result.getAccountsData().get(0);
        assertNull(data.getCrossing());
        assertNull(data.getCostCenter());
    }

    @Test
    @DisplayName("Debe normalizar encabezados con acentos")
    void testParseExcelFileNormalizesAccentedHeaders() throws IOException {
        // Arrange
        byte[] excelBytes = createExcelFileWithAccentedHeaders();

        // Act
        AccountCatalogueExcelParsingService.ExcelParsingResult result = 
                excelParsingService.parseExcelFileFromBytes(excelBytes, entId);

        // Assert
        assertNotNull(result);
        assertFalse(result.getAccountsData().isEmpty());
    }

    // Métodos auxiliares para crear archivos Excel de prueba

    private byte[] createValidExcelFile() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Catalogo");
            
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Código");
            headerRow.createCell(1).setCellValue("Nombre");
            headerRow.createCell(2).setCellValue("Naturaleza");
            headerRow.createCell(3).setCellValue("Estado Financiero");
            headerRow.createCell(4).setCellValue("Clasificación");
            
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("1105");
            dataRow.createCell(1).setCellValue("Caja General");
            dataRow.createCell(2).setCellValue("Debito");
            dataRow.createCell(3).setCellValue("Estado de Situacion Financiero");
            dataRow.createCell(4).setCellValue("Activo Corriente");
            
            return toByteArray(workbook);
        }
    }

    private byte[] createExcelFileWithBooleans(String crossing, String costCenter) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Catalogo");
            
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Código");
            headerRow.createCell(1).setCellValue("Nombre");
            headerRow.createCell(2).setCellValue("Naturaleza");
            headerRow.createCell(3).setCellValue("Estado Financiero");
            headerRow.createCell(4).setCellValue("Clasificación");
            headerRow.createCell(5).setCellValue("Cruce");
            headerRow.createCell(6).setCellValue("Centro de Costo");
            
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("11050101");
            dataRow.createCell(1).setCellValue("Caja General Bogota");
            dataRow.createCell(2).setCellValue("Debito");
            dataRow.createCell(3).setCellValue("Estado de Situacion Financiero");
            dataRow.createCell(4).setCellValue("Activo Corriente");
            dataRow.createCell(5).setCellValue(crossing);
            dataRow.createCell(6).setCellValue(costCenter);
            
            return toByteArray(workbook);
        }
    }

    private byte[] createEmptyExcelFile() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            workbook.createSheet("Catalogo");
            return toByteArray(workbook);
        }
    }

    private byte[] createExcelFileWithMissingHeader() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Catalogo");
            
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Código");
            headerRow.createCell(1).setCellValue("Nombre");
            // Falta Naturaleza, Estado Financiero, Clasificación
            
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("1105");
            dataRow.createCell(1).setCellValue("Caja General");
            
            return toByteArray(workbook);
        }
    }

    private byte[] createExcelFileWithEmptyRow() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Catalogo");
            
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Código");
            headerRow.createCell(1).setCellValue("Nombre");
            headerRow.createCell(2).setCellValue("Naturaleza");
            headerRow.createCell(3).setCellValue("Estado Financiero");
            headerRow.createCell(4).setCellValue("Clasificación");
            
            sheet.createRow(1);
            
            Row dataRow = sheet.createRow(2);
            dataRow.createCell(0).setCellValue("1105");
            dataRow.createCell(1).setCellValue("Caja General");
            dataRow.createCell(2).setCellValue("Debito");
            dataRow.createCell(3).setCellValue("Estado de Situacion Financiero");
            dataRow.createCell(4).setCellValue("Activo Corriente");
            
            return toByteArray(workbook);
        }
    }

    private byte[] createExcelFileWithInvalidNature() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Catalogo");
            
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Código");
            headerRow.createCell(1).setCellValue("Nombre");
            headerRow.createCell(2).setCellValue("Naturaleza");
            headerRow.createCell(3).setCellValue("Estado Financiero");
            headerRow.createCell(4).setCellValue("Clasificación");
            
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("1105");
            dataRow.createCell(1).setCellValue("Caja General");
            dataRow.createCell(2).setCellValue("INVALIDO");
            dataRow.createCell(3).setCellValue("Estado de Situacion Financiero");
            dataRow.createCell(4).setCellValue("Activo Corriente");
            
            return toByteArray(workbook);
        }
    }

    private byte[] createExcelFileWithMultipleRows() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Catalogo");
            
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Código");
            headerRow.createCell(1).setCellValue("Nombre");
            headerRow.createCell(2).setCellValue("Naturaleza");
            headerRow.createCell(3).setCellValue("Estado Financiero");
            headerRow.createCell(4).setCellValue("Clasificación");
            
            Row dataRow1 = sheet.createRow(1);
            dataRow1.createCell(0).setCellValue("1");
            dataRow1.createCell(1).setCellValue("Activos");
            dataRow1.createCell(2).setCellValue("Debito");
            dataRow1.createCell(3).setCellValue("Estado de Situacion Financiero");
            dataRow1.createCell(4).setCellValue("Activo Corriente");
            
            Row dataRow2 = sheet.createRow(2);
            dataRow2.createCell(0).setCellValue("2");
            dataRow2.createCell(1).setCellValue("Pasivos");
            dataRow2.createCell(2).setCellValue("Credito");
            dataRow2.createCell(3).setCellValue("Estado de Situacion Financiero");
            dataRow2.createCell(4).setCellValue("Pasivo Corriente");
            
            return toByteArray(workbook);
        }
    }

    private byte[] createExcelFileWithNumericCode() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Catalogo");
            
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Código");
            headerRow.createCell(1).setCellValue("Nombre");
            headerRow.createCell(2).setCellValue("Naturaleza");
            headerRow.createCell(3).setCellValue("Estado Financiero");
            headerRow.createCell(4).setCellValue("Clasificación");
            
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue(1105);
            dataRow.createCell(1).setCellValue("Caja General");
            dataRow.createCell(2).setCellValue("Debito");
            dataRow.createCell(3).setCellValue("Estado de Situacion Financiero");
            dataRow.createCell(4).setCellValue("Activo Corriente");
            
            return toByteArray(workbook);
        }
    }

    private byte[] createExcelFileWithAccentedHeaders() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Catalogo");
            
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Código");
            headerRow.createCell(1).setCellValue("Nombre");
            headerRow.createCell(2).setCellValue("Naturaleza");
            headerRow.createCell(3).setCellValue("Estado Financiero");
            headerRow.createCell(4).setCellValue("Clasificación");
            
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("1105");
            dataRow.createCell(1).setCellValue("Caja General");
            dataRow.createCell(2).setCellValue("Débito");
            dataRow.createCell(3).setCellValue("Estado de Situación Financiero");
            dataRow.createCell(4).setCellValue("Activo Corriente");
            
            return toByteArray(workbook);
        }
    }

    private byte[] toByteArray(Workbook workbook) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream.toByteArray();
    }
}
