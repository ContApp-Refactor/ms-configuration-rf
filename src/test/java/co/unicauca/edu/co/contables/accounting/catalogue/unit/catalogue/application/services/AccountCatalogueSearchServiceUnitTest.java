package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.AccountCatalogueSearchService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueSearchServiceUnitTest {

    @Mock
    private IAccountCatalogueSearchOutputPort accountCatalogueSearchOutputPort;

    @Mock
    private AccountCatalogueValidationService validationService;

    @InjectMocks
    private AccountCatalogueSearchService searchService;

    private String entId;
    private AccountCatalogue account;

    @BeforeEach
    void setUp() {
        entId = "ENT-001";
        account = AccountCatalogue.builder()
                .id(1L)
                .code("11050101")
                .description("Cuenta de prueba")
                .idEnterprise(entId)
                .status(true)
                .build();
    }

    // ========== Tests para getAccountCatalogueByCode ==========

    @Test
    @DisplayName("Debe obtener cuenta por código exitosamente")
    void testGetAccountCatalogueByCodeSuccess() {
        // Arrange
        when(validationService.validateAccountExists("11050101", entId)).thenReturn(account);

        // Act
        AccountCatalogue result = searchService.getAccountCatalogueByCode("11050101", entId);

        // Assert
        assertNotNull(result);
        assertEquals("11050101", result.getCode());
    }

    @Test
    @DisplayName("Debe delegar validación de existencia a validationService")
    void testGetAccountCatalogueByCodeDelegatesValidation() {
        // Arrange
        when(validationService.validateAccountExists("11050101", entId)).thenReturn(account);

        // Act
        searchService.getAccountCatalogueByCode("11050101", entId);

        // Assert
        verify(validationService).validateAccountExists("11050101", entId);
    }

    @Test
    @DisplayName("Debe propagar excepción cuando cuenta no existe")
    void testGetAccountCatalogueByCodeThrowsWhenNotFound() {
        // Arrange
        when(validationService.validateAccountExists("99999999", entId))
                .thenThrow(new AccountCatalogueNotFoundException("No encontrada"));

        // Act & Assert
        assertThrows(AccountCatalogueNotFoundException.class,
                () -> searchService.getAccountCatalogueByCode("99999999", entId));
    }

    // ========== Tests para getAccountCatalogueTree ==========

    @Test
    @DisplayName("Debe obtener árbol jerárquico por código exitosamente")
    void testGetAccountCatalogueTreeSuccess() {
        // Arrange
        AccountCatalogue tree = AccountCatalogue.builder()
                .id(1L)
                .code("1")
                .children(new ArrayList<>())
                .build();
        when(validationService.validateAccountExists("1", entId)).thenReturn(account);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByCode("1", entId)).thenReturn(tree);

        // Act
        AccountCatalogue result = searchService.getAccountCatalogueTree("1", entId);

        // Assert
        assertNotNull(result);
        assertEquals("1", result.getCode());
    }

    @Test
    @DisplayName("Debe validar existencia antes de obtener árbol")
    void testGetAccountCatalogueTreeValidatesFirst() {
        // Arrange
        when(validationService.validateAccountExists("1", entId)).thenReturn(account);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByCode("1", entId)).thenReturn(account);

        // Act
        searchService.getAccountCatalogueTree("1", entId);

        // Assert
        verify(validationService).validateAccountExists("1", entId);
        verify(accountCatalogueSearchOutputPort).getAccountCatalogueTreeByCode("1", entId);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta raíz no existe")
    void testGetAccountCatalogueTreeThrowsWhenNotFound() {
        // Arrange
        when(validationService.validateAccountExists("9", entId))
                .thenThrow(new AccountCatalogueNotFoundException("No encontrada"));

        // Act & Assert
        assertThrows(AccountCatalogueNotFoundException.class,
                () -> searchService.getAccountCatalogueTree("9", entId));
    }

    // ========== Tests para getAccountCatalogueTrees ==========

    @Test
    @DisplayName("Debe obtener árboles para todas las cuentas raíz existentes")
    void testGetAccountCatalogueTreesSuccess() {
        // Arrange
        AccountCatalogue tree1 = AccountCatalogue.builder().id(1L).code("1").build();
        AccountCatalogue tree2 = AccountCatalogue.builder().id(2L).code("2").build();
        
        when(validationService.validateAccountExists("1", entId)).thenReturn(tree1);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByCode("1", entId)).thenReturn(tree1);
        
        when(validationService.validateAccountExists("2", entId)).thenReturn(tree2);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByCode("2", entId)).thenReturn(tree2);
        
        for (int i = 3; i <= 9; i++) {
            when(validationService.validateAccountExists(String.valueOf(i), entId))
                    .thenThrow(new AccountCatalogueNotFoundException("No encontrada"));
        }

        // Act
        List<AccountCatalogue> result = searchService.getAccountCatalogueTrees(entId);

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay cuentas raíz")
    void testGetAccountCatalogueTreesReturnsEmptyWhenNoRoots() {
        // Arrange
        for (int i = 1; i <= 9; i++) {
            when(validationService.validateAccountExists(String.valueOf(i), entId))
                    .thenThrow(new AccountCatalogueNotFoundException("No encontrada"));
        }

        // Act
        List<AccountCatalogue> result = searchService.getAccountCatalogueTrees(entId);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando idEnterprise es null")
    void testGetAccountCatalogueTreesThrowsWhenIdEnterpriseNull() {
        // Arrange - null

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getAccountCatalogueTrees(null));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando idEnterprise está vacío")
    void testGetAccountCatalogueTreesThrowsWhenIdEnterpriseEmpty() {
        // Arrange - empty string

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getAccountCatalogueTrees("   "));
    }

    // ========== Tests para getAccountCatalogueById ==========

    @Test
    @DisplayName("Debe obtener cuenta por ID exitosamente")
    void testGetAccountCatalogueByIdSuccess() {
        // Arrange
        when(validationService.validateAccountExistsByIdAndEnterprise(1L, entId)).thenReturn(account);

        // Act
        AccountCatalogue result = searchService.getAccountCatalogueById(1L, entId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Debe delegar validación por ID a validationService")
    void testGetAccountCatalogueByIdDelegatesValidation() {
        // Arrange
        when(validationService.validateAccountExistsByIdAndEnterprise(1L, entId)).thenReturn(account);

        // Act
        searchService.getAccountCatalogueById(1L, entId);

        // Assert
        verify(validationService).validateAccountExistsByIdAndEnterprise(1L, entId);
    }

    // ========== Tests para getAuxiliaryAccounts ==========

    @Test
    @DisplayName("Debe obtener cuentas auxiliares exitosamente")
    void testGetAuxiliaryAccountsSuccess() {
        // Arrange
        List<AccountCatalogue> auxiliaries = List.of(account);
        when(accountCatalogueSearchOutputPort.getAuxiliaryAccountsByIdEnterprise(entId)).thenReturn(auxiliaries);

        // Act
        List<AccountCatalogue> result = searchService.getAuxiliaryAccounts(entId);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando idEnterprise es null en getAuxiliaryAccounts")
    void testGetAuxiliaryAccountsThrowsWhenIdEnterpriseNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getAuxiliaryAccounts(null));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando idEnterprise está vacío en getAuxiliaryAccounts")
    void testGetAuxiliaryAccountsThrowsWhenIdEnterpriseEmpty() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getAuxiliaryAccounts("  "));
    }

    @Test
    @DisplayName("Debe hacer trim al idEnterprise en getAuxiliaryAccounts")
    void testGetAuxiliaryAccountsTrimsIdEnterprise() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAuxiliaryAccountsByIdEnterprise(entId)).thenReturn(new ArrayList<>());

        // Act
        searchService.getAuxiliaryAccounts("  " + entId + "  ");

        // Assert
        verify(accountCatalogueSearchOutputPort).getAuxiliaryAccountsByIdEnterprise(entId);
    }

    // ========== Tests para getAuxiliaryAccountsWithCrossing ==========

    @Test
    @DisplayName("Debe obtener cuentas auxiliares con crossing exitosamente")
    void testGetAuxiliaryAccountsWithCrossingSuccess() {
        // Arrange
        AccountCatalogue crossingAccount = AccountCatalogue.builder()
                .id(1L)
                .code("11050101")
                .crossing(true)
                .build();
        List<AccountCatalogue> accounts = List.of(crossingAccount);
        when(accountCatalogueSearchOutputPort.getAuxiliaryAccountsWithCrossingByIdEnterprise(entId)).thenReturn(accounts);

        // Act
        List<AccountCatalogue> result = searchService.getAuxiliaryAccountsWithCrossing(entId);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0).getCrossing());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando idEnterprise es null en getAuxiliaryAccountsWithCrossing")
    void testGetAuxiliaryAccountsWithCrossingThrowsWhenIdEnterpriseNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getAuxiliaryAccountsWithCrossing(null));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando idEnterprise está vacío en getAuxiliaryAccountsWithCrossing")
    void testGetAuxiliaryAccountsWithCrossingThrowsWhenIdEnterpriseEmpty() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getAuxiliaryAccountsWithCrossing(""));
    }

    // ========== Tests para getAllAccountCatalogues ==========

    @Test
    @DisplayName("Debe obtener página de cuentas exitosamente")
    void testGetAllAccountCataloguesSuccess() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<AccountCatalogue> page = new PageImpl<>(List.of(account));
        when(accountCatalogueSearchOutputPort.getAllAccountCataloguesByIdEnterprise(entId, pageable)).thenReturn(page);

        // Act
        Page<AccountCatalogue> result = searchService.getAllAccountCatalogues(entId, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando idEnterprise es null en getAllAccountCatalogues")
    void testGetAllAccountCataloguesThrowsWhenIdEnterpriseNull() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getAllAccountCatalogues(null, pageable));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando idEnterprise está vacío en getAllAccountCatalogues")
    void testGetAllAccountCataloguesThrowsWhenIdEnterpriseEmpty() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getAllAccountCatalogues("   ", pageable));
    }

    @Test
    @DisplayName("Debe hacer trim al idEnterprise en getAllAccountCatalogues")
    void testGetAllAccountCataloguesTrimsIdEnterprise() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<AccountCatalogue> page = new PageImpl<>(new ArrayList<>());
        when(accountCatalogueSearchOutputPort.getAllAccountCataloguesByIdEnterprise(entId, pageable)).thenReturn(page);

        // Act
        searchService.getAllAccountCatalogues("  " + entId + "  ", pageable);

        // Assert
        verify(accountCatalogueSearchOutputPort).getAllAccountCataloguesByIdEnterprise(entId, pageable);
    }

    // ========== Tests para getAllAccountCataloguesByStatus ==========

    @Test
    @DisplayName("Debe obtener página de cuentas activas exitosamente")
    void testGetAllAccountCataloguesByStatusActiveSuccess() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<AccountCatalogue> page = new PageImpl<>(List.of(account));
        when(accountCatalogueSearchOutputPort.getAllAccountCataloguesByIdEnterpriseAndStatus(entId, true, pageable))
                .thenReturn(page);

        // Act
        Page<AccountCatalogue> result = searchService.getAllAccountCataloguesByStatus(entId, true, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Debe obtener página de cuentas inactivas exitosamente")
    void testGetAllAccountCataloguesByStatusInactiveSuccess() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        AccountCatalogue inactiveAccount = AccountCatalogue.builder()
                .id(2L)
                .code("11050102")
                .status(false)
                .build();
        Page<AccountCatalogue> page = new PageImpl<>(List.of(inactiveAccount));
        when(accountCatalogueSearchOutputPort.getAllAccountCataloguesByIdEnterpriseAndStatus(entId, false, pageable))
                .thenReturn(page);

        // Act
        Page<AccountCatalogue> result = searchService.getAllAccountCataloguesByStatus(entId, false, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando idEnterprise es null en getAllAccountCataloguesByStatus")
    void testGetAllAccountCataloguesByStatusThrowsWhenIdEnterpriseNull() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getAllAccountCataloguesByStatus(null, true, pageable));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando idEnterprise está vacío en getAllAccountCataloguesByStatus")
    void testGetAllAccountCataloguesByStatusThrowsWhenIdEnterpriseEmpty() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getAllAccountCataloguesByStatus("", true, pageable));
    }

    // ========== Tests para getAllAccountsByEnterprise ==========

    @Test
    @DisplayName("Debe obtener lista completa de cuentas exitosamente")
    void testGetAllAccountsByEnterpriseSuccess() {
        // Arrange
        List<AccountCatalogue> accounts = List.of(account);
        when(accountCatalogueSearchOutputPort.getAllAccountsByEnterprise(entId)).thenReturn(accounts);

        // Act
        List<AccountCatalogue> result = searchService.getAllAccountsByEnterprise(entId);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando idEnterprise es null en getAllAccountsByEnterprise")
    void testGetAllAccountsByEnterpriseThrowsWhenIdEnterpriseNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getAllAccountsByEnterprise(null));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando idEnterprise está vacío en getAllAccountsByEnterprise")
    void testGetAllAccountsByEnterpriseThrowsWhenIdEnterpriseEmpty() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getAllAccountsByEnterprise("   "));
    }

    @Test
    @DisplayName("Debe hacer trim al idEnterprise en getAllAccountsByEnterprise")
    void testGetAllAccountsByEnterpriseTrimsIdEnterprise() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAllAccountsByEnterprise(entId)).thenReturn(new ArrayList<>());

        // Act
        searchService.getAllAccountsByEnterprise("  " + entId + "  ");

        // Assert
        verify(accountCatalogueSearchOutputPort).getAllAccountsByEnterprise(entId);
    }

    // ========== Tests para getAccountsByCodeOrDescription ==========

    @Test
    @DisplayName("Debe buscar cuentas por código o descripción exitosamente")
    void testGetAccountsByCodeOrDescriptionSuccess() {
        // Arrange
        List<AccountCatalogue> accounts = List.of(account);
        when(accountCatalogueSearchOutputPort.getAccountsByCodeOrDescription(entId, "1105")).thenReturn(accounts);

        // Act
        List<AccountCatalogue> result = searchService.getAccountsByCodeOrDescription(entId, "1105");

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando idEnterprise es null en getAccountsByCodeOrDescription")
    void testGetAccountsByCodeOrDescriptionThrowsWhenIdEnterpriseNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getAccountsByCodeOrDescription(null, "search"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando idEnterprise está vacío en getAccountsByCodeOrDescription")
    void testGetAccountsByCodeOrDescriptionThrowsWhenIdEnterpriseEmpty() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getAccountsByCodeOrDescription("  ", "search"));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando search es null")
    void testGetAccountsByCodeOrDescriptionThrowsWhenSearchNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getAccountsByCodeOrDescription(entId, null));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando search está vacío")
    void testGetAccountsByCodeOrDescriptionThrowsWhenSearchEmpty() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getAccountsByCodeOrDescription(entId, "   "));
    }

    @Test
    @DisplayName("Debe hacer trim a idEnterprise y search en getAccountsByCodeOrDescription")
    void testGetAccountsByCodeOrDescriptionTrimsParameters() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountsByCodeOrDescription(entId, "caja")).thenReturn(new ArrayList<>());

        // Act
        searchService.getAccountsByCodeOrDescription("  " + entId + "  ", "  caja  ");

        // Assert
        verify(accountCatalogueSearchOutputPort).getAccountsByCodeOrDescription(entId, "caja");
    }

    @Test
    @DisplayName("Debe buscar por descripción parcial")
    void testGetAccountsByCodeOrDescriptionSearchesByDescription() {
        // Arrange
        List<AccountCatalogue> accounts = List.of(account);
        when(accountCatalogueSearchOutputPort.getAccountsByCodeOrDescription(entId, "prueba")).thenReturn(accounts);

        // Act
        List<AccountCatalogue> result = searchService.getAccountsByCodeOrDescription(entId, "prueba");

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0).getDescription().contains("prueba"));
    }

    // ========== Tests para getAllAccountCataloguesForExport ==========

    @Test
    @DisplayName("Debe lanzar excepción cuando idEnterprise es null en getAllAccountCataloguesForExport")
    void testGetAllAccountCataloguesForExportThrowsWhenIdEnterpriseNull() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getAllAccountCataloguesForExport(null, true, pageable));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando idEnterprise está vacío en getAllAccountCataloguesForExport")
    void testGetAllAccountCataloguesForExportThrowsWhenIdEnterpriseEmpty() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> searchService.getAllAccountCataloguesForExport("   ", true, pageable));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando idEnterprise tiene solo espacios en getAllAccountCataloguesForExport")
    void testGetAllAccountCataloguesForExportThrowsWhenIdEnterpriseWhitespace() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> searchService.getAllAccountCataloguesForExport("", null, pageable));
        assertTrue(exception.getMessage().contains("ID de empresa es requerido"));
    }
}
