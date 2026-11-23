package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.application.services.validation;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueBatchValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueBatchValidationService.BatchValidationResult;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogueExcelData;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.utils.ImportConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueBatchValidationServiceUnitTest {

    @Mock
    private AccountCatalogueValidationService validationService;

    @InjectMocks
    private AccountCatalogueBatchValidationService batchValidationService;

    private Map<String, Integer> columnMap;
    private String entId;

    @BeforeEach
    void setUp() {
        entId = "ENT-001";
        columnMap = new HashMap<>();
        columnMap.put(ImportConstants.CODE_COLUMN, 0);
        columnMap.put(ImportConstants.NAME_COLUMN, 1);
        columnMap.put(ImportConstants.NATURE_COLUMN, 2);
        columnMap.put(ImportConstants.FINANCIAL_STATUS_COLUMN, 3);
        columnMap.put(ImportConstants.CLASSIFICATION_COLUMN, 4);
        columnMap.put(ImportConstants.CROSSING_COLUMN, 5);
        columnMap.put(ImportConstants.COST_CENTER_COLUMN, 6);

        doNothing().when(validationService).validateAccountDescription(anyString());
    }

    private AccountCatalogueExcelData createValidExcelData(int rowNumber, String code) {
        return AccountCatalogueExcelData.builder()
                .rowNumber(rowNumber)
                .code(code)
                .description("Cuenta " + code)
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .classification(ClassificationEnum.OPERATINGEXPENSES)
                .crossing(false)
                .costCenter(false)
                .build();
    }

    private AccountCatalogueExcelData createValidAuxiliaryAccount(int rowNumber) {
        return AccountCatalogueExcelData.builder()
                .rowNumber(rowNumber)
                .code("11050101")
                .description("Cuenta auxiliar")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .classification(ClassificationEnum.OPERATINGEXPENSES)
                .crossing(false)
                .costCenter(false)
                .build();
    }

    @Test
    @DisplayName("Debe validar lote con todos los registros válidos")
    void testValidateBatchAllRecordsValid() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        accountsData.add(createValidExcelData(1, "1"));
        accountsData.add(createValidExcelData(2, "11"));
        accountsData.add(createValidExcelData(3, "1105"));

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(3, result.getTotalProcessed());
        assertEquals(3, result.getValidCount());
        assertEquals(0, result.getErrorCount());
        assertEquals(3, result.getValidRecords().size());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Debe retornar lista vacía para lote vacío")
    void testValidateBatchEmptyList() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(0, result.getTotalProcessed());
        assertEquals(0, result.getValidCount());
        assertEquals(0, result.getErrorCount());
    }

    @Test
    @DisplayName("Debe detectar error cuando código es null")
    void testValidateBatchCodeNull() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidExcelData(1, "1");
        data.setCode(null);
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(1, result.getTotalProcessed());
        assertEquals(0, result.getValidCount());
        assertTrue(result.getErrorCount() > 0);
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getErrorCode().equals(ImportConstants.ErrorCodes.REQUIRED_FIELD_MISSING)));
    }

    @Test
    @DisplayName("Debe detectar error cuando código está vacío")
    void testValidateBatchCodeEmpty() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidExcelData(1, "");
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(0, result.getValidCount());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getColumnName().equals(ImportConstants.CODE_COLUMN)));
    }

    @Test
    @DisplayName("Debe detectar error cuando descripción es null")
    void testValidateBatchDescriptionNull() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidExcelData(1, "1");
        data.setDescription(null);
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(0, result.getValidCount());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getColumnName().equals(ImportConstants.NAME_COLUMN)));
    }

    @Test
    @DisplayName("Debe detectar error cuando descripción está vacía")
    void testValidateBatchDescriptionEmpty() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidExcelData(1, "1");
        data.setDescription("   ");
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(0, result.getValidCount());
    }

    @Test
    @DisplayName("Debe detectar error cuando naturaleza es null")
    void testValidateBatchNatureNull() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidExcelData(1, "1");
        data.setNature(null);
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(0, result.getValidCount());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getColumnName().equals(ImportConstants.NATURE_COLUMN)));
    }

    @Test
    @DisplayName("Debe detectar error cuando estado financiero es null")
    void testValidateBatchFinancialStatusNull() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidExcelData(1, "1");
        data.setFinancialStatus(null);
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(0, result.getValidCount());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getColumnName().equals(ImportConstants.FINANCIAL_STATUS_COLUMN)));
    }

    @Test
    @DisplayName("Debe detectar error cuando clasificación es null")
    void testValidateBatchClassificationNull() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidExcelData(1, "1");
        data.setClassification(null);
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(0, result.getValidCount());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getColumnName().equals(ImportConstants.CLASSIFICATION_COLUMN)));
    }

    @Test
    @DisplayName("Debe detectar error cuando código contiene letras")
    void testValidateBatchCodeWithLetters() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidExcelData(1, "ABC123");
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(0, result.getValidCount());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getErrorCode().equals(ImportConstants.ErrorCodes.INVALID_CODE_FORMAT)));
    }

    @Test
    @DisplayName("Debe detectar error cuando código tiene longitud inválida")
    void testValidateBatchCodeInvalidLength() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidExcelData(1, "123");
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(0, result.getValidCount());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getErrorCode().equals(ImportConstants.ErrorCodes.INVALID_CODE_LENGTH)));
    }

    @Test
    @DisplayName("Debe detectar error cuando código es negativo")
    void testValidateBatchCodeNegative() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidExcelData(1, "-1");
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(0, result.getValidCount());
    }

    @Test
    @DisplayName("Debe detectar error cuando código es cero")
    void testValidateBatchCodeZero() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidExcelData(1, "0");
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(0, result.getValidCount());
    }

    @Test
    @DisplayName("Debe validar código de 1 dígito correctamente")
    void testValidateBatchCode1Digit() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        accountsData.add(createValidExcelData(1, "1"));

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(1, result.getValidCount());
    }

    @Test
    @DisplayName("Debe validar código de 2 dígitos correctamente")
    void testValidateBatchCode2Digits() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        accountsData.add(createValidExcelData(1, "11"));

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(1, result.getValidCount());
    }

    @Test
    @DisplayName("Debe validar código de 4 dígitos correctamente")
    void testValidateBatchCode4Digits() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        accountsData.add(createValidExcelData(1, "1105"));

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(1, result.getValidCount());
    }

    @Test
    @DisplayName("Debe validar código de 6 dígitos correctamente")
    void testValidateBatchCode6Digits() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        accountsData.add(createValidExcelData(1, "110501"));

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(1, result.getValidCount());
    }

    @Test
    @DisplayName("Debe validar código de 8 dígitos correctamente")
    void testValidateBatchCode8Digits() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        accountsData.add(createValidAuxiliaryAccount(1));

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(1, result.getValidCount());
    }

    @Test
    @DisplayName("Debe detectar error de descripción inválida del servicio de validación")
    void testValidateBatchDescriptionValidationError() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        accountsData.add(createValidExcelData(1, "1"));
        doThrow(new IllegalArgumentException("Descripción inválida"))
                .when(validationService).validateAccountDescription(anyString());

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(0, result.getValidCount());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getErrorCode().equals(ImportConstants.ErrorCodes.BUSINESS_RULE_VIOLATION)));
    }

    @Test
    @DisplayName("Debe detectar error cuando crossing es true en cuenta no auxiliar")
    void testValidateBatchCrossingTrueNonAuxiliary() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidExcelData(1, "1105");
        data.setCrossing(true);
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(0, result.getValidCount());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getColumnName().equals(ImportConstants.CROSSING_COLUMN)));
    }

    @Test
    @DisplayName("Debe permitir crossing true en cuenta auxiliar")
    void testValidateBatchCrossingTrueAuxiliary() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidAuxiliaryAccount(1);
        data.setCrossing(true);
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(1, result.getValidCount());
    }

    @Test
    @DisplayName("Debe permitir crossing false en cualquier cuenta")
    void testValidateBatchCrossingFalseAnyAccount() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidExcelData(1, "11");
        data.setCrossing(false);
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(1, result.getValidCount());
    }

    @Test
    @DisplayName("Debe permitir crossing null en cualquier cuenta")
    void testValidateBatchCrossingNullAnyAccount() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidExcelData(1, "11");
        data.setCrossing(null);
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(1, result.getValidCount());
    }

    @Test
    @DisplayName("Debe detectar error cuando costCenter es true en cuenta no auxiliar")
    void testValidateBatchCostCenterTrueNonAuxiliary() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidExcelData(1, "1105");
        data.setCostCenter(true);
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(0, result.getValidCount());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getColumnName().equals(ImportConstants.COST_CENTER_COLUMN)));
    }

    @Test
    @DisplayName("Debe detectar error cuando costCenter es true y estado financiero no es Estado de Resultados")
    void testValidateBatchCostCenterTrueWrongFinancialStatus() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidAuxiliaryAccount(1);
        data.setCostCenter(true);
        data.setFinancialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION);
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(0, result.getValidCount());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getColumnName().equals(ImportConstants.COST_CENTER_COLUMN)));
    }

    @Test
    @DisplayName("Debe permitir costCenter true en cuenta auxiliar con Estado de Resultados")
    void testValidateBatchCostCenterTrueAuxiliaryIncomeStatement() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidAuxiliaryAccount(1);
        data.setCostCenter(true);
        data.setFinancialStatus(FinancialStatusEnum.INCOMESTATEMENT);
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(1, result.getValidCount());
    }

    @Test
    @DisplayName("Debe permitir costCenter false en cualquier cuenta")
    void testValidateBatchCostCenterFalseAnyAccount() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidExcelData(1, "11");
        data.setCostCenter(false);
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(1, result.getValidCount());
    }

    @Test
    @DisplayName("Debe permitir costCenter null en cualquier cuenta")
    void testValidateBatchCostCenterNullAnyAccount() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidExcelData(1, "11");
        data.setCostCenter(null);
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(1, result.getValidCount());
    }

    @Test
    @DisplayName("Debe acumular múltiples errores del mismo registro")
    void testValidateBatchMultipleErrorsSameRecord() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = AccountCatalogueExcelData.builder()
                .rowNumber(1)
                .code(null)
                .description(null)
                .nature(null)
                .financialStatus(null)
                .classification(null)
                .build();
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(0, result.getValidCount());
        assertTrue(result.getErrorCount() >= 5);
    }

    @Test
    @DisplayName("Debe separar registros válidos de inválidos")
    void testValidateBatchSeparatesValidFromInvalid() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        accountsData.add(createValidExcelData(1, "1"));
        AccountCatalogueExcelData invalidData = createValidExcelData(2, "ABC");
        accountsData.add(invalidData);
        accountsData.add(createValidExcelData(3, "11"));

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(3, result.getTotalProcessed());
        assertEquals(2, result.getValidCount());
        assertEquals(2, result.getValidRecords().size());
        assertTrue(result.getErrorCount() > 0);
    }

    @Test
    @DisplayName("Debe manejar excepción del sistema durante validación")
    void testValidateBatchHandlesSystemException() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = AccountCatalogueExcelData.builder()
                .rowNumber(1)
                .code("1")
                .description("Test")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .classification(null)
                .build();
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(0, result.getValidCount());
        assertTrue(result.getErrorCount() > 0);
    }

    @Test
    @DisplayName("Debe incluir número de columna correcto en errores")
    void testValidateBatchIncludesColumnNumber() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidExcelData(1, "1");
        data.setCode(null);
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getColumnNumber() != null && e.getColumnNumber() == 1));
    }

    @Test
    @DisplayName("Debe incluir número de fila correcto en errores")
    void testValidateBatchIncludesRowNumber() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidExcelData(5, "ABC");
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getRowNumber() == 5));
    }

    @Test
    @DisplayName("Debe manejar columnMap null")
    void testValidateBatchWithNullColumnMap() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        AccountCatalogueExcelData data = createValidExcelData(1, "1");
        data.setCode(null);
        accountsData.add(data);

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, null);

        // Assert
        assertEquals(0, result.getValidCount());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getColumnNumber() == null));
    }

    @Test
    @DisplayName("Debe procesar lote grande correctamente")
    void testValidateBatchLargeBatch() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            accountsData.add(createValidExcelData(i, String.valueOf(i)));
        }

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(100, result.getTotalProcessed());
        assertEquals(99, result.getValidCount());
    }

    @Test
    @DisplayName("Debe incluir fieldValue en error de código inválido")
    void testValidateBatchIncludesFieldValueInError() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        accountsData.add(createValidExcelData(1, "ABC123"));

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> "ABC123".equals(e.getFieldValue())));
    }

    @Test
    @DisplayName("Debe detectar error cuando código tiene 5 dígitos")
    void testValidateBatchCode5Digits() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        accountsData.add(createValidExcelData(1, "12345"));

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(0, result.getValidCount());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getErrorCode().equals(ImportConstants.ErrorCodes.INVALID_CODE_LENGTH)));
    }

    @Test
    @DisplayName("Debe detectar error cuando código tiene 7 dígitos")
    void testValidateBatchCode7Digits() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        accountsData.add(createValidExcelData(1, "1234567"));

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(0, result.getValidCount());
    }

    @Test
    @DisplayName("Debe detectar error cuando código tiene más de 8 dígitos")
    void testValidateBatchCodeMoreThan8Digits() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = new ArrayList<>();
        accountsData.add(createValidExcelData(1, "123456789"));

        // Act
        BatchValidationResult result = batchValidationService.validateBatch(accountsData, entId, columnMap);

        // Assert
        assertEquals(0, result.getValidCount());
    }
}
