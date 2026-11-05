package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.AccountCatalogueHierarchyProcessor;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ImportErrorType;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogueExcelData;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.ImportErrorDetail;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.repository.IAccountCatalogueRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueHierarchyProcessorUnitTest {

    @Mock
    private IAccountCatalogueRepository accountCatalogueRepository;

    @InjectMocks
    private AccountCatalogueHierarchyProcessor hierarchyProcessor;

    private String entId;

    @BeforeEach
    void setUp() {
        entId = "ENT-001";
    }

    // ========== Tests para sortByHierarchy ==========

    @Test
    @DisplayName("Debe ordenar cuentas por jerarquía correctamente")
    void testSortByHierarchyOrdersCorrectly() {
        // Arrange
        List<AccountCatalogueExcelData> accounts = new ArrayList<>();
        accounts.add(createExcelData(1, "1105"));
        accounts.add(createExcelData(2, "1"));
        accounts.add(createExcelData(3, "11"));
        accounts.add(createExcelData(4, "110501"));

        // Act
        List<AccountCatalogueExcelData> result = hierarchyProcessor.sortByHierarchy(accounts);

        // Assert
        assertEquals(4, result.size());
        assertEquals("1", result.get(0).getCode());
        assertEquals("11", result.get(1).getCode());
        assertEquals("1105", result.get(2).getCode());
        assertEquals("110501", result.get(3).getCode());
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando entrada es null")
    void testSortByHierarchyReturnsEmptyWhenNull() {
        // Arrange - null

        // Act
        List<AccountCatalogueExcelData> result = hierarchyProcessor.sortByHierarchy(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando entrada está vacía")
    void testSortByHierarchyReturnsEmptyWhenEmpty() {
        // Arrange
        List<AccountCatalogueExcelData> accounts = new ArrayList<>();

        // Act
        List<AccountCatalogueExcelData> result = hierarchyProcessor.sortByHierarchy(accounts);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe manejar lista con un solo elemento")
    void testSortByHierarchyHandlesSingleElement() {
        // Arrange
        List<AccountCatalogueExcelData> accounts = new ArrayList<>();
        accounts.add(createExcelData(1, "1105"));

        // Act
        List<AccountCatalogueExcelData> result = hierarchyProcessor.sortByHierarchy(accounts);

        // Assert
        assertEquals(1, result.size());
        assertEquals("1105", result.get(0).getCode());
    }

    @Test
    @DisplayName("Debe ordenar cuentas auxiliares después de sus padres")
    void testSortByHierarchyOrdersAuxiliaryAccountsLast() {
        // Arrange
        List<AccountCatalogueExcelData> accounts = new ArrayList<>();
        accounts.add(createExcelData(1, "11050101"));
        accounts.add(createExcelData(2, "110501"));
        accounts.add(createExcelData(3, "1105"));

        // Act
        List<AccountCatalogueExcelData> result = hierarchyProcessor.sortByHierarchy(accounts);

        // Assert
        assertEquals("1105", result.get(0).getCode());
        assertEquals("110501", result.get(1).getCode());
        assertEquals("11050101", result.get(2).getCode());
    }

    // ========== Tests para validateHierarchyWithDetails ==========

    @Test
    @DisplayName("Debe retornar lista vacía cuando todas las cuentas tienen padre válido")
    void testValidateHierarchyNoErrorsWhenAllParentsExist() {
        // Arrange
        List<AccountCatalogueExcelData> accounts = new ArrayList<>();
        accounts.add(createExcelData(1, "1"));
        accounts.add(createExcelData(2, "11"));
        accounts.add(createExcelData(3, "1105"));

        // Act
        List<ImportErrorDetail> errors = hierarchyProcessor.validateHierarchyWithDetails(accounts, entId);

        // Assert
        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Debe detectar cuenta huérfana cuando padre no existe en Excel ni BD")
    void testValidateHierarchyDetectsOrphanAccount() {
        // Arrange
        List<AccountCatalogueExcelData> accounts = new ArrayList<>();
        accounts.add(createExcelData(1, "1105"));
        when(accountCatalogueRepository.findByCode("11", entId)).thenReturn(null);

        // Act
        List<ImportErrorDetail> errors = hierarchyProcessor.validateHierarchyWithDetails(accounts, entId);

        // Assert
        assertEquals(1, errors.size());
        assertEquals(ImportErrorType.HIERARCHY_ERROR, errors.get(0).getErrorType());
        assertTrue(errors.get(0).getErrorMessage().contains("1105"));
        assertTrue(errors.get(0).getErrorMessage().contains("11"));
    }

    @Test
    @DisplayName("Debe aceptar cuenta cuando padre existe en Excel")
    void testValidateHierarchyAcceptsWhenParentInExcel() {
        // Arrange
        List<AccountCatalogueExcelData> accounts = new ArrayList<>();
        accounts.add(createExcelData(1, "1"));
        accounts.add(createExcelData(2, "11"));
        accounts.add(createExcelData(3, "1105"));

        // Act
        List<ImportErrorDetail> errors = hierarchyProcessor.validateHierarchyWithDetails(accounts, entId);

        // Assert
        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Debe aceptar cuenta cuando padre existe en base de datos")
    void testValidateHierarchyAcceptsWhenParentInDatabase() {
        // Arrange
        List<AccountCatalogueExcelData> accounts = new ArrayList<>();
        accounts.add(createExcelData(1, "1105"));
        AccountCatalogueEntity parentEntity = AccountCatalogueEntity.builder()
                .id(1L)
                .code("11")
                .build();
        when(accountCatalogueRepository.findByCode("11", entId)).thenReturn(parentEntity);

        // Act
        List<ImportErrorDetail> errors = hierarchyProcessor.validateHierarchyWithDetails(accounts, entId);

        // Assert
        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("No debe validar padre para cuentas raíz")
    void testValidateHierarchySkipsRootAccounts() {
        // Arrange
        List<AccountCatalogueExcelData> accounts = new ArrayList<>();
        accounts.add(createExcelData(1, "1"));
        accounts.add(createExcelData(2, "2"));

        // Act
        List<ImportErrorDetail> errors = hierarchyProcessor.validateHierarchyWithDetails(accounts, entId);

        // Assert
        assertTrue(errors.isEmpty());
        verify(accountCatalogueRepository, never()).findByCode(any(), any());
    }

    @Test
    @DisplayName("Debe detectar múltiples cuentas huérfanas")
    void testValidateHierarchyDetectsMultipleOrphans() {
        // Arrange
        List<AccountCatalogueExcelData> accounts = new ArrayList<>();
        accounts.add(createExcelData(1, "1105"));
        accounts.add(createExcelData(2, "2105"));
        when(accountCatalogueRepository.findByCode("11", entId)).thenReturn(null);
        when(accountCatalogueRepository.findByCode("21", entId)).thenReturn(null);

        // Act
        List<ImportErrorDetail> errors = hierarchyProcessor.validateHierarchyWithDetails(accounts, entId);

        // Assert
        assertEquals(2, errors.size());
    }

    @Test
    @DisplayName("Debe incluir número de fila en error de jerarquía")
    void testValidateHierarchyIncludesRowNumber() {
        // Arrange
        List<AccountCatalogueExcelData> accounts = new ArrayList<>();
        accounts.add(createExcelData(5, "1105"));
        when(accountCatalogueRepository.findByCode("11", entId)).thenReturn(null);

        // Act
        List<ImportErrorDetail> errors = hierarchyProcessor.validateHierarchyWithDetails(accounts, entId);

        // Assert
        assertEquals(5, errors.get(0).getRowNumber());
    }

    @Test
    @DisplayName("Debe manejar excepción en búsqueda de base de datos")
    void testValidateHierarchyHandlesDatabaseException() {
        // Arrange
        List<AccountCatalogueExcelData> accounts = new ArrayList<>();
        accounts.add(createExcelData(1, "1105"));
        when(accountCatalogueRepository.findByCode("11", entId)).thenThrow(new RuntimeException("DB Error"));

        // Act
        List<ImportErrorDetail> errors = hierarchyProcessor.validateHierarchyWithDetails(accounts, entId);

        // Assert
        assertEquals(1, errors.size());
    }

    @Test
    @DisplayName("Debe validar jerarquía completa de 5 niveles")
    void testValidateHierarchyValidatesAllLevels() {
        // Arrange
        List<AccountCatalogueExcelData> accounts = new ArrayList<>();
        accounts.add(createExcelData(1, "1"));
        accounts.add(createExcelData(2, "11"));
        accounts.add(createExcelData(3, "1105"));
        accounts.add(createExcelData(4, "110501"));
        accounts.add(createExcelData(5, "11050101"));

        // Act
        List<ImportErrorDetail> errors = hierarchyProcessor.validateHierarchyWithDetails(accounts, entId);

        // Assert
        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Debe manejar códigos con espacios")
    void testValidateHierarchyHandlesCodesWithSpaces() {
        // Arrange
        List<AccountCatalogueExcelData> accounts = new ArrayList<>();
        accounts.add(createExcelData(1, " 1 "));
        accounts.add(createExcelData(2, " 11 "));
        accounts.add(createExcelData(3, " 1105 "));

        // Act
        List<ImportErrorDetail> errors = hierarchyProcessor.validateHierarchyWithDetails(accounts, entId);

        // Assert
        assertTrue(errors.isEmpty());
    }

    // ========== Tests para buildParentMapFromDatabase ==========

    @Test
    @DisplayName("Debe construir mapa de padres desde BD correctamente")
    void testBuildParentMapFromDatabaseSuccess() {
        // Arrange
        Set<String> parentCodes = new HashSet<>(Arrays.asList("11", "1105"));
        AccountCatalogueEntity parent1 = AccountCatalogueEntity.builder()
                .id(1L)
                .code("11")
                .build();
        AccountCatalogueEntity parent2 = AccountCatalogueEntity.builder()
                .id(2L)
                .code("1105")
                .build();
        when(accountCatalogueRepository.findByCodeWithFullHierarchy("11", entId)).thenReturn(parent1);
        when(accountCatalogueRepository.findByCodeWithFullHierarchy("1105", entId)).thenReturn(parent2);

        // Act
        Map<String, AccountCatalogueEntity> result = hierarchyProcessor.buildParentMapFromDatabase(parentCodes, entId);

        // Assert
        assertEquals(2, result.size());
        assertEquals(parent1, result.get("11"));
        assertEquals(parent2, result.get("1105"));
    }

    @Test
    @DisplayName("Debe retornar mapa vacío cuando conjunto de códigos está vacío")
    void testBuildParentMapFromDatabaseEmptySet() {
        // Arrange
        Set<String> parentCodes = new HashSet<>();

        // Act
        Map<String, AccountCatalogueEntity> result = hierarchyProcessor.buildParentMapFromDatabase(parentCodes, entId);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe ignorar códigos null en conjunto")
    void testBuildParentMapFromDatabaseIgnoresNullCodes() {
        // Arrange
        Set<String> parentCodes = new HashSet<>();
        parentCodes.add(null);
        parentCodes.add("11");
        AccountCatalogueEntity parent = AccountCatalogueEntity.builder()
                .id(1L)
                .code("11")
                .build();
        when(accountCatalogueRepository.findByCodeWithFullHierarchy("11", entId)).thenReturn(parent);

        // Act
        Map<String, AccountCatalogueEntity> result = hierarchyProcessor.buildParentMapFromDatabase(parentCodes, entId);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.containsKey("11"));
    }

    @Test
    @DisplayName("Debe omitir cuentas que no existen en BD")
    void testBuildParentMapFromDatabaseSkipsNonExistent() {
        // Arrange
        Set<String> parentCodes = new HashSet<>(Arrays.asList("11", "99"));
        AccountCatalogueEntity parent = AccountCatalogueEntity.builder()
                .id(1L)
                .code("11")
                .build();
        when(accountCatalogueRepository.findByCodeWithFullHierarchy("11", entId)).thenReturn(parent);
        when(accountCatalogueRepository.findByCodeWithFullHierarchy("99", entId)).thenReturn(null);

        // Act
        Map<String, AccountCatalogueEntity> result = hierarchyProcessor.buildParentMapFromDatabase(parentCodes, entId);

        // Assert
        assertEquals(1, result.size());
        assertFalse(result.containsKey("99"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando BD falla")
    void testBuildParentMapFromDatabaseThrowsOnDbError() {
        // Arrange
        Set<String> parentCodes = new HashSet<>(Collections.singletonList("11"));
        when(accountCatalogueRepository.findByCodeWithFullHierarchy("11", entId))
                .thenThrow(new RuntimeException("DB Error"));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> hierarchyProcessor.buildParentMapFromDatabase(parentCodes, entId));
    }

    @Test
    @DisplayName("No debe duplicar búsquedas para códigos repetidos")
    void testBuildParentMapFromDatabaseNoDuplicateQueries() {
        // Arrange
        Set<String> parentCodes = new HashSet<>(Collections.singletonList("11"));
        AccountCatalogueEntity parent = AccountCatalogueEntity.builder()
                .id(1L)
                .code("11")
                .build();
        when(accountCatalogueRepository.findByCodeWithFullHierarchy("11", entId)).thenReturn(parent);

        // Act
        hierarchyProcessor.buildParentMapFromDatabase(parentCodes, entId);

        // Assert
        verify(accountCatalogueRepository, times(1)).findByCodeWithFullHierarchy("11", entId);
    }

    // ========== Tests para reloadAccountWithFullHierarchy ==========

    @Test
    @DisplayName("Debe recargar cuenta con jerarquía completa")
    void testReloadAccountWithFullHierarchySuccess() {
        // Arrange
        AccountCatalogueEntity parentEntity = AccountCatalogueEntity.builder()
                .id(1L)
                .code("11")
                .description("Disponible")
                .build();
        AccountCatalogueEntity entity = AccountCatalogueEntity.builder()
                .id(2L)
                .code("1105")
                .description("Caja")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .parent(parentEntity)
                .crossing(false)
                .costCenter(false)
                .status(true)
                .idEnterprise(entId)
                .build();
        when(accountCatalogueRepository.findByCodeWithFullHierarchy("1105", entId)).thenReturn(entity);

        // Act
        AccountCatalogue result = hierarchyProcessor.reloadAccountWithFullHierarchy("1105", entId);

        // Assert
        assertNotNull(result);
        assertEquals("1105", result.getCode());
        assertNotNull(result.getParent());
        assertEquals("11", result.getParent().getCode());
    }

    @Test
    @DisplayName("Debe retornar null cuando cuenta no existe")
    void testReloadAccountWithFullHierarchyReturnsNullWhenNotFound() {
        // Arrange
        when(accountCatalogueRepository.findByCodeWithFullHierarchy("9999", entId)).thenReturn(null);

        // Act
        AccountCatalogue result = hierarchyProcessor.reloadAccountWithFullHierarchy("9999", entId);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Debe convertir todos los campos de la entidad")
    void testReloadAccountWithFullHierarchyConvertsAllFields() {
        // Arrange
        AccountCatalogueEntity entity = AccountCatalogueEntity.builder()
                .id(1L)
                .code("1105")
                .description("Caja")
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .crossing(true)
                .costCenter(true)
                .status(true)
                .idEnterprise(entId)
                .build();
        when(accountCatalogueRepository.findByCodeWithFullHierarchy("1105", entId)).thenReturn(entity);

        // Act
        AccountCatalogue result = hierarchyProcessor.reloadAccountWithFullHierarchy("1105", entId);

        // Assert
        assertEquals(1L, result.getId());
        assertEquals("1105", result.getCode());
        assertEquals("Caja", result.getDescription());
        assertEquals(NatureEnum.DEBIT, result.getNature());
        assertEquals(FinancialStatusEnum.STATEMENTFINANCIALPOSITION, result.getFinancialStatus());
        assertEquals(ClassificationEnum.CURRENTASSETS, result.getClassification());
        assertTrue(result.getCrossing());
        assertTrue(result.getCostCenter());
        assertTrue(result.getStatus());
        assertEquals(entId, result.getIdEnterprise());
    }

    @Test
    @DisplayName("Debe convertir jerarquía de padres recursivamente")
    void testReloadAccountWithFullHierarchyConvertsParentsRecursively() {
        // Arrange
        AccountCatalogueEntity grandParent = AccountCatalogueEntity.builder()
                .id(1L)
                .code("1")
                .description("Activos")
                .idEnterprise(entId)
                .build();
        AccountCatalogueEntity parentEntity = AccountCatalogueEntity.builder()
                .id(2L)
                .code("11")
                .description("Disponible")
                .parent(grandParent)
                .idEnterprise(entId)
                .build();
        AccountCatalogueEntity entity = AccountCatalogueEntity.builder()
                .id(3L)
                .code("1105")
                .description("Caja")
                .parent(parentEntity)
                .idEnterprise(entId)
                .build();
        when(accountCatalogueRepository.findByCodeWithFullHierarchy("1105", entId)).thenReturn(entity);

        // Act
        AccountCatalogue result = hierarchyProcessor.reloadAccountWithFullHierarchy("1105", entId);

        // Assert
        assertNotNull(result.getParent());
        assertEquals("11", result.getParent().getCode());
        assertNotNull(result.getParent().getParent());
        assertEquals("1", result.getParent().getParent().getCode());
    }

    @Test
    @DisplayName("Debe manejar cuenta sin padre")
    void testReloadAccountWithFullHierarchyHandlesNoParent() {
        // Arrange
        AccountCatalogueEntity entity = AccountCatalogueEntity.builder()
                .id(1L)
                .code("1")
                .description("Activos")
                .parent(null)
                .idEnterprise(entId)
                .build();
        when(accountCatalogueRepository.findByCodeWithFullHierarchy("1", entId)).thenReturn(entity);

        // Act
        AccountCatalogue result = hierarchyProcessor.reloadAccountWithFullHierarchy("1", entId);

        // Assert
        assertNotNull(result);
        assertNull(result.getParent());
    }

    // ========== Método auxiliar ==========

    private AccountCatalogueExcelData createExcelData(int rowNumber, String code) {
        return AccountCatalogueExcelData.builder()
                .rowNumber(rowNumber)
                .code(code)
                .description("Cuenta " + code)
                .idEnterprise(entId)
                .nature(NatureEnum.DEBIT)
                .financialStatus(FinancialStatusEnum.STATEMENTFINANCIALPOSITION)
                .classification(ClassificationEnum.CURRENTASSETS)
                .build();
    }
}
