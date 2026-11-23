package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.application.services.importExport;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.importExport.AccountCatalogueDuplicateDetectionService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogueExcelData;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.repository.IAccountCatalogueRepository;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueImportException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueDuplicateDetectionServiceUnitTest {

    @Mock
    private IAccountCatalogueRepository accountCatalogueRepository;

    @InjectMocks
    private AccountCatalogueDuplicateDetectionService duplicateDetectionService;

    private String entId;
    private AccountCatalogueExcelData excelData1;
    private AccountCatalogueExcelData excelData2;
    private AccountCatalogueEntity existingEntity;

    @BeforeEach
    void setUp() {
        entId = "ENT-001";

        excelData1 = AccountCatalogueExcelData.builder()
                .rowNumber(2)
                .idEnterprise(entId)
                .code("1105")
                .description("Caja General")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .crossing(false)
                .costCenter(false)
                .build();

        excelData2 = AccountCatalogueExcelData.builder()
                .rowNumber(3)
                .idEnterprise(entId)
                .code("1110")
                .description("Bancos")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .crossing(false)
                .costCenter(false)
                .build();

        existingEntity = new AccountCatalogueEntity();
        existingEntity.setId(1L);
        existingEntity.setCode("1105");
        existingEntity.setDescription("Caja General");
        existingEntity.setIdEnterprise(entId);
    }

    @Test
    @DisplayName("Debe detectar registros únicos sin duplicados")
    void testDetectDuplicatesWithNoDuplicates() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelData1, excelData2);
        
        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueRepository.findByDescriptionsInIgnoreCase(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());

        // Act
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult result = 
                duplicateDetectionService.detectDuplicates(accountsData, entId);

        // Assert
        assertEquals(2, result.getUniqueRecords().size());
        assertEquals(0, result.getDuplicateCount());
        assertEquals(2, result.getUniqueCount());
        assertEquals(2, result.getTotalAnalyzed());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Debe detectar duplicado por código en base de datos")
    void testDetectDuplicatesByCodeInDatabase() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelData1, excelData2);
        
        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(entId)))
                .thenReturn(List.of(existingEntity));
        when(accountCatalogueRepository.findByDescriptionsInIgnoreCase(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());

        // Act
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult result = 
                duplicateDetectionService.detectDuplicates(accountsData, entId);

        // Assert
        assertEquals(1, result.getUniqueRecords().size());
        assertEquals(1, result.getDuplicateCount());
        assertEquals("1110", result.getUniqueRecords().get(0).getCode());
    }

    @Test
    @DisplayName("Debe detectar duplicado por descripción en base de datos")
    void testDetectDuplicatesByDescriptionInDatabase() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelData1, excelData2);
        
        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueRepository.findByDescriptionsInIgnoreCase(anyList(), eq(entId)))
                .thenReturn(List.of(existingEntity));

        // Act
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult result = 
                duplicateDetectionService.detectDuplicates(accountsData, entId);

        // Assert
        assertEquals(1, result.getUniqueRecords().size());
        assertEquals(1, result.getDuplicateCount());
        assertEquals("1110", result.getUniqueRecords().get(0).getCode());
    }

    @Test
    @DisplayName("Debe detectar duplicados internos por código en Excel")
    void testDetectInternalDuplicatesByCode() {
        // Arrange
        AccountCatalogueExcelData duplicateCode = AccountCatalogueExcelData.builder()
                .rowNumber(4)
                .idEnterprise(entId)
                .code("1105")
                .description("Otra Descripción")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .build();
        List<AccountCatalogueExcelData> accountsData = List.of(excelData1, duplicateCode);
        
        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueRepository.findByDescriptionsInIgnoreCase(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());

        // Act
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult result = 
                duplicateDetectionService.detectDuplicates(accountsData, entId);

        // Assert
        assertEquals(1, result.getUniqueRecords().size());
        assertEquals(1, result.getDuplicateCount());
        assertEquals(2, result.getUniqueRecords().get(0).getRowNumber());
    }

    @Test
    @DisplayName("Debe detectar duplicados internos por descripción en Excel")
    void testDetectInternalDuplicatesByDescription() {
        // Arrange
        AccountCatalogueExcelData duplicateDesc = AccountCatalogueExcelData.builder()
                .rowNumber(4)
                .idEnterprise(entId)
                .code("1115")
                .description("Caja General")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .build();
        List<AccountCatalogueExcelData> accountsData = List.of(excelData1, duplicateDesc);
        
        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueRepository.findByDescriptionsInIgnoreCase(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());

        // Act
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult result = 
                duplicateDetectionService.detectDuplicates(accountsData, entId);

        // Assert
        assertEquals(1, result.getUniqueRecords().size());
        assertEquals(1, result.getDuplicateCount());
    }

    @Test
    @DisplayName("Debe manejar lista vacía de cuentas")
    void testDetectDuplicatesWithEmptyList() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = Collections.emptyList();

        // Act
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult result = 
                duplicateDetectionService.detectDuplicates(accountsData, entId);

        // Assert
        assertTrue(result.getUniqueRecords().isEmpty());
        assertEquals(0, result.getDuplicateCount());
        assertEquals(0, result.getTotalAnalyzed());
    }

    @Test
    @DisplayName("Debe manejar excepción al consultar duplicados por código")
    void testDetectDuplicatesThrowsExceptionOnCodeQuery() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelData1);
        
        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(entId)))
                .thenThrow(new RuntimeException("Error de conexión"));

        // Act & Assert
        assertThrows(AccountCatalogueImportException.class, 
                () -> duplicateDetectionService.detectDuplicates(accountsData, entId));
    }

    @Test
    @DisplayName("Debe manejar excepción al consultar duplicados por descripción")
    void testDetectDuplicatesThrowsExceptionOnDescriptionQuery() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelData1);
        
        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueRepository.findByDescriptionsInIgnoreCase(anyList(), eq(entId)))
                .thenThrow(new RuntimeException("Error de conexión"));

        // Act & Assert
        assertThrows(AccountCatalogueImportException.class, 
                () -> duplicateDetectionService.detectDuplicates(accountsData, entId));
    }

    @Test
    @DisplayName("Debe retornar lista de errores vacía en omisión silenciosa")
    void testDetectDuplicatesReturnsEmptyErrors() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelData1, excelData1);
        
        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueRepository.findByDescriptionsInIgnoreCase(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());

        // Act
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult result = 
                duplicateDetectionService.detectDuplicates(accountsData, entId);

        // Assert
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Debe manejar descripciones con espacios y normalizar para comparación")
    void testDetectDuplicatesNormalizesDescriptions() {
        // Arrange
        AccountCatalogueExcelData dataWithSpaces = AccountCatalogueExcelData.builder()
                .rowNumber(4)
                .idEnterprise(entId)
                .code("1120")
                .description("  Caja General  ")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .build();
        List<AccountCatalogueExcelData> accountsData = List.of(excelData1, dataWithSpaces);
        
        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueRepository.findByDescriptionsInIgnoreCase(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());

        // Act
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult result = 
                duplicateDetectionService.detectDuplicates(accountsData, entId);

        // Assert
        assertEquals(1, result.getUniqueRecords().size());
        assertEquals(1, result.getDuplicateCount());
    }

    @Test
    @DisplayName("Debe consultar códigos distintos en batch")
    void testDetectDuplicatesQueriesDistinctCodes() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelData1, excelData2);
        
        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueRepository.findByDescriptionsInIgnoreCase(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());

        // Act
        duplicateDetectionService.detectDuplicates(accountsData, entId);

        // Assert
        verify(accountCatalogueRepository).findByCodesIn(
                argThat(codes -> codes.size() == 2 && codes.contains("1105") && codes.contains("1110")), 
                eq(entId));
    }

    @Test
    @DisplayName("Debe consultar descripciones en mayúsculas para case-insensitive")
    void testDetectDuplicatesQueriesUppercaseDescriptions() {
        // Arrange
        List<AccountCatalogueExcelData> accountsData = List.of(excelData1);
        
        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueRepository.findByDescriptionsInIgnoreCase(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());

        // Act
        duplicateDetectionService.detectDuplicates(accountsData, entId);

        // Assert
        verify(accountCatalogueRepository).findByDescriptionsInIgnoreCase(
                argThat(descriptions -> descriptions.stream().allMatch(d -> d.equals(d.toUpperCase()))), 
                eq(entId));
    }

    @Test
    @DisplayName("Debe manejar código null en datos de Excel")
    void testDetectDuplicatesHandlesNullCode() {
        // Arrange
        AccountCatalogueExcelData dataWithNullCode = AccountCatalogueExcelData.builder()
                .rowNumber(4)
                .idEnterprise(entId)
                .code(null)
                .description("Descripción sin código")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .build();
        List<AccountCatalogueExcelData> accountsData = List.of(excelData1, dataWithNullCode);
        
        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueRepository.findByDescriptionsInIgnoreCase(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());

        // Act
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult result = 
                duplicateDetectionService.detectDuplicates(accountsData, entId);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueRepository).findByCodesIn(
                argThat(codes -> codes.size() == 1 && codes.contains("1105")), 
                eq(entId));
    }

    @Test
    @DisplayName("Debe manejar descripción null en datos de Excel")
    void testDetectDuplicatesHandlesNullDescription() {
        // Arrange
        AccountCatalogueExcelData dataWithNullDesc = AccountCatalogueExcelData.builder()
                .rowNumber(4)
                .idEnterprise(entId)
                .code("1120")
                .description(null)
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .build();
        List<AccountCatalogueExcelData> accountsData = List.of(excelData1, dataWithNullDesc);
        
        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueRepository.findByDescriptionsInIgnoreCase(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());

        // Act
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult result = 
                duplicateDetectionService.detectDuplicates(accountsData, entId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getUniqueRecords().size());
    }

    @Test
    @DisplayName("Debe detectar múltiples duplicados en base de datos")
    void testDetectMultipleDatabaseDuplicates() {
        // Arrange
        AccountCatalogueEntity existingEntity2 = new AccountCatalogueEntity();
        existingEntity2.setId(2L);
        existingEntity2.setCode("1110");
        existingEntity2.setDescription("Bancos");
        existingEntity2.setIdEnterprise(entId);
        
        List<AccountCatalogueExcelData> accountsData = List.of(excelData1, excelData2);
        
        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(entId)))
                .thenReturn(List.of(existingEntity, existingEntity2));
        when(accountCatalogueRepository.findByDescriptionsInIgnoreCase(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());

        // Act
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult result = 
                duplicateDetectionService.detectDuplicates(accountsData, entId);

        // Assert
        assertEquals(0, result.getUniqueRecords().size());
        assertEquals(2, result.getDuplicateCount());
    }

    @Test
    @DisplayName("Debe preservar orden de registros únicos")
    void testDetectDuplicatesPreservesOrder() {
        // Arrange
        AccountCatalogueExcelData excelData3 = AccountCatalogueExcelData.builder()
                .rowNumber(5)
                .idEnterprise(entId)
                .code("1115")
                .description("Inversiones")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .build();
        List<AccountCatalogueExcelData> accountsData = List.of(excelData1, excelData2, excelData3);
        
        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueRepository.findByDescriptionsInIgnoreCase(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());

        // Act
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult result = 
                duplicateDetectionService.detectDuplicates(accountsData, entId);

        // Assert
        assertEquals(3, result.getUniqueRecords().size());
        assertEquals("1105", result.getUniqueRecords().get(0).getCode());
        assertEquals("1110", result.getUniqueRecords().get(1).getCode());
        assertEquals("1115", result.getUniqueRecords().get(2).getCode());
    }

    @Test
    @DisplayName("Debe manejar descripción vacía en datos de Excel")
    void testDetectDuplicatesHandlesEmptyDescription() {
        // Arrange
        AccountCatalogueExcelData dataWithEmptyDesc = AccountCatalogueExcelData.builder()
                .rowNumber(4)
                .idEnterprise(entId)
                .code("1120")
                .description("   ")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .build();
        List<AccountCatalogueExcelData> accountsData = List.of(excelData1, dataWithEmptyDesc);
        
        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueRepository.findByDescriptionsInIgnoreCase(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());

        // Act
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult result = 
                duplicateDetectionService.detectDuplicates(accountsData, entId);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Debe manejar entidad con descripción null desde base de datos")
    void testDetectDuplicatesHandlesNullDescriptionFromDatabase() {
        // Arrange
        AccountCatalogueEntity entityWithNullDesc = new AccountCatalogueEntity();
        entityWithNullDesc.setId(2L);
        entityWithNullDesc.setCode("1120");
        entityWithNullDesc.setDescription(null);
        entityWithNullDesc.setIdEnterprise(entId);
        
        List<AccountCatalogueExcelData> accountsData = List.of(excelData1);
        
        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueRepository.findByDescriptionsInIgnoreCase(anyList(), eq(entId)))
                .thenReturn(List.of(entityWithNullDesc));

        // Act
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult result = 
                duplicateDetectionService.detectDuplicates(accountsData, entId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getUniqueRecords().size());
    }

    @Test
    @DisplayName("Debe calcular correctamente uniqueCount y duplicateCount")
    void testDetectDuplicatesCalculatesCountsCorrectly() {
        // Arrange
        AccountCatalogueExcelData duplicateInExcel = AccountCatalogueExcelData.builder()
                .rowNumber(4)
                .idEnterprise(entId)
                .code("1105")
                .description("Diferente descripción")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .build();
        List<AccountCatalogueExcelData> accountsData = List.of(excelData1, excelData2, duplicateInExcel);
        
        when(accountCatalogueRepository.findByCodesIn(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());
        when(accountCatalogueRepository.findByDescriptionsInIgnoreCase(anyList(), eq(entId)))
                .thenReturn(Collections.emptyList());

        // Act
        AccountCatalogueDuplicateDetectionService.DuplicateDetectionResult result = 
                duplicateDetectionService.detectDuplicates(accountsData, entId);

        // Assert
        assertEquals(3, result.getTotalAnalyzed());
        assertEquals(2, result.getUniqueCount());
        assertEquals(1, result.getDuplicateCount());
    }
}
