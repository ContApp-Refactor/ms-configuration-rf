package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.application.services.importExport;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.AccountCatalogueCreateService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.AccountCatalogueDataConverter;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.AccountCatalogueHierarchyProcessor;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport.AccountCatalogueBatchProcessor;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ImportErrorType;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogueExcelData;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueBatchProcessorUnitTest {

    @Mock
    private AccountCatalogueCreateService createService;

    @Mock
    private AccountCatalogueDataConverter dataConverter;

    @Mock
    private AccountCatalogueHierarchyProcessor hierarchyProcessor;

    @InjectMocks
    private AccountCatalogueBatchProcessor batchProcessor;

    private String entId;
    private AccountCatalogueExcelData excelDataLevel1;
    private AccountCatalogueExcelData excelDataLevel2;
    private AccountCatalogueExcelData excelDataLevel4;
    private AccountCatalogue accountLevel1;
    private AccountCatalogue accountLevel2;
    private AccountCatalogue accountLevel4;

    @BeforeEach
    void setUp() {
        entId = "ENT-001";

        excelDataLevel1 = AccountCatalogueExcelData.builder()
                .rowNumber(2)
                .idEnterprise(entId)
                .code("1")
                .description("Activos")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .crossing(false)
                .costCenter(false)
                .build();

        excelDataLevel2 = AccountCatalogueExcelData.builder()
                .rowNumber(3)
                .idEnterprise(entId)
                .code("11")
                .description("Activos Corrientes")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .crossing(false)
                .costCenter(false)
                .build();

        excelDataLevel4 = AccountCatalogueExcelData.builder()
                .rowNumber(4)
                .idEnterprise(entId)
                .code("1105")
                .description("Caja")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .crossing(false)
                .costCenter(false)
                .build();

        accountLevel1 = AccountCatalogue.builder()
                .id(1L)
                .code("1")
                .description("Activos")
                .idEnterprise(entId)
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .status(true)
                .build();

        accountLevel2 = AccountCatalogue.builder()
                .id(2L)
                .code("11")
                .description("Activos Corrientes")
                .idEnterprise(entId)
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .parent(accountLevel1)
                .status(true)
                .build();

        accountLevel4 = AccountCatalogue.builder()
                .id(3L)
                .code("1105")
                .description("Caja")
                .idEnterprise(entId)
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .parent(accountLevel2)
                .status(true)
                .build();
    }

    @Test
    @DisplayName("Debe procesar lote exitosamente con una cuenta")
    void testProcessBatchSuccessWithSingleAccount() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelDataLevel1);
        
        when(hierarchyProcessor.buildParentMapFromDatabase(anySet(), eq(entId)))
                .thenReturn(new HashMap<>());
        when(dataConverter.convertToAccountCatalogue(eq(excelDataLevel1), anyMap(), anyMap()))
                .thenReturn(accountLevel1);
        when(createService.createAllAccountCatalogues(anyList()))
                .thenReturn(List.of(accountLevel1));

        // Act
        AccountCatalogueBatchProcessor.BatchProcessingResult result = batchProcessor.processBatch(accountsData, entId);

        // Assert
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertEquals(0, result.getSkippedCount());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Debe procesar múltiples cuentas en orden jerárquico")
    void testProcessBatchSuccessWithHierarchy() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelDataLevel1, excelDataLevel2, excelDataLevel4);
        
        when(hierarchyProcessor.buildParentMapFromDatabase(anySet(), eq(entId)))
                .thenReturn(new HashMap<>());
        when(dataConverter.convertToAccountCatalogue(eq(excelDataLevel1), anyMap(), anyMap()))
                .thenReturn(accountLevel1);
        when(dataConverter.convertToAccountCatalogue(eq(excelDataLevel2), anyMap(), anyMap()))
                .thenReturn(accountLevel2);
        when(dataConverter.convertToAccountCatalogue(eq(excelDataLevel4), anyMap(), anyMap()))
                .thenReturn(accountLevel4);
        when(createService.createAllAccountCatalogues(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AccountCatalogueBatchProcessor.BatchProcessingResult result = batchProcessor.processBatch(accountsData, entId);

        // Assert
        assertEquals(3, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertTrue(result.getErrors().isEmpty());
        verify(createService, times(3)).createAllAccountCatalogues(anyList());
    }

    @Test
    @DisplayName("Debe manejar error en conversión de cuenta individual")
    void testProcessBatchHandlesConversionError() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelDataLevel1);
        
        when(hierarchyProcessor.buildParentMapFromDatabase(anySet(), eq(entId)))
                .thenReturn(new HashMap<>());
        when(dataConverter.convertToAccountCatalogue(eq(excelDataLevel1), anyMap(), anyMap()))
                .thenThrow(new RuntimeException("Error de conversión"));

        // Act
        AccountCatalogueBatchProcessor.BatchProcessingResult result = batchProcessor.processBatch(accountsData, entId);

        // Assert
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertFalse(result.getErrors().isEmpty());
        assertEquals(ImportErrorType.SYSTEM_ERROR, result.getErrors().get(0).getErrorType());
    }

    @Test
    @DisplayName("Debe manejar lista vacía de cuentas")
    void testProcessBatchWithEmptyList() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = Collections.emptyList();

        // Act
        AccountCatalogueBatchProcessor.BatchProcessingResult result = batchProcessor.processBatch(accountsData, entId);

        // Assert
        assertEquals(0, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertEquals(0, result.getSkippedCount());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Debe construir mapa de padres desde base de datos")
    void testProcessBatchBuildsParentMapFromDatabase() {
        // Arrange
        AccountCatalogueEntity parentEntity = new AccountCatalogueEntity();
        parentEntity.setId(1L);
        parentEntity.setCode("1");
        Map<String, AccountCatalogueEntity> parentsFromDb = Map.of("1", parentEntity);
        
        List<AccountCatalogueExcelData> accountsData = List.of(excelDataLevel2);
        
        when(hierarchyProcessor.buildParentMapFromDatabase(anySet(), eq(entId)))
                .thenReturn(parentsFromDb);
        when(dataConverter.convertToAccountCatalogue(eq(excelDataLevel2), anyMap(), anyMap()))
                .thenReturn(accountLevel2);
        when(createService.createAllAccountCatalogues(anyList()))
                .thenReturn(List.of(accountLevel2));

        // Act
        batchProcessor.processBatch(accountsData, entId);

        // Assert
        verify(hierarchyProcessor).buildParentMapFromDatabase(anySet(), eq(entId));
    }

    @Test
    @DisplayName("Debe acumular cuentas procesadas en mapa para lotes posteriores")
    void testProcessBatchAccumulatesProcessedAccounts() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelDataLevel1, excelDataLevel2);
        
        when(hierarchyProcessor.buildParentMapFromDatabase(anySet(), eq(entId)))
                .thenReturn(new HashMap<>());
        when(dataConverter.convertToAccountCatalogue(eq(excelDataLevel1), anyMap(), anyMap()))
                .thenReturn(accountLevel1);
        when(dataConverter.convertToAccountCatalogue(eq(excelDataLevel2), anyMap(), anyMap()))
                .thenReturn(accountLevel2);
        when(createService.createAllAccountCatalogues(anyList()))
                .thenAnswer(invocation -> {
                    List<AccountCatalogue> accounts = invocation.getArgument(0);
                    return accounts;
                });

        // Act
        AccountCatalogueBatchProcessor.BatchProcessingResult result = batchProcessor.processBatch(accountsData, entId);

        // Assert
        assertEquals(2, result.getSuccessCount());
        verify(createService, times(2)).createAllAccountCatalogues(anyList());
    }

    @Test
    @DisplayName("Debe agregar error con código SYSTEM_ERROR cuando falla conversión")
    void testProcessBatchErrorCodeOnConversionFailure() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelDataLevel1);
        
        when(hierarchyProcessor.buildParentMapFromDatabase(anySet(), eq(entId)))
                .thenReturn(new HashMap<>());
        when(dataConverter.convertToAccountCatalogue(any(), anyMap(), anyMap()))
                .thenThrow(new RuntimeException("Error"));

        // Act
        AccountCatalogueBatchProcessor.BatchProcessingResult result = batchProcessor.processBatch(accountsData, entId);

        // Assert
        assertFalse(result.getErrors().isEmpty());
        assertEquals("SYSTEM_ERROR", result.getErrors().get(0).getErrorCode());
        assertEquals(excelDataLevel1.getRowNumber(), result.getErrors().get(0).getRowNumber());
    }

    @Test
    @DisplayName("Debe calcular totalProcessed como suma de éxitos y fallos")
    void testProcessBatchCalculatesTotalProcessed() {
        // Arrange
        AccountCatalogueExcelData excelData2 = AccountCatalogueExcelData.builder()
                .rowNumber(5)
                .idEnterprise(entId)
                .code("2")
                .description("Pasivos")
                .nature(NatureEnum.CREDIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTLIABILITIES)
                .build();
        
        List<AccountCatalogueExcelData> accountsData = List.of(excelDataLevel1, excelData2);
        
        when(hierarchyProcessor.buildParentMapFromDatabase(anySet(), eq(entId)))
                .thenReturn(new HashMap<>());
        when(dataConverter.convertToAccountCatalogue(eq(excelDataLevel1), anyMap(), anyMap()))
                .thenReturn(accountLevel1);
        when(dataConverter.convertToAccountCatalogue(eq(excelData2), anyMap(), anyMap()))
                .thenThrow(new RuntimeException("Error"));
        when(createService.createAllAccountCatalogues(anyList()))
                .thenReturn(List.of(accountLevel1));

        // Act
        AccountCatalogueBatchProcessor.BatchProcessingResult result = batchProcessor.processBatch(accountsData, entId);

        // Assert
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertEquals(2, result.getTotalProcessed());
    }

    @Test
    @DisplayName("Debe procesar cuentas del mismo nivel en batch")
    void testProcessBatchProcessesSameLevelTogether() {
        // Arrange
        AccountCatalogueExcelData excelData2 = AccountCatalogueExcelData.builder()
                .rowNumber(5)
                .idEnterprise(entId)
                .code("2")
                .description("Pasivos")
                .nature(NatureEnum.CREDIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTLIABILITIES)
                .build();
        AccountCatalogue account2 = AccountCatalogue.builder()
                .id(10L)
                .code("2")
                .description("Pasivos")
                .idEnterprise(entId)
                .nature(NatureEnum.CREDIT)
                .status(true)
                .build();
        
        List<AccountCatalogueExcelData> accountsData = List.of(excelDataLevel1, excelData2);
        
        when(hierarchyProcessor.buildParentMapFromDatabase(anySet(), eq(entId)))
                .thenReturn(new HashMap<>());
        when(dataConverter.convertToAccountCatalogue(eq(excelDataLevel1), anyMap(), anyMap()))
                .thenReturn(accountLevel1);
        when(dataConverter.convertToAccountCatalogue(eq(excelData2), anyMap(), anyMap()))
                .thenReturn(account2);
        when(createService.createAllAccountCatalogues(argThat(list -> list.size() == 2)))
                .thenReturn(List.of(accountLevel1, account2));

        // Act
        AccountCatalogueBatchProcessor.BatchProcessingResult result = batchProcessor.processBatch(accountsData, entId);

        // Assert
        assertEquals(2, result.getSuccessCount());
        verify(createService).createAllAccountCatalogues(argThat(list -> list.size() == 2));
    }

    @Test
    @DisplayName("Debe incluir campo de valor en error cuando falla conversión")
    void testProcessBatchIncludesFieldValueInError() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelDataLevel1);
        
        when(hierarchyProcessor.buildParentMapFromDatabase(anySet(), eq(entId)))
                .thenReturn(new HashMap<>());
        when(dataConverter.convertToAccountCatalogue(any(), anyMap(), anyMap()))
                .thenThrow(new RuntimeException("Error de conversión"));

        // Act
        AccountCatalogueBatchProcessor.BatchProcessingResult result = batchProcessor.processBatch(accountsData, entId);

        // Assert
        assertFalse(result.getErrors().isEmpty());
        assertEquals(excelDataLevel1.getCode(), result.getErrors().get(0).getFieldValue());
    }

    @Test
    @DisplayName("Debe manejar excepción en createAllAccountCatalogues")
    void testProcessBatchHandlesCreateServiceException() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelDataLevel1);
        
        when(hierarchyProcessor.buildParentMapFromDatabase(anySet(), eq(entId)))
                .thenReturn(new HashMap<>());
        when(dataConverter.convertToAccountCatalogue(eq(excelDataLevel1), anyMap(), anyMap()))
                .thenReturn(accountLevel1);
        when(createService.createAllAccountCatalogues(anyList()))
                .thenThrow(new RuntimeException("Error en BD"));

        // Act
        AccountCatalogueBatchProcessor.BatchProcessingResult result = batchProcessor.processBatch(accountsData, entId);

        // Assert
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Debe retornar skippedCount cero cuando no hay omitidos")
    void testProcessBatchReturnsZeroSkippedCount() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelDataLevel1);
        
        when(hierarchyProcessor.buildParentMapFromDatabase(anySet(), eq(entId)))
                .thenReturn(new HashMap<>());
        when(dataConverter.convertToAccountCatalogue(eq(excelDataLevel1), anyMap(), anyMap()))
                .thenReturn(accountLevel1);
        when(createService.createAllAccountCatalogues(anyList()))
                .thenReturn(List.of(accountLevel1));

        // Act
        AccountCatalogueBatchProcessor.BatchProcessingResult result = batchProcessor.processBatch(accountsData, entId);

        // Assert
        assertEquals(0, result.getSkippedCount());
    }

    @Test
    @DisplayName("Debe manejar múltiples niveles jerárquicos correctamente")
    void testProcessBatchHandlesMultipleLevels() {
        // Arrange
        AccountCatalogueExcelData excelDataLevel6 = AccountCatalogueExcelData.builder()
                .rowNumber(5)
                .idEnterprise(entId)
                .code("110501")
                .description("Caja General")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .build();
        AccountCatalogue accountLevel6 = AccountCatalogue.builder()
                .id(4L)
                .code("110501")
                .description("Caja General")
                .idEnterprise(entId)
                .nature(NatureEnum.DEBIT)
                .parent(accountLevel4)
                .status(true)
                .build();
        
        List<AccountCatalogueExcelData> accountsData = List.of(
                excelDataLevel1, excelDataLevel2, excelDataLevel4, excelDataLevel6);
        
        when(hierarchyProcessor.buildParentMapFromDatabase(anySet(), eq(entId)))
                .thenReturn(new HashMap<>());
        when(dataConverter.convertToAccountCatalogue(eq(excelDataLevel1), anyMap(), anyMap()))
                .thenReturn(accountLevel1);
        when(dataConverter.convertToAccountCatalogue(eq(excelDataLevel2), anyMap(), anyMap()))
                .thenReturn(accountLevel2);
        when(dataConverter.convertToAccountCatalogue(eq(excelDataLevel4), anyMap(), anyMap()))
                .thenReturn(accountLevel4);
        when(dataConverter.convertToAccountCatalogue(eq(excelDataLevel6), anyMap(), anyMap()))
                .thenReturn(accountLevel6);
        when(createService.createAllAccountCatalogues(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AccountCatalogueBatchProcessor.BatchProcessingResult result = batchProcessor.processBatch(accountsData, entId);

        // Assert
        assertEquals(4, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        verify(createService, times(4)).createAllAccountCatalogues(anyList());
    }

    @Test
    @DisplayName("Debe excluir códigos ya procesados de consulta a BD")
    void testProcessBatchExcludesAlreadyProcessedFromDbQuery() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelDataLevel1, excelDataLevel2);
        
        when(hierarchyProcessor.buildParentMapFromDatabase(anySet(), eq(entId)))
                .thenReturn(new HashMap<>());
        when(dataConverter.convertToAccountCatalogue(any(), anyMap(), anyMap()))
                .thenAnswer(invocation -> {
                    AccountCatalogueExcelData data = invocation.getArgument(0);
                    if (data.getCode().equals("1")) return accountLevel1;
                    return accountLevel2;
                });
        when(createService.createAllAccountCatalogues(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        batchProcessor.processBatch(accountsData, entId);

        // Assert
        verify(hierarchyProcessor).buildParentMapFromDatabase(anySet(), eq(entId));
    }

    @Test
    @DisplayName("Debe continuar procesando después de error individual cuando CONTINUE_ON_ERROR es true")
    void testProcessBatchContinuesOnIndividualError() {
        // Arrange
        AccountCatalogueExcelData excelData2 = AccountCatalogueExcelData.builder()
                .rowNumber(5)
                .idEnterprise(entId)
                .code("2")
                .description("Pasivos")
                .nature(NatureEnum.CREDIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTLIABILITIES)
                .build();
        AccountCatalogue account2 = AccountCatalogue.builder()
                .id(10L)
                .code("2")
                .description("Pasivos")
                .idEnterprise(entId)
                .status(true)
                .build();
        
        List<AccountCatalogueExcelData> accountsData = List.of(excelDataLevel1, excelData2);
        
        when(hierarchyProcessor.buildParentMapFromDatabase(anySet(), eq(entId)))
                .thenReturn(new HashMap<>());
        when(dataConverter.convertToAccountCatalogue(eq(excelDataLevel1), anyMap(), anyMap()))
                .thenThrow(new RuntimeException("Error en primera cuenta"));
        when(dataConverter.convertToAccountCatalogue(eq(excelData2), anyMap(), anyMap()))
                .thenReturn(account2);
        when(createService.createAllAccountCatalogues(anyList()))
                .thenReturn(List.of(account2));

        // Act
        AccountCatalogueBatchProcessor.BatchProcessingResult result = batchProcessor.processBatch(accountsData, entId);

        // Assert
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
    }

    @Test
    @DisplayName("Debe incluir mensaje de error descriptivo en detalle de error")
    void testProcessBatchIncludesDescriptiveErrorMessage() {
        // Arrange
        String errorMessage = "Padre no encontrado para código 11";
        List<AccountCatalogueExcelData> accountsData = List.of(excelDataLevel2);
        
        when(hierarchyProcessor.buildParentMapFromDatabase(anySet(), eq(entId)))
                .thenReturn(new HashMap<>());
        when(dataConverter.convertToAccountCatalogue(any(), anyMap(), anyMap()))
                .thenThrow(new RuntimeException(errorMessage));

        // Act
        AccountCatalogueBatchProcessor.BatchProcessingResult result = batchProcessor.processBatch(accountsData, entId);

        // Assert
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().get(0).getErrorMessage().contains(errorMessage));
    }

    @Test
    @DisplayName("Debe manejar cuentas con nivel 8 (auxiliares)")
    void testProcessBatchHandlesLevel8Accounts() {
        // Arrange
        AccountCatalogueExcelData excelDataLevel8 = AccountCatalogueExcelData.builder()
                .rowNumber(6)
                .idEnterprise(entId)
                .code("11050101")
                .description("Caja General Bogotá")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .crossing(true)
                .costCenter(false)
                .build();
        AccountCatalogue accountLevel8 = AccountCatalogue.builder()
                .id(5L)
                .code("11050101")
                .description("Caja General Bogotá")
                .idEnterprise(entId)
                .nature(NatureEnum.DEBIT)
                .crossing(true)
                .costCenter(false)
                .status(true)
                .build();
        
        List<AccountCatalogueExcelData> accountsData = List.of(excelDataLevel8);
        
        when(hierarchyProcessor.buildParentMapFromDatabase(anySet(), eq(entId)))
                .thenReturn(new HashMap<>());
        when(dataConverter.convertToAccountCatalogue(eq(excelDataLevel8), anyMap(), anyMap()))
                .thenReturn(accountLevel8);
        when(createService.createAllAccountCatalogues(anyList()))
                .thenReturn(List.of(accountLevel8));

        // Act
        AccountCatalogueBatchProcessor.BatchProcessingResult result = batchProcessor.processBatch(accountsData, entId);

        // Assert
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
    }

    @Test
    @DisplayName("Debe pasar mapa de procesados a convertidor de datos")
    void testProcessBatchPassesProcessedMapToConverter() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelDataLevel1, excelDataLevel2);
        
        when(hierarchyProcessor.buildParentMapFromDatabase(anySet(), eq(entId)))
                .thenReturn(new HashMap<>());
        when(dataConverter.convertToAccountCatalogue(any(), anyMap(), anyMap()))
                .thenAnswer(invocation -> {
                    AccountCatalogueExcelData data = invocation.getArgument(0);
                    if (data.getCode().equals("1")) return accountLevel1;
                    return accountLevel2;
                });
        when(createService.createAllAccountCatalogues(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        batchProcessor.processBatch(accountsData, entId);

        // Assert
        verify(dataConverter, times(2)).convertToAccountCatalogue(any(), anyMap(), anyMap());
    }
}
