package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.AccountCatalogueDataConverter;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogueExcelData;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueDataConverterUnitTest {

    @InjectMocks
    private AccountCatalogueDataConverter dataConverter;

    private String entId;
    private AccountCatalogueExcelData excelData;
    private Map<String, AccountCatalogueEntity> parentsMap;
    private Map<String, AccountCatalogue> processedAccountsMap;

    @BeforeEach
    void setUp() {
        entId = "ENT-001";
        parentsMap = new HashMap<>();
        processedAccountsMap = new HashMap<>();

        excelData = AccountCatalogueExcelData.builder()
                .rowNumber(1)
                .code("11050101")
                .description("Cuenta auxiliar de prueba")
                .idEnterprise(entId)
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .crossing(true)
                .costCenter(false)
                .build();
    }

    // ========== Tests para convertToAccountCatalogue - Campos básicos ==========

    @Test
    @DisplayName("Debe convertir datos de Excel a AccountCatalogue exitosamente")
    void testConvertToAccountCatalogueSuccess() {
        // Arrange - ya configurado en setUp

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertNotNull(result);
        assertEquals("11050101", result.getCode());
        assertEquals("Cuenta auxiliar de prueba", result.getDescription());
        assertEquals(entId, result.getIdEnterprise());
    }

    @Test
    @DisplayName("Debe asignar correctamente la naturaleza")
    void testConvertToAccountCatalogueAssignsNature() {
        // Arrange - ya configurado

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertEquals(NatureEnum.DEBIT, result.getNature());
    }

    @Test
    @DisplayName("Debe asignar correctamente el estado financiero")
    void testConvertToAccountCatalogueAssignsFinancialStatus() {
        // Arrange - ya configurado

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertEquals(FinancialStatusEnum.STATEMENTFINANCIALPOSITION, result.getFinancialStatus());
    }

    @Test
    @DisplayName("Debe asignar correctamente la clasificación")
    void testConvertToAccountCatalogueAssignsClassification() {
        // Arrange - ya configurado

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertEquals(ClassificationEnum.CURRENTASSETS, result.getClassification());
    }

    @Test
    @DisplayName("Debe asignar crossing cuando está presente")
    void testConvertToAccountCatalogueAssignsCrossing() {
        // Arrange
        excelData.setCrossing(true);

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertTrue(result.getCrossing());
    }

    @Test
    @DisplayName("Debe asignar costCenter cuando está presente")
    void testConvertToAccountCatalogueAssignsCostCenter() {
        // Arrange
        excelData.setCostCenter(true);

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertTrue(result.getCostCenter());
    }

    @Test
    @DisplayName("Debe asignar crossing como false cuando es null")
    void testConvertToAccountCatalogueDefaultsCrossingToFalse() {
        // Arrange
        excelData.setCrossing(null);

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertFalse(result.getCrossing());
    }

    @Test
    @DisplayName("Debe asignar costCenter como false cuando es null")
    void testConvertToAccountCatalogueDefaultsCostCenterToFalse() {
        // Arrange
        excelData.setCostCenter(null);

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertFalse(result.getCostCenter());
    }

    @Test
    @DisplayName("Debe asignar status como true por defecto")
    void testConvertToAccountCatalogueDefaultsStatusToTrue() {
        // Arrange - ya configurado

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertTrue(result.getStatus());
    }

    // ========== Tests para normalización ==========

    @Test
    @DisplayName("Debe normalizar código con espacios")
    void testConvertToAccountCatalogueNormalizesCodeWithSpaces() {
        // Arrange
        excelData.setCode("  11050101  ");

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertEquals("11050101", result.getCode());
    }

    @Test
    @DisplayName("Debe normalizar descripción con espacios múltiples")
    void testConvertToAccountCatalogueNormalizesDescriptionWithMultipleSpaces() {
        // Arrange
        excelData.setDescription("  Cuenta   auxiliar   de   prueba  ");

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertEquals("Cuenta auxiliar de prueba", result.getDescription());
    }

    @Test
    @DisplayName("Debe manejar código null")
    void testConvertToAccountCatalogueHandlesNullCode() {
        // Arrange
        excelData.setCode(null);

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertNull(result.getCode());
        assertNull(result.getParent());
    }

    @Test
    @DisplayName("Debe manejar descripción null")
    void testConvertToAccountCatalogueHandlesNullDescription() {
        // Arrange
        excelData.setDescription(null);

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertNull(result.getDescription());
    }

    // ========== Tests para resolución de padre - processedAccountsMap ==========

    @Test
    @DisplayName("Debe resolver padre desde processedAccountsMap")
    void testConvertToAccountCatalogueResolvesParentFromProcessedMap() {
        // Arrange
        AccountCatalogue parentAccount = AccountCatalogue.builder()
                .id(10L)
                .code("110501")
                .description("Cuenta padre")
                .build();
        processedAccountsMap.put("110501", parentAccount);

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertNotNull(result.getParent());
        assertEquals(10L, result.getParent().getId());
        assertEquals("110501", result.getParent().getCode());
    }

    @Test
    @DisplayName("Debe priorizar processedAccountsMap sobre parentsMap")
    void testConvertToAccountCataloguePrioritizesProcessedMapOverParentsMap() {
        // Arrange
        AccountCatalogue processedParent = AccountCatalogue.builder()
                .id(100L)
                .code("110501")
                .description("Padre del lote")
                .build();
        processedAccountsMap.put("110501", processedParent);

        AccountCatalogueEntity dbParent = AccountCatalogueEntity.builder()
                .id(200L)
                .code("110501")
                .description("Padre de BD")
                .build();
        parentsMap.put("110501", dbParent);

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertNotNull(result.getParent());
        assertEquals(100L, result.getParent().getId());
    }

    // ========== Tests para resolución de padre - parentsMap (BD) ==========

    @Test
    @DisplayName("Debe resolver padre desde parentsMap cuando no está en processedMap")
    void testConvertToAccountCatalogueResolvesParentFromParentsMap() {
        // Arrange
        AccountCatalogueEntity parentEntity = AccountCatalogueEntity.builder()
                .id(50L)
                .code("110501")
                .description("Cuenta padre BD")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .crossing(false)
                .costCenter(false)
                .status(true)
                .idEnterprise(entId)
                .build();
        parentsMap.put("110501", parentEntity);

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertNotNull(result.getParent());
        assertEquals(50L, result.getParent().getId());
        assertEquals("110501", result.getParent().getCode());
    }

    @Test
    @DisplayName("Debe convertir entidad padre con todos sus campos")
    void testConvertToAccountCatalogueConvertsParentEntityFully() {
        // Arrange
        AccountCatalogueEntity parentEntity = AccountCatalogueEntity.builder()
                .id(50L)
                .code("110501")
                .description("Cuenta padre BD")
                .nature(NatureEnum.CREDIT)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .classification(ClassificationEnum.CURRENTLIABILITIES)
                .crossing(true)
                .costCenter(true)
                .status(false)
                .idEnterprise(entId)
                .build();
        parentsMap.put("110501", parentEntity);

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        AccountCatalogue parent = result.getParent();
        assertNotNull(parent);
        assertEquals(50L, parent.getId());
        assertEquals("110501", parent.getCode());
        assertEquals("Cuenta padre BD", parent.getDescription());
        assertEquals(NatureEnum.CREDIT, parent.getNature());
        assertEquals(FinancialStatusEnum.INCOMESTATEMENT, parent.getFinancialStatus());
        assertEquals(ClassificationEnum.CURRENTLIABILITIES, parent.getClassification());
    }

    @Test
    @DisplayName("Debe resolver padre anidado desde entidad BD")
    void testConvertToAccountCatalogueResolvesNestedParent() {
        // Arrange
        AccountCatalogueEntity grandParent = AccountCatalogueEntity.builder()
                .id(5L)
                .code("1105")
                .build();

        AccountCatalogueEntity parentEntity = AccountCatalogueEntity.builder()
                .id(50L)
                .code("110501")
                .description("Cuenta padre BD")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .parent(grandParent)
                .status(true)
                .idEnterprise(entId)
                .build();
        parentsMap.put("110501", parentEntity);

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        AccountCatalogue parent = result.getParent();
        assertNotNull(parent);
        assertNotNull(parent.getParent());
        assertEquals(5L, parent.getParent().getId());
        assertEquals("1105", parent.getParent().getCode());
    }

    // ========== Tests para casos donde no hay padre ==========

    @Test
    @DisplayName("Debe retornar null como padre para cuenta raíz de un dígito")
    void testConvertToAccountCatalogueNoParentForRootAccount() {
        // Arrange
        excelData.setCode("1");

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertNull(result.getParent());
    }

    @Test
    @DisplayName("Debe retornar null como padre cuando padre no existe en ningún mapa")
    void testConvertToAccountCatalogueNoParentWhenNotFound() {
        // Arrange - parentsMap y processedAccountsMap están vacíos

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertNull(result.getParent());
    }

    @Test
    @DisplayName("Debe manejar código vacío sin buscar padre")
    void testConvertToAccountCatalogueHandlesEmptyCode() {
        // Arrange
        excelData.setCode("");

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertNull(result.getParent());
    }

    @Test
    @DisplayName("Debe manejar código con solo espacios")
    void testConvertToAccountCatalogueHandlesWhitespaceOnlyCode() {
        // Arrange
        excelData.setCode("   ");

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertNull(result.getParent());
    }

    // ========== Tests para diferentes niveles jerárquicos ==========

    @Test
    @DisplayName("Debe resolver padre para cuenta nivel 2 (2 dígitos -> 1 dígito)")
    void testConvertToAccountCatalogueResolvesParentForLevel2() {
        // Arrange
        excelData.setCode("11");
        AccountCatalogue parentAccount = AccountCatalogue.builder()
                .id(1L)
                .code("1")
                .description("Activos")
                .build();
        processedAccountsMap.put("1", parentAccount);

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertNotNull(result.getParent());
        assertEquals("1", result.getParent().getCode());
    }

    @Test
    @DisplayName("Debe resolver padre para cuenta nivel 3 (4 dígitos -> 2 dígitos)")
    void testConvertToAccountCatalogueResolvesParentForLevel3() {
        // Arrange
        excelData.setCode("1105");
        AccountCatalogue parentAccount = AccountCatalogue.builder()
                .id(2L)
                .code("11")
                .description("Disponible")
                .build();
        processedAccountsMap.put("11", parentAccount);

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertNotNull(result.getParent());
        assertEquals("11", result.getParent().getCode());
    }

    @Test
    @DisplayName("Debe resolver padre para cuenta nivel 4 (6 dígitos -> 4 dígitos)")
    void testConvertToAccountCatalogueResolvesParentForLevel4() {
        // Arrange
        excelData.setCode("110501");
        AccountCatalogue parentAccount = AccountCatalogue.builder()
                .id(3L)
                .code("1105")
                .description("Caja")
                .build();
        processedAccountsMap.put("1105", parentAccount);

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertNotNull(result.getParent());
        assertEquals("1105", result.getParent().getCode());
    }

    @Test
    @DisplayName("Debe resolver padre para cuenta nivel 5 (8 dígitos -> 6 dígitos)")
    void testConvertToAccountCatalogueResolvesParentForLevel5() {
        // Arrange
        excelData.setCode("11050101");
        AccountCatalogue parentAccount = AccountCatalogue.builder()
                .id(4L)
                .code("110501")
                .description("Caja General")
                .build();
        processedAccountsMap.put("110501", parentAccount);

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertNotNull(result.getParent());
        assertEquals("110501", result.getParent().getCode());
    }

    // ========== Tests para códigos no estándar ==========

    @Test
    @DisplayName("Debe manejar código con longitud no estándar sin padre")
    void testConvertToAccountCatalogueHandlesNonStandardCodeLength() {
        // Arrange
        excelData.setCode("123");

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertNull(result.getParent());
    }

    @Test
    @DisplayName("Debe manejar código de 5 dígitos sin padre")
    void testConvertToAccountCatalogueHandles5DigitCode() {
        // Arrange
        excelData.setCode("12345");

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertNull(result.getParent());
    }

    @Test
    @DisplayName("Debe manejar código de 7 dígitos sin padre")
    void testConvertToAccountCatalogueHandles7DigitCode() {
        // Arrange
        excelData.setCode("1234567");

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertNull(result.getParent());
    }

    // ========== Tests para mapas vacíos ==========

    @Test
    @DisplayName("Debe funcionar correctamente con ambos mapas vacíos")
    void testConvertToAccountCatalogueWithEmptyMaps() {
        // Arrange - mapas están vacíos por defecto

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertNotNull(result);
        assertEquals("11050101", result.getCode());
        assertNull(result.getParent());
    }

    @Test
    @DisplayName("Debe funcionar con parentsMap null y processedMap con padre")
    void testConvertToAccountCatalogueWithNullParentsMap() {
        // Arrange
        AccountCatalogue parentAccount = AccountCatalogue.builder()
                .id(10L)
                .code("110501")
                .build();
        processedAccountsMap.put("110501", parentAccount);

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, null, processedAccountsMap);

        // Assert
        assertNotNull(result.getParent());
        assertEquals("110501", result.getParent().getCode());
    }

    // ========== Tests para valores de enums ==========

    @Test
    @DisplayName("Debe asignar naturaleza CREDIT correctamente")
    void testConvertToAccountCatalogueWithCreditNature() {
        // Arrange
        excelData.setNature(NatureEnum.CREDIT);

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertEquals(NatureEnum.CREDIT, result.getNature());
    }

    @Test
    @DisplayName("Debe asignar estado financiero INCOMESTATEMENT correctamente")
    void testConvertToAccountCatalogueWithIncomeStatement() {
        // Arrange
        excelData.setFinancialStatus(FinancialStatusEnum.INCOMESTATEMENT);

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertEquals(FinancialStatusEnum.INCOMESTATEMENT, result.getFinancialStatus());
    }

    @Test
    @DisplayName("Debe asignar clasificación CURRENTLIABILITIES correctamente")
    void testConvertToAccountCatalogueWithCurrentLiabilitiesClassification() {
        // Arrange
        excelData.setClassification(ClassificationEnum.CURRENTLIABILITIES);

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertEquals(ClassificationEnum.CURRENTLIABILITIES, result.getClassification());
    }

    @Test
    @DisplayName("Debe asignar clasificación EQUITY correctamente")
    void testConvertToAccountCatalogueWithEquityClassification() {
        // Arrange
        excelData.setClassification(ClassificationEnum.EQUITY);

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertEquals(ClassificationEnum.EQUITY, result.getClassification());
    }

    @Test
    @DisplayName("Debe manejar enums null")
    void testConvertToAccountCatalogueWithNullEnums() {
        // Arrange
        excelData.setNature(null);
        excelData.setFinancialStatus(null);
        excelData.setClassification(null);

        // Act
        AccountCatalogue result = dataConverter.convertToAccountCatalogue(excelData, parentsMap, processedAccountsMap);

        // Assert
        assertNull(result.getNature());
        assertNull(result.getFinancialStatus());
        assertNull(result.getClassification());
    }
}
