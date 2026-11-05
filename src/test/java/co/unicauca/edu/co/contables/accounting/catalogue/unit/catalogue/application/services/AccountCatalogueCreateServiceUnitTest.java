package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueCreateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.AccountCatalogueCreateService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueAlreadyExistsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueDescriptionAlreadyExistsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.InvalidAccountCodeException;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueCreateServiceUnitTest {

    @Mock
    private IAccountCatalogueCreateOutputPort accountCatalogueCreateOutputPort;

    @Mock
    private AccountCatalogueValidationService validationService;

    @InjectMocks
    private AccountCatalogueCreateService createService;

    private String entId;
    private AccountCatalogue account;
    private AccountCatalogue createdAccount;

    @BeforeEach
    void setUp() {
        entId = "ENT-001";
        account = AccountCatalogue.builder()
                .code("11050101")
                .description("Cuenta de prueba")
                .idEnterprise(entId)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .crossing(false)
                .costCenter(false)
                .build();

        createdAccount = AccountCatalogue.builder()
                .id(1L)
                .code("11050101")
                .description("Cuenta de prueba")
                .idEnterprise(entId)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .crossing(false)
                .costCenter(false)
                .build();
    }

    @Test
    @DisplayName("Debe crear cuenta contable exitosamente")
    void testCreateAccountCatalogueSuccess() {
        // Arrange
        when(accountCatalogueCreateOutputPort.createAccountCatalogue(any())).thenReturn(createdAccount);

        // Act
        AccountCatalogue result = createService.createAccountCatalogue(account);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("11050101", result.getCode());
    }

    @Test
    @DisplayName("Debe hacer trim al código antes de validar")
    void testCreateAccountCatalogueTrimsCode() {
        // Arrange
        account.setCode("  11050101  ");
        when(accountCatalogueCreateOutputPort.createAccountCatalogue(any())).thenReturn(createdAccount);

        // Act
        createService.createAccountCatalogue(account);

        // Assert
        assertEquals("11050101", account.getCode());
    }

    @Test
    @DisplayName("Debe hacer trim a la descripción antes de validar")
    void testCreateAccountCatalogueTrimsDescription() {
        // Arrange
        account.setDescription("  Cuenta de prueba  ");
        when(accountCatalogueCreateOutputPort.createAccountCatalogue(any())).thenReturn(createdAccount);

        // Act
        createService.createAccountCatalogue(account);

        // Assert
        assertEquals("Cuenta de prueba", account.getDescription());
    }

    @Test
    @DisplayName("Debe validar código de cuenta")
    void testCreateAccountCatalogueValidatesCode() {
        // Arrange
        when(accountCatalogueCreateOutputPort.createAccountCatalogue(any())).thenReturn(createdAccount);

        // Act
        createService.createAccountCatalogue(account);

        // Assert
        verify(validationService).validateAccountCode("11050101");
    }

    @Test
    @DisplayName("Debe validar descripción de cuenta")
    void testCreateAccountCatalogueValidatesDescription() {
        // Arrange
        when(accountCatalogueCreateOutputPort.createAccountCatalogue(any())).thenReturn(createdAccount);

        // Act
        createService.createAccountCatalogue(account);

        // Assert
        verify(validationService).validateAccountDescription("Cuenta de prueba");
    }

    @Test
    @DisplayName("Debe validar que código no exista")
    void testCreateAccountCatalogueValidatesCodeDoesNotExist() {
        // Arrange
        when(accountCatalogueCreateOutputPort.createAccountCatalogue(any())).thenReturn(createdAccount);

        // Act
        createService.createAccountCatalogue(account);

        // Assert
        verify(validationService).validateAccountDoesNotExist("11050101", entId);
    }

    @Test
    @DisplayName("Debe validar que descripción no exista")
    void testCreateAccountCatalogueValidatesDescriptionDoesNotExist() {
        // Arrange
        when(accountCatalogueCreateOutputPort.createAccountCatalogue(any())).thenReturn(createdAccount);

        // Act
        createService.createAccountCatalogue(account);

        // Assert
        verify(validationService).validateAccountDescriptionDoesNotExist("Cuenta de prueba", entId);
    }

    @Test
    @DisplayName("Debe validar crossing y costCenter solo para cuentas auxiliares")
    void testCreateAccountCatalogueValidatesCrossingAndCostCenter() {
        // Arrange
        when(accountCatalogueCreateOutputPort.createAccountCatalogue(any())).thenReturn(createdAccount);

        // Act
        createService.createAccountCatalogue(account);

        // Assert
        verify(validationService).validateCrossingAndCostCenterOnlyForAuxiliaryAccounts(account);
    }

    @Test
    @DisplayName("Debe validar costCenter requiere Estado de Resultados")
    void testCreateAccountCatalogueValidatesCostCenterRequiresIncomeStatement() {
        // Arrange
        when(accountCatalogueCreateOutputPort.createAccountCatalogue(any())).thenReturn(createdAccount);

        // Act
        createService.createAccountCatalogue(account);

        // Assert
        verify(validationService).validateCostCenterRequiresIncomeStatement(account);
    }

    @Test
    @DisplayName("Debe validar existencia de padre cuando tiene parent con ID")
    void testCreateAccountCatalogueValidatesParentExists() {
        // Arrange
        AccountCatalogue parent = AccountCatalogue.builder().id(10L).build();
        account.setParent(parent);
        when(accountCatalogueCreateOutputPort.createAccountCatalogue(any())).thenReturn(createdAccount);

        // Act
        createService.createAccountCatalogue(account);

        // Assert
        verify(validationService).validateAccountExistsByIdAndEnterprise(10L, entId);
    }

    @Test
    @DisplayName("No debe validar existencia de padre cuando parent es null")
    void testCreateAccountCatalogueDoesNotValidateParentWhenNull() {
        // Arrange
        account.setParent(null);
        when(accountCatalogueCreateOutputPort.createAccountCatalogue(any())).thenReturn(createdAccount);

        // Act
        createService.createAccountCatalogue(account);

        // Assert
        verify(validationService, never()).validateAccountExistsByIdAndEnterprise(any(), any());
    }

    @Test
    @DisplayName("No debe validar existencia de padre cuando parent ID es null")
    void testCreateAccountCatalogueDoesNotValidateParentWhenIdNull() {
        // Arrange
        AccountCatalogue parent = AccountCatalogue.builder().id(null).build();
        account.setParent(parent);
        when(accountCatalogueCreateOutputPort.createAccountCatalogue(any())).thenReturn(createdAccount);

        // Act
        createService.createAccountCatalogue(account);

        // Assert
        verify(validationService, never()).validateAccountExistsByIdAndEnterprise(any(), any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código es inválido")
    void testCreateAccountCatalogueThrowsExceptionWhenCodeInvalid() {
        // Arrange
        doThrow(new InvalidAccountCodeException("Código inválido"))
                .when(validationService).validateAccountCode(any());

        // Act & Assert
        assertThrows(InvalidAccountCodeException.class,
                () -> createService.createAccountCatalogue(account));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código ya existe")
    void testCreateAccountCatalogueThrowsExceptionWhenCodeExists() {
        // Arrange
        doThrow(new AccountCatalogueAlreadyExistsException("Ya existe"))
                .when(validationService).validateAccountDoesNotExist(any(), any());

        // Act & Assert
        assertThrows(AccountCatalogueAlreadyExistsException.class,
                () -> createService.createAccountCatalogue(account));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando descripción ya existe")
    void testCreateAccountCatalogueThrowsExceptionWhenDescriptionExists() {
        // Arrange
        doThrow(new AccountCatalogueDescriptionAlreadyExistsException("Ya existe"))
                .when(validationService).validateAccountDescriptionDoesNotExist(any(), any());

        // Act & Assert
        assertThrows(AccountCatalogueDescriptionAlreadyExistsException.class,
                () -> createService.createAccountCatalogue(account));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando padre no existe")
    void testCreateAccountCatalogueThrowsExceptionWhenParentNotExists() {
        // Arrange
        AccountCatalogue parent = AccountCatalogue.builder().id(10L).build();
        account.setParent(parent);
        doThrow(new AccountCatalogueNotFoundException("Padre no existe"))
                .when(validationService).validateAccountExistsByIdAndEnterprise(10L, entId);

        // Act & Assert
        assertThrows(AccountCatalogueNotFoundException.class,
                () -> createService.createAccountCatalogue(account));
    }

    @Test
    @DisplayName("No debe invocar output port si validación falla")
    void testCreateAccountCatalogueDoesNotInvokeOutputPortOnValidationFailure() {
        // Arrange
        doThrow(new InvalidAccountCodeException("Código inválido"))
                .when(validationService).validateAccountCode(any());

        // Act
        try {
            createService.createAccountCatalogue(account);
        } catch (InvalidAccountCodeException e) {
            // Esperado
        }

        // Assert
        verify(accountCatalogueCreateOutputPort, never()).createAccountCatalogue(any());
    }

    @Test
    @DisplayName("Debe manejar código null sin hacer trim")
    void testCreateAccountCatalogueHandlesNullCode() {
        // Arrange
        account.setCode(null);
        doThrow(new InvalidAccountCodeException("Código vacío"))
                .when(validationService).validateAccountCode(null);

        // Act & Assert
        assertThrows(InvalidAccountCodeException.class,
                () -> createService.createAccountCatalogue(account));
    }

    @Test
    @DisplayName("Debe manejar descripción null sin hacer trim")
    void testCreateAccountCatalogueHandlesNullDescription() {
        // Arrange
        account.setDescription(null);
        doThrow(new InvalidAccountCodeException("Descripción vacía"))
                .when(validationService).validateAccountDescription(null);

        // Act & Assert
        assertThrows(InvalidAccountCodeException.class,
                () -> createService.createAccountCatalogue(account));
    }

    // ========== Tests para createAllAccountCatalogues ==========

    @Test
    @DisplayName("Debe crear múltiples cuentas en batch")
    void testCreateAllAccountCataloguesSuccess() {
        // Arrange
        List<AccountCatalogue> accounts = new ArrayList<>();
        accounts.add(account);
        accounts.add(AccountCatalogue.builder()
                .code("11050102")
                .description("Otra cuenta")
                .idEnterprise(entId)
                .build());

        List<AccountCatalogue> createdAccounts = new ArrayList<>();
        createdAccounts.add(createdAccount);
        createdAccounts.add(AccountCatalogue.builder()
                .id(2L)
                .code("11050102")
                .description("Otra cuenta")
                .idEnterprise(entId)
                .build());

        when(accountCatalogueCreateOutputPort.createAllAccountCatalogues(accounts)).thenReturn(createdAccounts);

        // Act
        List<AccountCatalogue> result = createService.createAllAccountCatalogues(accounts);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Debe delegar directamente al output port sin validaciones")
    void testCreateAllAccountCataloguesDelegatesDirectly() {
        // Arrange
        List<AccountCatalogue> accounts = new ArrayList<>();
        accounts.add(account);
        when(accountCatalogueCreateOutputPort.createAllAccountCatalogues(accounts)).thenReturn(List.of(createdAccount));

        // Act
        createService.createAllAccountCatalogues(accounts);

        // Assert
        verify(accountCatalogueCreateOutputPort).createAllAccountCatalogues(accounts);
        verifyNoInteractions(validationService);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando entrada es vacía")
    void testCreateAllAccountCataloguesEmptyList() {
        // Arrange
        List<AccountCatalogue> emptyList = new ArrayList<>();
        when(accountCatalogueCreateOutputPort.createAllAccountCatalogues(emptyList)).thenReturn(new ArrayList<>());

        // Act
        List<AccountCatalogue> result = createService.createAllAccountCatalogues(emptyList);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
