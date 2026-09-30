package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueChangeStateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.AccountCatalogueChangeStateService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueChangeStateServiceUnitTest {

    @Mock
    private IAccountCatalogueChangeStateOutputPort accountCatalogueChangeStateOutputPort;

    @Mock
    private AccountCatalogueValidationService validationService;

    @InjectMocks
    private AccountCatalogueChangeStateService changeStateService;

    private Long accountId;
    private String entId;
    private AccountCatalogue account;

    @BeforeEach
    void setUp() {
        accountId = 1L;
        entId = "ENT-001";
        account = AccountCatalogue.builder()
                .id(accountId)
                .code("11050101")
                .description("Cuenta de prueba")
                .idEnterprise(entId)
                .status(true)
                .build();
    }

    @Test
    @DisplayName("Debe activar cuenta correctamente")
    void testChangeStateToActiveSuccess() {
        // Arrange
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(account);
        when(accountCatalogueChangeStateOutputPort.changeState(accountId, true)).thenReturn(account);

        // Act
        AccountCatalogue result = changeStateService.changeState(accountId, entId, true);

        // Assert
        assertNotNull(result);
        assertEquals(accountId, result.getId());
    }

    @Test
    @DisplayName("Debe desactivar cuenta correctamente")
    void testChangeStateToInactiveSuccess() {
        // Arrange
        account.setStatus(false);
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(account);
        when(accountCatalogueChangeStateOutputPort.changeState(accountId, false)).thenReturn(account);

        // Act
        AccountCatalogue result = changeStateService.changeState(accountId, entId, false);

        // Assert
        assertNotNull(result);
        assertEquals(accountId, result.getId());
    }

    @Test
    @DisplayName("Debe validar existencia de cuenta antes de cambiar estado")
    void testChangeStateValidatesAccountExists() {
        // Arrange
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(account);
        when(accountCatalogueChangeStateOutputPort.changeState(accountId, true)).thenReturn(account);

        // Act
        changeStateService.changeState(accountId, entId, true);

        // Assert
        verify(validationService).validateAccountExistsByIdAndEnterprise(accountId, entId);
    }

    @Test
    @DisplayName("Debe invocar output port con parámetros correctos")
    void testChangeStateInvokesOutputPortWithCorrectParams() {
        // Arrange
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(account);
        when(accountCatalogueChangeStateOutputPort.changeState(accountId, true)).thenReturn(account);

        // Act
        changeStateService.changeState(accountId, entId, true);

        // Assert
        verify(accountCatalogueChangeStateOutputPort).changeState(accountId, true);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando status es null")
    void testChangeStateThrowsExceptionWhenStatusNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> changeStateService.changeState(accountId, entId, null));

        assertEquals("El parámetro 'status' es requerido", exception.getMessage());
    }

    @Test
    @DisplayName("No debe invocar output port cuando status es null")
    void testChangeStateDoesNotInvokeOutputPortWhenStatusNull() {
        // Act
        try {
            changeStateService.changeState(accountId, entId, null);
        } catch (IllegalArgumentException e) {
            // Esperado
        }

        // Assert
        verify(accountCatalogueChangeStateOutputPort, never()).changeState(any(), any());
    }

    @Test
    @DisplayName("No debe validar existencia cuando status es null")
    void testChangeStateDoesNotValidateWhenStatusNull() {
        // Act
        try {
            changeStateService.changeState(accountId, entId, null);
        } catch (IllegalArgumentException e) {
            // Esperado
        }

        // Assert
        verify(validationService, never()).validateAccountExistsByIdAndEnterprise(any(), any());
    }

    @Test
    @DisplayName("Debe propagar excepción cuando cuenta no existe")
    void testChangeStateThrowsExceptionWhenAccountNotFound() {
        // Arrange
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId))
                .thenThrow(new AccountCatalogueNotFoundException("No se encontró la cuenta"));

        // Act & Assert
        assertThrows(AccountCatalogueNotFoundException.class,
                () -> changeStateService.changeState(accountId, entId, true));
    }

    @Test
    @DisplayName("No debe invocar output port cuando cuenta no existe")
    void testChangeStateDoesNotInvokeOutputPortWhenAccountNotFound() {
        // Arrange
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId))
                .thenThrow(new AccountCatalogueNotFoundException("No se encontró la cuenta"));

        // Act
        try {
            changeStateService.changeState(accountId, entId, true);
        } catch (AccountCatalogueNotFoundException e) {
            // Esperado
        }

        // Assert
        verify(accountCatalogueChangeStateOutputPort, never()).changeState(any(), any());
    }

    @Test
    @DisplayName("Debe retornar cuenta actualizada del output port")
    void testChangeStateReturnsUpdatedAccount() {
        // Arrange
        AccountCatalogue updatedAccount = AccountCatalogue.builder()
                .id(accountId)
                .code("11050101")
                .description("Cuenta de prueba")
                .idEnterprise(entId)
                .status(false)
                .build();
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(account);
        when(accountCatalogueChangeStateOutputPort.changeState(accountId, false)).thenReturn(updatedAccount);

        // Act
        AccountCatalogue result = changeStateService.changeState(accountId, entId, false);

        // Assert
        assertNotNull(result);
        assertFalse(result.getStatus());
    }

    @Test
    @DisplayName("Debe manejar cambio de estado para cuenta con hijos")
    void testChangeStateAccountWithChildren() {
        // Arrange
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(account);
        when(accountCatalogueChangeStateOutputPort.changeState(accountId, true)).thenReturn(account);

        // Act
        AccountCatalogue result = changeStateService.changeState(accountId, entId, true);

        // Assert
        assertNotNull(result);
        verify(accountCatalogueChangeStateOutputPort).changeState(accountId, true);
    }
}
