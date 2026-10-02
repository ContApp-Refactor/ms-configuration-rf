package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.application.services.validation;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueExcelValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.ExcelValidationException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueExcelValidationServiceUnitTest {

    private AccountCatalogueExcelValidationService validationService;
    private Workbook workbook;
    private XSSFSheet sheet;

    @BeforeEach
    void setUp() {
        validationService = new AccountCatalogueExcelValidationService();
        workbook = new XSSFWorkbook();
        sheet = (XSSFSheet) workbook.createSheet("Test");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (workbook != null) {
            workbook.close();
        }
    }

    @Test
    @DisplayName("Debe retornar opciones de naturaleza correctas")
    void testGetNatureOptionsReturnsCorrectOptions() {
        // Act
        List<String> options = validationService.getNatureOptions();

        // Assert
        assertNotNull(options);
        assertEquals(2, options.size());
        assertTrue(options.contains(NatureEnum.DEBIT.getState()));
        assertTrue(options.contains(NatureEnum.CREDIT.getState()));
    }

    @Test
    @DisplayName("Debe retornar Debito como primera opción de naturaleza")
    void testGetNatureOptionsDebitFirst() {
        // Act
        List<String> options = validationService.getNatureOptions();

        // Assert
        assertEquals(NatureEnum.DEBIT.getState(), options.get(0));
    }

    @Test
    @DisplayName("Debe retornar Credito como segunda opción de naturaleza")
    void testGetNatureOptionsCreditSecond() {
        // Act
        List<String> options = validationService.getNatureOptions();

        // Assert
        assertEquals(NatureEnum.CREDIT.getState(), options.get(1));
    }

    @Test
    @DisplayName("Debe retornar opciones de estado financiero correctas")
    void testGetFinancialStatusOptionsReturnsCorrectOptions() {
        // Act
        List<String> options = validationService.getFinancialStatusOptions();

        // Assert
        assertNotNull(options);
        assertEquals(2, options.size());
        assertTrue(options.contains(FinancialStatusEnum.STATEMENTFINANCIALPOSITION.getState()));
        assertTrue(options.contains(FinancialStatusEnum.INCOMESTATEMENT.getState()));
    }

    @Test
    @DisplayName("Debe retornar Estado de Situación Financiero como primera opción")
    void testGetFinancialStatusOptionsStatementFirst() {
        // Act
        List<String> options = validationService.getFinancialStatusOptions();

        // Assert
        assertEquals(FinancialStatusEnum.STATEMENTFINANCIALPOSITION.getState(), options.get(0));
    }

    @Test
    @DisplayName("Debe retornar Estado de Resultados como segunda opción")
    void testGetFinancialStatusOptionsIncomeSecond() {
        // Act
        List<String> options = validationService.getFinancialStatusOptions();

        // Assert
        assertEquals(FinancialStatusEnum.INCOMESTATEMENT.getState(), options.get(1));
    }

    @Test
    @DisplayName("Debe retornar opciones de clasificación correctas")
    void testGetClassificationOptionsReturnsCorrectOptions() {
        // Act
        List<String> options = validationService.getClassificationOptions();

        // Assert
        assertNotNull(options);
        assertEquals(8, options.size());
    }

    @Test
    @DisplayName("Debe incluir Activos Corrientes en clasificaciones")
    void testGetClassificationOptionsIncludesCurrentAssets() {
        // Act
        List<String> options = validationService.getClassificationOptions();

        // Assert
        assertTrue(options.contains(ClassificationEnum.CURRENTASSETS.getState()));
    }

    @Test
    @DisplayName("Debe incluir Activos No Corrientes en clasificaciones")
    void testGetClassificationOptionsIncludesNonCurrentAssets() {
        // Act
        List<String> options = validationService.getClassificationOptions();

        // Assert
        assertTrue(options.contains(ClassificationEnum.NONCURRENTASSETS.getState()));
    }

    @Test
    @DisplayName("Debe incluir Pasivos Corrientes en clasificaciones")
    void testGetClassificationOptionsIncludesCurrentLiabilities() {
        // Act
        List<String> options = validationService.getClassificationOptions();

        // Assert
        assertTrue(options.contains(ClassificationEnum.CURRENTLIABILITIES.getState()));
    }

    @Test
    @DisplayName("Debe incluir Pasivos No Corrientes en clasificaciones")
    void testGetClassificationOptionsIncludesNonCurrentLiabilities() {
        // Act
        List<String> options = validationService.getClassificationOptions();

        // Assert
        assertTrue(options.contains(ClassificationEnum.NONCURRENTLIABILITIES.getState()));
    }

    @Test
    @DisplayName("Debe incluir Patrimonio en clasificaciones")
    void testGetClassificationOptionsIncludesEquity() {
        // Act
        List<String> options = validationService.getClassificationOptions();

        // Assert
        assertTrue(options.contains(ClassificationEnum.EQUITY.getState()));
    }

    @Test
    @DisplayName("Debe incluir Ingresos Operacionales en clasificaciones")
    void testGetClassificationOptionsIncludesOperatingRevenues() {
        // Act
        List<String> options = validationService.getClassificationOptions();

        // Assert
        assertTrue(options.contains(ClassificationEnum.OPERATINGREVENUES.getState()));
    }

    @Test
    @DisplayName("Debe incluir Ingresos No Operacionales en clasificaciones")
    void testGetClassificationOptionsIncludesNonOperatingIncome() {
        // Act
        List<String> options = validationService.getClassificationOptions();

        // Assert
        assertTrue(options.contains(ClassificationEnum.NONOPERATINGINCOME.getState()));
    }

    @Test
    @DisplayName("Debe incluir Gastos Operacionales en clasificaciones")
    void testGetClassificationOptionsIncludesOperatingExpenses() {
        // Act
        List<String> options = validationService.getClassificationOptions();

        // Assert
        assertTrue(options.contains(ClassificationEnum.OPERATINGEXPENSES.getState()));
    }

    @Test
    @DisplayName("Debe aplicar validaciones de catálogo de cuentas correctamente")
    void testApplyAccountCatalogueValidationsSuccess() throws ExcelValidationException {
        // Act & Assert
        assertDoesNotThrow(() -> validationService.applyAccountCatalogueValidations(sheet, 1, 10));
    }

    @Test
    @DisplayName("Debe aplicar validación de código correctamente")
    void testApplyCodeValidationSuccess() throws ExcelValidationException {
        // Act & Assert
        assertDoesNotThrow(() -> validationService.applyCodeValidation(sheet, 0, 1, 10));
    }

    @Test
    @DisplayName("Debe agregar validación de datos a la hoja para código")
    void testApplyCodeValidationAddsDataValidation() throws ExcelValidationException {
        // Act
        validationService.applyCodeValidation(sheet, 0, 1, 10);

        // Assert
        assertNotNull(sheet.getDataValidations());
        assertFalse(sheet.getDataValidations().isEmpty());
    }

    @Test
    @DisplayName("Debe aplicar validación dropdown correctamente")
    void testApplyDropdownValidationSuccess() throws ExcelValidationException {
        // Arrange
        List<String> options = List.of("Opcion1", "Opcion2");

        // Act & Assert
        assertDoesNotThrow(() -> validationService.applyDropdownValidation(sheet, 1, 1, 10, options, "Error message"));
    }

    @Test
    @DisplayName("Debe agregar validación de datos a la hoja para dropdown")
    void testApplyDropdownValidationAddsDataValidation() throws ExcelValidationException {
        // Arrange
        List<String> options = List.of("Opcion1", "Opcion2");

        // Act
        validationService.applyDropdownValidation(sheet, 1, 1, 10, options, "Error message");

        // Assert
        assertNotNull(sheet.getDataValidations());
        assertFalse(sheet.getDataValidations().isEmpty());
    }

    @Test
    @DisplayName("No debe aplicar dropdown cuando opciones es null")
    void testApplyDropdownValidationWithNullOptions() throws ExcelValidationException {
        // Act
        validationService.applyDropdownValidation(sheet, 1, 1, 10, null, "Error message");

        // Assert
        assertTrue(sheet.getDataValidations().isEmpty());
    }

    @Test
    @DisplayName("No debe aplicar dropdown cuando opciones está vacía")
    void testApplyDropdownValidationWithEmptyOptions() throws ExcelValidationException {
        // Act
        validationService.applyDropdownValidation(sheet, 1, 1, 10, List.of(), "Error message");

        // Assert
        assertTrue(sheet.getDataValidations().isEmpty());
    }

    @Test
    @DisplayName("Debe aplicar validación de cruce correctamente")
    void testApplyCruceValidationSuccess() throws ExcelValidationException {
        // Act & Assert
        assertDoesNotThrow(() -> validationService.applyCruceValidation(sheet, 5, 1, 5));
    }

    @Test
    @DisplayName("Debe crear hoja de validaciones oculta para cruce")
    void testApplyCruceValidationCreatesValidationSheet() throws ExcelValidationException {
        // Act
        validationService.applyCruceValidation(sheet, 5, 1, 5);

        // Assert
        Sheet validationSheet = workbook.getSheet("Validations");
        assertNotNull(validationSheet);
        assertTrue(workbook.isSheetHidden(workbook.getSheetIndex(validationSheet)));
    }

    @Test
    @DisplayName("Debe aplicar validación de centro de costo correctamente")
    void testApplyCentroCostoValidationSuccess() throws ExcelValidationException {
        // Act & Assert
        assertDoesNotThrow(() -> validationService.applyCentroCostoValidation(sheet, 6, 1, 5));
    }

    @Test
    @DisplayName("Debe reutilizar hoja de validaciones existente")
    void testApplyCruceAndCentroCostoReuseValidationSheet() throws ExcelValidationException {
        // Act
        validationService.applyCruceValidation(sheet, 5, 1, 5);
        validationService.applyCentroCostoValidation(sheet, 6, 1, 5);

        // Assert
        int validationSheetCount = 0;
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if ("Validations".equals(workbook.getSheetName(i))) {
                validationSheetCount++;
            }
        }
        assertEquals(1, validationSheetCount);
    }

    @Test
    @DisplayName("Debe crear opciones SI/NO en hoja de validaciones")
    void testApplyCruceValidationCreatesSiNoOptions() throws ExcelValidationException {
        // Act
        validationService.applyCruceValidation(sheet, 5, 1, 5);

        // Assert
        Sheet validationSheet = workbook.getSheet("Validations");
        assertNotNull(validationSheet.getRow(0));
        assertNotNull(validationSheet.getRow(1));
        assertEquals("SI", validationSheet.getRow(0).getCell(0).getStringCellValue());
        assertEquals("NO", validationSheet.getRow(1).getCell(0).getStringCellValue());
    }

    @Test
    @DisplayName("Debe aplicar validaciones completas en rango correcto")
    void testApplyAccountCatalogueValidationsAppliesAllValidations() throws ExcelValidationException {
        // Act
        validationService.applyAccountCatalogueValidations(sheet, 1, 100);

        // Assert
        assertFalse(sheet.getDataValidations().isEmpty());
    }

    @Test
    @DisplayName("Debe aplicar validación dropdown con opciones de naturaleza")
    void testApplyDropdownWithNatureOptions() throws ExcelValidationException {
        // Arrange
        List<String> natureOptions = validationService.getNatureOptions();

        // Act
        validationService.applyDropdownValidation(sheet, 2, 1, 10, natureOptions, "Seleccione naturaleza");

        // Assert
        assertFalse(sheet.getDataValidations().isEmpty());
    }

    @Test
    @DisplayName("Debe aplicar validación dropdown con opciones de estado financiero")
    void testApplyDropdownWithFinancialStatusOptions() throws ExcelValidationException {
        // Arrange
        List<String> financialOptions = validationService.getFinancialStatusOptions();

        // Act
        validationService.applyDropdownValidation(sheet, 3, 1, 10, financialOptions, "Seleccione estado");

        // Assert
        assertFalse(sheet.getDataValidations().isEmpty());
    }

    @Test
    @DisplayName("Debe aplicar validación dropdown con opciones de clasificación")
    void testApplyDropdownWithClassificationOptions() throws ExcelValidationException {
        // Arrange
        List<String> classificationOptions = validationService.getClassificationOptions();

        // Act
        validationService.applyDropdownValidation(sheet, 4, 1, 10, classificationOptions, "Seleccione clasificación");

        // Assert
        assertFalse(sheet.getDataValidations().isEmpty());
    }

    @Test
    @DisplayName("Debe aplicar validación de código en columna 0")
    void testApplyAccountCatalogueValidationsCodeInColumn0() throws ExcelValidationException {
        // Act
        validationService.applyAccountCatalogueValidations(sheet, 1, 10);

        // Assert
        boolean hasValidationInColumn0 = sheet.getDataValidations().stream()
                .anyMatch(v -> v.getRegions().getCellRangeAddresses()[0].getFirstColumn() == 0);
        assertTrue(hasValidationInColumn0);
    }

    @Test
    @DisplayName("Debe manejar rango de una sola fila")
    void testApplyValidationsWithSingleRow() throws ExcelValidationException {
        // Act & Assert
        assertDoesNotThrow(() -> validationService.applyAccountCatalogueValidations(sheet, 1, 1));
    }

    @Test
    @DisplayName("Debe manejar rango grande de filas")
    void testApplyValidationsWithLargeRange() throws ExcelValidationException {
        // Act & Assert
        assertDoesNotThrow(() -> validationService.applyAccountCatalogueValidations(sheet, 1, 1000));
    }

    @Test
    @DisplayName("Debe crear rango nombrado ValidationOptions")
    void testApplyCruceValidationCreatesNamedRange() throws ExcelValidationException {
        // Act
        validationService.applyCruceValidation(sheet, 5, 1, 5);

        // Assert
        assertNotNull(workbook.getName("ValidationOptions"));
    }

    @Test
    @DisplayName("Opciones de naturaleza deben ser inmutables")
    void testGetNatureOptionsIsImmutable() {
        // Act
        List<String> options = validationService.getNatureOptions();

        // Assert
        assertThrows(UnsupportedOperationException.class, () -> options.add("Nueva opción"));
    }

    @Test
    @DisplayName("Opciones de estado financiero deben ser inmutables")
    void testGetFinancialStatusOptionsIsImmutable() {
        // Act
        List<String> options = validationService.getFinancialStatusOptions();

        // Assert
        assertThrows(UnsupportedOperationException.class, () -> options.add("Nueva opción"));
    }

    @Test
    @DisplayName("Opciones de clasificación deben ser inmutables")
    void testGetClassificationOptionsIsImmutable() {
        // Act
        List<String> options = validationService.getClassificationOptions();

        // Assert
        assertThrows(UnsupportedOperationException.class, () -> options.add("Nueva opción"));
    }
}
