package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueDeleteOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.AccountCatalogueDeleteService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueHasChildrenException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueInUseException;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueDeleteServiceUnitTest {

    @Mock
    private IAccountCatalogueDeleteOutputPort accountCatalogueDeleteOutputPort;

    @Mock
    private IAccountCatalogueSearchOutputPort accountCatalogueSearchOutputPort;

    @Mock
    private AccountCatalogueValidationService validationService;

    @InjectMocks
    private AccountCatalogueDeleteService deleteService;

    private String entId;
    private Long accountId;
    private AccountCatalogue account;

    @BeforeEach
    void setUp() {
        entId = "ENT-001";
        accountId = 1L;
        account = AccountCatalogue.builder()
                .id(accountId)
                .code("11050101")
                .description("Cuenta de prueba")
                .idEnterprise(entId)
                .children(new ArrayList<>())
                .build();
    }

    // ========== Tests de eliminación exitosa ==========

    @Test
    @DisplayName("Debe eliminar cuenta exitosamente cuando cumple todas las validaciones")
    void testDeleteByIdSuccess() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(account);

        // Act
        deleteService.deleteById(accountId, entId);

        // Assert
        verify(accountCatalogueDeleteOutputPort).deleteById(accountId);
    }

    @Test
    @DisplayName("Debe buscar árbol completo de cuenta antes de eliminar")
    void testDeleteByIdSearchesAccountTree() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(account);

        // Act
        deleteService.deleteById(accountId, entId);

        // Assert
        verify(accountCatalogueSearchOutputPort).getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId);
    }

    @Test
    @DisplayName("Debe validar que cuenta no esté asociada a impuestos")
    void testDeleteByIdValidatesTaxAssociation() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(account);

        // Act
        deleteService.deleteById(accountId, entId);

        // Assert
        verify(validationService).validateAccountAndChildrenNotAssociatedWithTaxes(account);
    }

    @Test
    @DisplayName("Debe validar que cuenta no esté asociada a cuentas bancarias")
    void testDeleteByIdValidatesBankAccountAssociation() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(account);

        // Act
        deleteService.deleteById(accountId, entId);

        // Assert
        verify(validationService).validateAccountAndChildrenNotAssociatedWithBankAccounts(account);
    }

    @Test
    @DisplayName("Debe validar que cuenta no esté asociada a métodos de pago")
    void testDeleteByIdValidatesPaymentMethodAssociation() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(account);

        // Act
        deleteService.deleteById(accountId, entId);

        // Assert
        verify(validationService).validateAccountAndChildrenNotAssociatedWithPaymentMethods(account);
    }

    // ========== Tests de cuenta no encontrada ==========

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta no existe")
    void testDeleteByIdThrowsExceptionWhenAccountNotFound() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(null);

        // Act & Assert
        AccountCatalogueNotFoundException exception = assertThrows(
                AccountCatalogueNotFoundException.class,
                () -> deleteService.deleteById(accountId, entId)
        );
        assertTrue(exception.getMessage().contains(accountId.toString()));
    }

    @Test
    @DisplayName("No debe invocar delete cuando cuenta no existe")
    void testDeleteByIdDoesNotDeleteWhenAccountNotFound() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(null);

        // Act
        try {
            deleteService.deleteById(accountId, entId);
        } catch (AccountCatalogueNotFoundException e) {
            // Esperado
        }

        // Assert
        verify(accountCatalogueDeleteOutputPort, never()).deleteById(any());
    }

    // ========== Tests de cuenta en uso ==========

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta está en uso")
    void testDeleteByIdThrowsExceptionWhenAccountInUse() {
        // Arrange
        account.setUsageCount(5);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(account);

        // Act & Assert
        assertThrows(AccountCatalogueInUseException.class,
                () -> deleteService.deleteById(accountId, entId));
    }

    @Test
    @DisplayName("No debe invocar delete cuando cuenta está en uso")
    void testDeleteByIdDoesNotDeleteWhenAccountInUse() {
        // Arrange
        account.setUsageCount(1);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(account);

        // Act
        try {
            deleteService.deleteById(accountId, entId);
        } catch (AccountCatalogueInUseException e) {
            // Esperado
        }

        // Assert
        verify(accountCatalogueDeleteOutputPort, never()).deleteById(any());
    }

    // ========== Tests de cuenta con hijos ==========

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta tiene hijos")
    void testDeleteByIdThrowsExceptionWhenAccountHasChildren() {
        // Arrange
        AccountCatalogue child = AccountCatalogue.builder()
                .id(2L)
                .code("1105010101")
                .build();
        account.setChildren(List.of(child));
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(account);

        // Act & Assert
        AccountCatalogueHasChildrenException exception = assertThrows(
                AccountCatalogueHasChildrenException.class,
                () -> deleteService.deleteById(accountId, entId)
        );
        assertTrue(exception.getMessage().contains(account.getCode()));
    }

    @Test
    @DisplayName("No debe invocar delete cuando cuenta tiene hijos")
    void testDeleteByIdDoesNotDeleteWhenAccountHasChildren() {
        // Arrange
        AccountCatalogue child = AccountCatalogue.builder()
                .id(2L)
                .code("1105010101")
                .build();
        account.setChildren(List.of(child));
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(account);

        // Act
        try {
            deleteService.deleteById(accountId, entId);
        } catch (AccountCatalogueHasChildrenException e) {
            // Esperado
        }

        // Assert
        verify(accountCatalogueDeleteOutputPort, never()).deleteById(any());
    }

    @Test
    @DisplayName("Debe permitir eliminar cuando lista de hijos es vacía")
    void testDeleteByIdAllowsWhenChildrenListEmpty() {
        // Arrange
        account.setChildren(new ArrayList<>());
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(account);

        // Act
        deleteService.deleteById(accountId, entId);

        // Assert
        verify(accountCatalogueDeleteOutputPort).deleteById(accountId);
    }

    @Test
    @DisplayName("Debe permitir eliminar cuando lista de hijos es null")
    void testDeleteByIdAllowsWhenChildrenListNull() {
        // Arrange
        account.setChildren(null);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(account);

        // Act
        deleteService.deleteById(accountId, entId);

        // Assert
        verify(accountCatalogueDeleteOutputPort).deleteById(accountId);
    }

    // ========== Tests de validaciones de asociaciones ==========

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta está asociada a impuestos")
    void testDeleteByIdThrowsExceptionWhenAssociatedWithTaxes() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(account);
        doThrow(new RuntimeException("Cuenta asociada a impuestos"))
                .when(validationService).validateAccountAndChildrenNotAssociatedWithTaxes(account);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> deleteService.deleteById(accountId, entId));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta está asociada a cuentas bancarias")
    void testDeleteByIdThrowsExceptionWhenAssociatedWithBankAccounts() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(account);
        doThrow(new RuntimeException("Cuenta asociada a cuentas bancarias"))
                .when(validationService).validateAccountAndChildrenNotAssociatedWithBankAccounts(account);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> deleteService.deleteById(accountId, entId));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta está asociada a métodos de pago")
    void testDeleteByIdThrowsExceptionWhenAssociatedWithPaymentMethods() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(account);
        doThrow(new RuntimeException("Cuenta asociada a métodos de pago"))
                .when(validationService).validateAccountAndChildrenNotAssociatedWithPaymentMethods(account);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> deleteService.deleteById(accountId, entId));
    }

    @Test
    @DisplayName("No debe invocar delete cuando validación de impuestos falla")
    void testDeleteByIdDoesNotDeleteWhenTaxValidationFails() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(account);
        doThrow(new RuntimeException("Cuenta asociada a impuestos"))
                .when(validationService).validateAccountAndChildrenNotAssociatedWithTaxes(account);

        // Act
        try {
            deleteService.deleteById(accountId, entId);
        } catch (RuntimeException e) {
            // Esperado
        }

        // Assert
        verify(accountCatalogueDeleteOutputPort, never()).deleteById(any());
    }

    @Test
    @DisplayName("No debe invocar delete cuando validación de cuentas bancarias falla")
    void testDeleteByIdDoesNotDeleteWhenBankAccountValidationFails() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(account);
        doThrow(new RuntimeException("Cuenta asociada a cuentas bancarias"))
                .when(validationService).validateAccountAndChildrenNotAssociatedWithBankAccounts(account);

        // Act
        try {
            deleteService.deleteById(accountId, entId);
        } catch (RuntimeException e) {
            // Esperado
        }

        // Assert
        verify(accountCatalogueDeleteOutputPort, never()).deleteById(any());
    }

    @Test
    @DisplayName("No debe invocar delete cuando validación de métodos de pago falla")
    void testDeleteByIdDoesNotDeleteWhenPaymentMethodValidationFails() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(account);
        doThrow(new RuntimeException("Cuenta asociada a métodos de pago"))
                .when(validationService).validateAccountAndChildrenNotAssociatedWithPaymentMethods(account);

        // Act
        try {
            deleteService.deleteById(accountId, entId);
        } catch (RuntimeException e) {
            // Esperado
        }

        // Assert
        verify(accountCatalogueDeleteOutputPort, never()).deleteById(any());
    }

    // ========== Tests de orden de validaciones ==========

    @Test
    @DisplayName("Debe validar existencia antes que uso")
    void testDeleteByIdValidatesExistenceBeforeUsage() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(null);

        // Act & Assert
        assertThrows(AccountCatalogueNotFoundException.class,
                () -> deleteService.deleteById(accountId, entId));
        
        verifyNoInteractions(validationService);
    }

    @Test
    @DisplayName("Debe validar uso antes que hijos")
    void testDeleteByIdValidatesUsageBeforeChildren() {
        // Arrange
        account.setUsageCount(5);
        AccountCatalogue child = AccountCatalogue.builder().id(2L).build();
        account.setChildren(List.of(child));
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(account);

        // Act & Assert
        assertThrows(AccountCatalogueInUseException.class,
                () -> deleteService.deleteById(accountId, entId));
    }

    @Test
    @DisplayName("Debe validar hijos antes que asociaciones")
    void testDeleteByIdValidatesChildrenBeforeAssociations() {
        // Arrange
        AccountCatalogue child = AccountCatalogue.builder().id(2L).build();
        account.setChildren(List.of(child));
        when(accountCatalogueSearchOutputPort.getAccountCatalogueTreeByIdAndIdEnterprise(accountId, entId))
                .thenReturn(account);

        // Act & Assert
        assertThrows(AccountCatalogueHasChildrenException.class,
                () -> deleteService.deleteById(accountId, entId));
        
        verifyNoInteractions(validationService);
    }
}
