package co.unicauca.edu.co.contables.accounting.catalogue.unit.catalogue.application.services;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueUpdateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.AccountCatalogueUpdateService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.services.validation.AccountCatalogueValidationService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueAlreadyExistsException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue.AccountCatalogueInUseException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCatalogueUpdateServiceUnitTest {

    @Mock
    private IAccountCatalogueUpdateOutputPort accountCatalogueUpdateOutputport;

    @Mock
    private AccountCatalogueValidationService validationService;

    @InjectMocks
    private AccountCatalogueUpdateService updateService;

    private String entId;
    private Long accountId;
    private AccountCatalogue existingAccount;
    private AccountCatalogue updateAccount;
    private AccountCatalogue updatedAccount;

    @BeforeEach
    void setUp() {
        entId = "ENT-001";
        accountId = 1L;

        existingAccount = AccountCatalogue.builder()
                .id(accountId)
                .code("11050101")
                .description("Cuenta existente")
                .idEnterprise(entId)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .crossing(false)
                .costCenter(false)
                .usageCount(0)
                .build();

        updateAccount = AccountCatalogue.builder()
                .code("11050101")
                .description("Cuenta actualizada")
                .idEnterprise(entId)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .crossing(true)
                .costCenter(true)
                .build();

        updatedAccount = AccountCatalogue.builder()
                .id(accountId)
                .code("11050101")
                .description("Cuenta actualizada")
                .idEnterprise(entId)
                .financialStatus(FinancialStatusEnum.INCOMESTATEMENT)
                .crossing(true)
                .costCenter(true)
                .build();
    }

    // ========== Tests de actualización exitosa ==========

    @Test
    @DisplayName("Debe actualizar cuenta exitosamente")
    void testUpdateAccountCatalogueSuccess() {
        // Arrange
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);
        when(accountCatalogueUpdateOutputport.updateAccountCatalogue(accountId, updateAccount)).thenReturn(updatedAccount);

        // Act
        AccountCatalogue result = updateService.updateAccountCatalogue(accountId, updateAccount);

        // Assert
        assertNotNull(result);
        assertEquals("Cuenta actualizada", result.getDescription());
    }

    @Test
    @DisplayName("Debe invocar output port con datos correctos")
    void testUpdateAccountCatalogueInvokesOutputPort() {
        // Arrange
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);
        when(accountCatalogueUpdateOutputport.updateAccountCatalogue(accountId, updateAccount)).thenReturn(updatedAccount);

        // Act
        updateService.updateAccountCatalogue(accountId, updateAccount);

        // Assert
        verify(accountCatalogueUpdateOutputport).updateAccountCatalogue(accountId, updateAccount);
    }

    // ========== Tests de validación de existencia ==========

    @Test
    @DisplayName("Debe validar que cuenta existe antes de actualizar")
    void testUpdateAccountCatalogueValidatesExistence() {
        // Arrange
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);
        when(accountCatalogueUpdateOutputport.updateAccountCatalogue(accountId, updateAccount)).thenReturn(updatedAccount);

        // Act
        updateService.updateAccountCatalogue(accountId, updateAccount);

        // Assert
        verify(validationService).validateAccountExistsByIdAndEnterprise(accountId, entId);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta no existe")
    void testUpdateAccountCatalogueThrowsWhenNotFound() {
        // Arrange
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId))
                .thenThrow(new AccountCatalogueNotFoundException("No encontrada"));

        // Act & Assert
        assertThrows(AccountCatalogueNotFoundException.class,
                () -> updateService.updateAccountCatalogue(accountId, updateAccount));
    }

    // ========== Tests de cuenta en uso ==========

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta está en uso")
    void testUpdateAccountCatalogueThrowsWhenInUse() {
        // Arrange
        existingAccount.setUsageCount(5);
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);

        // Act & Assert
        assertThrows(AccountCatalogueInUseException.class,
                () -> updateService.updateAccountCatalogue(accountId, updateAccount));
    }

    @Test
    @DisplayName("No debe invocar output port cuando cuenta está en uso")
    void testUpdateAccountCatalogueDoesNotUpdateWhenInUse() {
        // Arrange
        existingAccount.setUsageCount(1);
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);

        // Act
        try {
            updateService.updateAccountCatalogue(accountId, updateAccount);
        } catch (AccountCatalogueInUseException e) {
            // Esperado
        }

        // Assert
        verify(accountCatalogueUpdateOutputport, never()).updateAccountCatalogue(anyLong(), any());
    }

    // ========== Tests de validación de idEnterprise ==========

    @Test
    @DisplayName("Debe lanzar excepción cuando idEnterprise es null")
    void testUpdateAccountCatalogueThrowsWhenIdEnterpriseNull() {
        // Arrange
        updateAccount.setIdEnterprise(null);
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, null)).thenReturn(existingAccount);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> updateService.updateAccountCatalogue(accountId, updateAccount));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando idEnterprise está vacío")
    void testUpdateAccountCatalogueThrowsWhenIdEnterpriseEmpty() {
        // Arrange
        updateAccount.setIdEnterprise("   ");
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, "   ")).thenReturn(existingAccount);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> updateService.updateAccountCatalogue(accountId, updateAccount));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando idEnterprise no coincide")
    void testUpdateAccountCatalogueThrowsWhenIdEnterpriseMismatch() {
        // Arrange
        updateAccount.setIdEnterprise("ENT-002");
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, "ENT-002")).thenReturn(existingAccount);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> updateService.updateAccountCatalogue(accountId, updateAccount));
        assertTrue(exception.getMessage().contains("no coincide"));
    }

    // ========== Tests de validación de código ==========

    @Test
    @DisplayName("Debe hacer trim al código antes de validar")
    void testUpdateAccountCatalogueTrimsCode() {
        // Arrange
        updateAccount.setCode("  11050101  ");
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);
        when(accountCatalogueUpdateOutputport.updateAccountCatalogue(eq(accountId), any())).thenReturn(updatedAccount);

        // Act
        updateService.updateAccountCatalogue(accountId, updateAccount);

        // Assert
        assertEquals("11050101", updateAccount.getCode());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código es null")
    void testUpdateAccountCatalogueThrowsWhenCodeNull() {
        // Arrange
        updateAccount.setCode(null);
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> updateService.updateAccountCatalogue(accountId, updateAccount));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código está vacío")
    void testUpdateAccountCatalogueThrowsWhenCodeEmpty() {
        // Arrange
        updateAccount.setCode("   ");
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> updateService.updateAccountCatalogue(accountId, updateAccount));
    }

    @Test
    @DisplayName("Debe validar formato de código")
    void testUpdateAccountCatalogueValidatesCodeFormat() {
        // Arrange
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);
        when(accountCatalogueUpdateOutputport.updateAccountCatalogue(accountId, updateAccount)).thenReturn(updatedAccount);

        // Act
        updateService.updateAccountCatalogue(accountId, updateAccount);

        // Assert
        verify(validationService).validateAccountCode("11050101");
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando formato de código es inválido")
    void testUpdateAccountCatalogueThrowsWhenCodeFormatInvalid() {
        // Arrange
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);
        doThrow(new InvalidAccountCodeException("Código inválido"))
                .when(validationService).validateAccountCode(any());

        // Act & Assert
        assertThrows(InvalidAccountCodeException.class,
                () -> updateService.updateAccountCatalogue(accountId, updateAccount));
    }

    // ========== Tests de validación de descripción ==========

    @Test
    @DisplayName("Debe hacer trim a la descripción antes de validar")
    void testUpdateAccountCatalogueTrimsDescription() {
        // Arrange
        updateAccount.setDescription("  Cuenta actualizada  ");
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);
        when(accountCatalogueUpdateOutputport.updateAccountCatalogue(eq(accountId), any())).thenReturn(updatedAccount);

        // Act
        updateService.updateAccountCatalogue(accountId, updateAccount);

        // Assert
        assertEquals("Cuenta actualizada", updateAccount.getDescription());
    }

    @Test
    @DisplayName("Debe validar descripción")
    void testUpdateAccountCatalogueValidatesDescription() {
        // Arrange
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);
        when(accountCatalogueUpdateOutputport.updateAccountCatalogue(accountId, updateAccount)).thenReturn(updatedAccount);

        // Act
        updateService.updateAccountCatalogue(accountId, updateAccount);

        // Assert
        verify(validationService).validateAccountDescription("Cuenta actualizada");
    }

    // ========== Tests de duplicados excluyendo cuenta actual ==========

    @Test
    @DisplayName("Debe validar que código no existe excluyendo cuenta actual")
    void testUpdateAccountCatalogueValidatesCodeDoesNotExistExcluding() {
        // Arrange
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);
        when(accountCatalogueUpdateOutputport.updateAccountCatalogue(accountId, updateAccount)).thenReturn(updatedAccount);

        // Act
        updateService.updateAccountCatalogue(accountId, updateAccount);

        // Assert
        verify(validationService).validateAccountDoesNotExistExcluding("11050101", entId, accountId);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando código ya existe en otra cuenta")
    void testUpdateAccountCatalogueThrowsWhenCodeExistsInOtherAccount() {
        // Arrange
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);
        doThrow(new AccountCatalogueAlreadyExistsException("Ya existe"))
                .when(validationService).validateAccountDoesNotExistExcluding(any(), any(), any());

        // Act & Assert
        assertThrows(AccountCatalogueAlreadyExistsException.class,
                () -> updateService.updateAccountCatalogue(accountId, updateAccount));
    }

    @Test
    @DisplayName("Debe validar que descripción no existe excluyendo cuenta actual")
    void testUpdateAccountCatalogueValidatesDescriptionDoesNotExistExcluding() {
        // Arrange
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);
        when(accountCatalogueUpdateOutputport.updateAccountCatalogue(accountId, updateAccount)).thenReturn(updatedAccount);

        // Act
        updateService.updateAccountCatalogue(accountId, updateAccount);

        // Assert
        verify(validationService).validateAccountDescriptionDoesNotExistExcluding("Cuenta actualizada", entId, accountId);
    }

    // ========== Tests de validación de prefijo de padre ==========

    @Test
    @DisplayName("Debe validar prefijo de padre cuando código cambia y tiene padre")
    void testUpdateAccountCatalogueValidatesParentPrefixWhenCodeChanges() {
        // Arrange
        AccountCatalogue parent = AccountCatalogue.builder()
                .id(10L)
                .code("1105010")
                .build();
        existingAccount.setParent(parent);
        updateAccount.setCode("11050102");
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);
        when(accountCatalogueUpdateOutputport.updateAccountCatalogue(eq(accountId), any())).thenReturn(updatedAccount);

        // Act
        updateService.updateAccountCatalogue(accountId, updateAccount);

        // Assert
        verify(validationService).validateParentCodePrefix("11050102", "1105010");
    }

    @Test
    @DisplayName("No debe validar prefijo de padre cuando código no cambia")
    void testUpdateAccountCatalogueSkipsParentPrefixValidationWhenCodeUnchanged() {
        // Arrange
        AccountCatalogue parent = AccountCatalogue.builder()
                .id(10L)
                .code("1105010")
                .build();
        existingAccount.setParent(parent);
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);
        when(accountCatalogueUpdateOutputport.updateAccountCatalogue(accountId, updateAccount)).thenReturn(updatedAccount);

        // Act
        updateService.updateAccountCatalogue(accountId, updateAccount);

        // Assert
        verify(validationService, never()).validateParentCodePrefix(any(), any());
    }

    @Test
    @DisplayName("No debe validar prefijo de padre cuando cuenta no tiene padre")
    void testUpdateAccountCatalogueSkipsParentPrefixValidationWhenNoParent() {
        // Arrange
        existingAccount.setParent(null);
        updateAccount.setCode("11050102");
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);
        when(accountCatalogueUpdateOutputport.updateAccountCatalogue(eq(accountId), any())).thenReturn(updatedAccount);

        // Act
        updateService.updateAccountCatalogue(accountId, updateAccount);

        // Assert
        verify(validationService, never()).validateParentCodePrefix(any(), any());
    }

    // ========== Tests de validación de padre ==========

    @Test
    @DisplayName("Debe validar existencia de padre cuando se proporciona")
    void testUpdateAccountCatalogueValidatesParentExists() {
        // Arrange
        AccountCatalogue newParent = AccountCatalogue.builder().id(20L).build();
        updateAccount.setParent(newParent);
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);
        when(validationService.validateAccountExistsByIdAndEnterprise(20L, entId)).thenReturn(newParent);
        when(accountCatalogueUpdateOutputport.updateAccountCatalogue(accountId, updateAccount)).thenReturn(updatedAccount);

        // Act
        updateService.updateAccountCatalogue(accountId, updateAccount);

        // Assert
        verify(validationService).validateAccountExistsByIdAndEnterprise(20L, entId);
    }

    @Test
    @DisplayName("No debe validar padre cuando parent es null")
    void testUpdateAccountCatalogueSkipsParentValidationWhenNull() {
        // Arrange
        updateAccount.setParent(null);
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);
        when(accountCatalogueUpdateOutputport.updateAccountCatalogue(accountId, updateAccount)).thenReturn(updatedAccount);

        // Act
        updateService.updateAccountCatalogue(accountId, updateAccount);

        // Assert
        verify(validationService, times(1)).validateAccountExistsByIdAndEnterprise(any(), any());
    }

    @Test
    @DisplayName("No debe validar padre cuando parent ID es null")
    void testUpdateAccountCatalogueSkipsParentValidationWhenParentIdNull() {
        // Arrange
        AccountCatalogue parentWithNullId = AccountCatalogue.builder().id(null).build();
        updateAccount.setParent(parentWithNullId);
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);
        when(accountCatalogueUpdateOutputport.updateAccountCatalogue(accountId, updateAccount)).thenReturn(updatedAccount);

        // Act
        updateService.updateAccountCatalogue(accountId, updateAccount);

        // Assert
        verify(validationService, times(1)).validateAccountExistsByIdAndEnterprise(any(), any());
    }

    // ========== Tests de validación de crossing y costCenter ==========

    @Test
    @DisplayName("Debe validar crossing y costCenter solo para cuentas auxiliares")
    void testUpdateAccountCatalogueValidatesCrossingAndCostCenter() {
        // Arrange
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);
        when(accountCatalogueUpdateOutputport.updateAccountCatalogue(accountId, updateAccount)).thenReturn(updatedAccount);

        // Act
        updateService.updateAccountCatalogue(accountId, updateAccount);

        // Assert
        verify(validationService).validateCrossingAndCostCenterOnlyForAuxiliaryAccounts(updateAccount);
    }

    @Test
    @DisplayName("Debe validar que costCenter requiere Estado de Resultados")
    void testUpdateAccountCatalogueValidatesCostCenterRequiresIncomeStatement() {
        // Arrange
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);
        when(accountCatalogueUpdateOutputport.updateAccountCatalogue(accountId, updateAccount)).thenReturn(updatedAccount);

        // Act
        updateService.updateAccountCatalogue(accountId, updateAccount);

        // Assert
        verify(validationService).validateCostCenterRequiresIncomeStatement(updateAccount);
    }

    // ========== Tests de orden de validaciones ==========

    @Test
    @DisplayName("Debe validar existencia antes que uso")
    void testUpdateAccountCatalogueValidatesExistenceBeforeUsage() {
        // Arrange
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId))
                .thenThrow(new AccountCatalogueNotFoundException("No encontrada"));

        // Act & Assert
        assertThrows(AccountCatalogueNotFoundException.class,
                () -> updateService.updateAccountCatalogue(accountId, updateAccount));
        
        verify(accountCatalogueUpdateOutputport, never()).updateAccountCatalogue(anyLong(), any());
    }

    @Test
    @DisplayName("No debe invocar validaciones cuando cuenta está en uso")
    void testUpdateAccountCatalogueStopsValidationsWhenInUse() {
        // Arrange
        existingAccount.setUsageCount(1);
        when(validationService.validateAccountExistsByIdAndEnterprise(accountId, entId)).thenReturn(existingAccount);

        // Act
        try {
            updateService.updateAccountCatalogue(accountId, updateAccount);
        } catch (AccountCatalogueInUseException e) {
            // Esperado
        }

        // Assert
        verify(validationService, never()).validateAccountCode(any());
        verify(validationService, never()).validateAccountDescription(any());
    }
}
