package co.unicauca.edu.co.contables.accounting.catalogue.unit.bankAccounts.domain.services;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.enums.AccountType;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.model.BankAccount;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.services.BankAccountUsageService;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.services.IBankAccountService;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.domain.model.Bank;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.bankAccounts.BankAccountNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BankAccountUsageServiceUnitTest {

    @Mock
    private IBankAccountService bankAccountService;

    @InjectMocks
    private BankAccountUsageService usageService;

    private BankAccount bankAccount;
    private String enterpriseId;
    private Long bankAccountId;

    @BeforeEach
    void setUp() {
        enterpriseId = "ENT-001";
        bankAccountId = 1L;

        Bank bank = Bank.builder()
                .id(1L)
                .code("001")
                .name("Banco Test")
                .status(true)
                .idEnterprise(enterpriseId)
                .build();

        bankAccount = BankAccount.builder()
                .id(bankAccountId)
                .accountNumber(12345678L)
                .bank(bank)
                .accountType(AccountType.AHORROS)
                .accountingAccountId(1L)
                .status(true)
                .idEnterprise(enterpriseId)
                .usageCount(0)
                .build();
    }

    @Test
    @DisplayName("Debe incrementar contador de uso desde cero exitosamente")
    void testIncrementUsageCountFromZeroSuccess() {
        // Arrange
        when(bankAccountService.findById(bankAccountId, enterpriseId)).thenReturn(bankAccount);
        doNothing().when(bankAccountService).updateUsageCount(bankAccountId, enterpriseId, 1);

        // Act
        usageService.incrementUsageCount(bankAccountId, enterpriseId);

        // Assert
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> enterpriseCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> countCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(bankAccountService).findById(idCaptor.capture(), enterpriseCaptor.capture());
        verify(bankAccountService).updateUsageCount(idCaptor.capture(), enterpriseCaptor.capture(), countCaptor.capture());

        assertEquals(bankAccountId, idCaptor.getValue());
        assertEquals(enterpriseId, enterpriseCaptor.getValue());
        assertEquals(1, countCaptor.getValue());
    }

    @Test
    @DisplayName("Debe incrementar contador de uso desde valor existente exitosamente")
    void testIncrementUsageCountFromExistingValueSuccess() {
        // Arrange
        bankAccount.setUsageCount(5);
        when(bankAccountService.findById(bankAccountId, enterpriseId)).thenReturn(bankAccount);
        doNothing().when(bankAccountService).updateUsageCount(bankAccountId, enterpriseId, 6);

        // Act
        usageService.incrementUsageCount(bankAccountId, enterpriseId);

        // Assert
        ArgumentCaptor<Integer> countCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(bankAccountService).updateUsageCount(eq(bankAccountId), eq(enterpriseId), countCaptor.capture());
        assertEquals(6, countCaptor.getValue());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cuenta bancaria no existe")
    void testIncrementUsageCount_WithNonExistentBankAccount_ThrowsException() {
        // Arrange
        when(bankAccountService.findById(bankAccountId, enterpriseId))
                .thenThrow(new BankAccountNotFoundException());

        // Act & Assert
        assertThrows(BankAccountNotFoundException.class, () ->
                usageService.incrementUsageCount(bankAccountId, enterpriseId)
        );
        verify(bankAccountService).findById(bankAccountId, enterpriseId);
        verify(bankAccountService, never()).updateUsageCount(anyLong(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Debe incrementar contador de uso con valor grande")
    void testIncrementUsageCountWithLargeValue() {
        // Arrange
        bankAccount.setUsageCount(999);
        when(bankAccountService.findById(bankAccountId, enterpriseId)).thenReturn(bankAccount);
        doNothing().when(bankAccountService).updateUsageCount(bankAccountId, enterpriseId, 1000);

        // Act
        usageService.incrementUsageCount(bankAccountId, enterpriseId);

        // Assert
        ArgumentCaptor<Integer> countCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(bankAccountService).updateUsageCount(eq(bankAccountId), eq(enterpriseId), countCaptor.capture());
        assertEquals(1000, countCaptor.getValue());
    }

    @Test
    @DisplayName("Debe incrementar contador de uso múltiples veces consecutivas")
    void testIncrementUsageCountMultipleTimes() {
        // Arrange
        when(bankAccountService.findById(bankAccountId, enterpriseId))
                .thenReturn(bankAccount)
                .thenReturn(bankAccount)
                .thenReturn(bankAccount);

        doNothing().when(bankAccountService).updateUsageCount(anyLong(), anyString(), anyInt());

        // Act
        usageService.incrementUsageCount(bankAccountId, enterpriseId);
        bankAccount.setUsageCount(1);
        usageService.incrementUsageCount(bankAccountId, enterpriseId);
        bankAccount.setUsageCount(2);
        usageService.incrementUsageCount(bankAccountId, enterpriseId);

        // Assert
        verify(bankAccountService, times(3)).findById(bankAccountId, enterpriseId);
        verify(bankAccountService).updateUsageCount(bankAccountId, enterpriseId, 1);
        verify(bankAccountService).updateUsageCount(bankAccountId, enterpriseId, 2);
        verify(bankAccountService).updateUsageCount(bankAccountId, enterpriseId, 3);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando servicio de actualización falla")
    void testIncrementUsageCount_WhenUpdateFails_ThrowsException() {
        // Arrange
        when(bankAccountService.findById(bankAccountId, enterpriseId)).thenReturn(bankAccount);
        doThrow(new RuntimeException("Database error"))
                .when(bankAccountService).updateUsageCount(bankAccountId, enterpriseId, 1);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                usageService.incrementUsageCount(bankAccountId, enterpriseId)
        );
        verify(bankAccountService).findById(bankAccountId, enterpriseId);
        verify(bankAccountService).updateUsageCount(bankAccountId, enterpriseId, 1);
    }

    @Test
    @DisplayName("Debe incrementar contador de uso para diferentes empresas")
    void testIncrementUsageCountForDifferentEnterprises() {
        // Arrange
        String enterprise1 = "ENT-001";
        String enterprise2 = "ENT-002";

        BankAccount account1 = BankAccount.builder()
                .id(bankAccountId)
                .usageCount(3)
                .idEnterprise(enterprise1)
                .build();

        BankAccount account2 = BankAccount.builder()
                .id(bankAccountId)
                .usageCount(5)
                .idEnterprise(enterprise2)
                .build();

        when(bankAccountService.findById(bankAccountId, enterprise1)).thenReturn(account1);
        when(bankAccountService.findById(bankAccountId, enterprise2)).thenReturn(account2);
        doNothing().when(bankAccountService).updateUsageCount(anyLong(), anyString(), anyInt());

        // Act
        usageService.incrementUsageCount(bankAccountId, enterprise1);
        usageService.incrementUsageCount(bankAccountId, enterprise2);

        // Assert
        verify(bankAccountService).updateUsageCount(bankAccountId, enterprise1, 4);
        verify(bankAccountService).updateUsageCount(bankAccountId, enterprise2, 6);
    }

    @Test
    @DisplayName("Debe incrementar contador de uso para diferentes cuentas bancarias")
    void testIncrementUsageCountForDifferentBankAccounts() {
        // Arrange
        Long accountId1 = 1L;
        Long accountId2 = 2L;

        BankAccount account1 = BankAccount.builder()
                .id(accountId1)
                .usageCount(0)
                .idEnterprise(enterpriseId)
                .build();

        BankAccount account2 = BankAccount.builder()
                .id(accountId2)
                .usageCount(10)
                .idEnterprise(enterpriseId)
                .build();

        when(bankAccountService.findById(accountId1, enterpriseId)).thenReturn(account1);
        when(bankAccountService.findById(accountId2, enterpriseId)).thenReturn(account2);
        doNothing().when(bankAccountService).updateUsageCount(anyLong(), anyString(), anyInt());

        // Act
        usageService.incrementUsageCount(accountId1, enterpriseId);
        usageService.incrementUsageCount(accountId2, enterpriseId);

        // Assert
        verify(bankAccountService).updateUsageCount(accountId1, enterpriseId, 1);
        verify(bankAccountService).updateUsageCount(accountId2, enterpriseId, 11);
    }

    @Test
    @DisplayName("Debe lanzar NullPointerException cuando usageCount es null")
    void testIncrementUsageCount_WithNullUsageCount_ThrowsNullPointerException() {
        // Arrange
        bankAccount.setUsageCount(null);
        when(bankAccountService.findById(bankAccountId, enterpriseId)).thenReturn(bankAccount);

        // Act & Assert
        assertThrows(NullPointerException.class, () ->
                usageService.incrementUsageCount(bankAccountId, enterpriseId)
        );
        verify(bankAccountService).findById(bankAccountId, enterpriseId);
        verify(bankAccountService, never()).updateUsageCount(anyLong(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Debe propagar excepción cuando findById falla con excepción genérica")
    void testIncrementUsageCount_WhenFindByIdFails_PropagatesException() {
        // Arrange
        when(bankAccountService.findById(bankAccountId, enterpriseId))
                .thenThrow(new RuntimeException("Connection timeout"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                usageService.incrementUsageCount(bankAccountId, enterpriseId)
        );
        assertEquals("Connection timeout", exception.getMessage());
        verify(bankAccountService).findById(bankAccountId, enterpriseId);
        verify(bankAccountService, never()).updateUsageCount(anyLong(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Debe incrementar contador correctamente cuando cuenta está inactiva")
    void testIncrementUsageCount_WithInactiveBankAccount_Success() {
        // Arrange
        bankAccount.setStatus(false);
        bankAccount.setUsageCount(2);
        when(bankAccountService.findById(bankAccountId, enterpriseId)).thenReturn(bankAccount);
        doNothing().when(bankAccountService).updateUsageCount(bankAccountId, enterpriseId, 3);

        // Act
        usageService.incrementUsageCount(bankAccountId, enterpriseId);

        // Assert
        ArgumentCaptor<Integer> countCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(bankAccountService).updateUsageCount(eq(bankAccountId), eq(enterpriseId), countCaptor.capture());
        assertEquals(3, countCaptor.getValue());
    }

    @Test
    @DisplayName("Debe invocar servicio con parámetros correctos en orden")
    void testIncrementUsageCountInvokesServicesInCorrectOrder() {
        // Arrange
        when(bankAccountService.findById(bankAccountId, enterpriseId)).thenReturn(bankAccount);
        doNothing().when(bankAccountService).updateUsageCount(bankAccountId, enterpriseId, 1);

        // Act
        usageService.incrementUsageCount(bankAccountId, enterpriseId);

        // Assert
        var inOrder = inOrder(bankAccountService);
        inOrder.verify(bankAccountService).findById(bankAccountId, enterpriseId);
        inOrder.verify(bankAccountService).updateUsageCount(bankAccountId, enterpriseId, 1);
    }
}
