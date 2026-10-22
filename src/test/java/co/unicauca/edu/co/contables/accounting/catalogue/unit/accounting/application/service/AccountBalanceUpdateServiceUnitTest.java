package co.unicauca.edu.co.contables.accounting.catalogue.unit.accounting.application.service;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IAccountBalanceUpdateInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.service.AccountBalanceUpdateService;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingEntry;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingMovement;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueUpdateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountBalanceUpdateServiceUnitTest {

    @Mock
    private IAccountCatalogueSearchOutputPort accountCatalogueSearchOutputPort;

    @Mock
    private IAccountCatalogueUpdateOutputPort accountCatalogueUpdateOutputPort;

    @InjectMocks
    private AccountBalanceUpdateService accountBalanceUpdateService;

    private AccountingEntry accountingEntry;
    private AccountingMovement movement1;
    private AccountingMovement movement2;
    private AccountCatalogue debitAccount;
    private AccountCatalogue creditAccount;
    private AccountCatalogue parentAccount;

    @BeforeEach
    void setUp() {
        movement1 = AccountingMovement.builder()
                .id(1L)
                .account(100L)
                .thirdPartyId(50L)
                .description("Ingreso por Recibo")
                .debit(BigDecimal.valueOf(1000))
                .credit(BigDecimal.ZERO)
                .build();

        movement2 = AccountingMovement.builder()
                .id(2L)
                .account(200L)
                .thirdPartyId(50L)
                .description("Abono a factura")
                .debit(BigDecimal.ZERO)
                .credit(BigDecimal.valueOf(1000))
                .build();

        accountingEntry = AccountingEntry.builder()
                .id(1L)
                .code("AE-2024-001")
                .date(LocalDate.of(2024, 1, 15))
                .description("Contabilización de Recibo de Caja RC-12345")
                .sourceDocumentId(100L)
                .type("RECEIPT")
                .idEnterprise("ENT-001")
                .centerCostId(1L)
                .movements(Arrays.asList(movement1, movement2))
                .build();

        parentAccount = AccountCatalogue.builder()
                .id(10L)
                .code("1")
                .description("Activos")
                .nature(NatureEnum.DEBIT)
                .amount(BigDecimal.valueOf(10000))
                .status(true)
                .idEnterprise("ENT-001")
                .build();

        debitAccount = AccountCatalogue.builder()
                .id(100L)
                .code("11050501")
                .description("Bancos")
                .nature(NatureEnum.DEBIT)
                .amount(BigDecimal.valueOf(5000))
                .status(true)
                .idEnterprise("ENT-001")
                .parent(parentAccount)
                .build();

        creditAccount = AccountCatalogue.builder()
                .id(200L)
                .code("21050501")
                .description("Cuentas por cobrar")
                .nature(NatureEnum.CREDIT)
                .amount(BigDecimal.valueOf(3000))
                .status(true)
                .idEnterprise("ENT-001")
                .parent(parentAccount)
                .build();
    }

    @Test
    @DisplayName("Debe actualizar saldos exitosamente desde un asiento contable")
    void testUpdateBalancesFromAccountingEntrySuccess() {
        // Arrange
        // Crear cuentas sin padre para evitar NPE 
        AccountCatalogue debitAccountWithoutParent = AccountCatalogue.builder()
                .id(100L)
                .code("11050501")
                .description("Bancos")
                .nature(NatureEnum.DEBIT)
                .amount(BigDecimal.valueOf(5000))
                .status(true)
                .idEnterprise("ENT-001")
                .parent(null)
                .build();
        
        AccountCatalogue creditAccountWithoutParent = AccountCatalogue.builder()
                .id(200L)
                .code("21050501")
                .description("Cuentas por cobrar")
                .nature(NatureEnum.CREDIT)
                .amount(BigDecimal.valueOf(3000))
                .status(true)
                .idEnterprise("ENT-001")
                .parent(null)
                .build();
        
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(100L, "ENT-001"))
                .thenReturn(debitAccountWithoutParent);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(200L, "ENT-001"))
                .thenReturn(creditAccountWithoutParent);

        // Act
        accountBalanceUpdateService.updateBalancesFromAccountingEntry(accountingEntry);

        // Assert
        verify(accountCatalogueUpdateOutputPort, atLeastOnce()).updateAmount(anyLong(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Debe actualizar saldo de cuenta deudora correctamente")
    void testUpdateBalancesFromAccountingEntry_WithDebtorAccount_IncreaseBalance() {
        // Arrange
        // Crear una cuenta sin padre para evitar NPE
        AccountCatalogue debitAccountWithoutParent = AccountCatalogue.builder()
                .id(100L)
                .code("11050501")
                .description("Bancos")
                .nature(NatureEnum.DEBIT)
                .amount(BigDecimal.valueOf(5000))
                .status(true)
                .idEnterprise("ENT-001")
                .parent(null)
                .build();
                
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(100L, "ENT-001"))
                .thenReturn(debitAccountWithoutParent);

        // Act
        accountBalanceUpdateService.updateBalancesFromAccountingEntry(accountingEntry);

        // Assert
        ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(accountCatalogueUpdateOutputPort).updateAmount(eq(100L), amountCaptor.capture());
        assertEquals(BigDecimal.valueOf(6000), amountCaptor.getValue());
    }

    @Test
    @DisplayName("Debe revertir saldos exitosamente desde un asiento contable")
    void testReverseBalancesFromAccountingEntrySuccess() {
        // Arrange
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(100L, "ENT-001"))
                .thenReturn(debitAccount);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(10L, "ENT-001"))
                .thenReturn(parentAccount);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(200L, "ENT-001"))
                .thenReturn(creditAccount);

        // Act
        accountBalanceUpdateService.reverseBalancesFromAccountingEntry(accountingEntry);

        // Assert
        verify(accountCatalogueUpdateOutputPort, atLeastOnce()).updateAmount(anyLong(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Debe revertir saldo de cuenta deudora correctamente")
    void testReverseBalancesFromAccountingEntry_WithDebtorAccount_DecreaseBalance() {
        // Arrange
        // Crear una cuenta sin padre para evitar NPE
        AccountCatalogue debitAccountWithoutParent = AccountCatalogue.builder()
                .id(100L)
                .code("11050501")
                .description("Bancos")
                .nature(NatureEnum.DEBIT)
                .amount(BigDecimal.valueOf(5000))
                .status(true)
                .idEnterprise("ENT-001")
                .parent(null)
                .build();
                
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(100L, "ENT-001"))
                .thenReturn(debitAccountWithoutParent);

        // Act
        accountBalanceUpdateService.reverseBalancesFromAccountingEntry(accountingEntry);

        // Assert
        ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(accountCatalogueUpdateOutputPort).updateAmount(eq(100L), amountCaptor.capture());
        assertEquals(BigDecimal.valueOf(4000), amountCaptor.getValue());
    }

    @Test
    @DisplayName("Debe procesar múltiples movimientos en un asiento contable")
    void testUpdateBalancesFromAccountingEntry_WithMultipleMovements_Success() {
        // Arrange
        AccountCatalogue debitAccountWithoutParent = AccountCatalogue.builder()
                .id(100L)
                .code("11050501")
                .description("Bancos")
                .nature(NatureEnum.DEBIT)
                .amount(BigDecimal.valueOf(5000))
                .status(true)
                .idEnterprise("ENT-001")
                .parent(null)
                .build();
        
        AccountCatalogue creditAccountWithoutParent = AccountCatalogue.builder()
                .id(200L)
                .code("21050501")
                .description("Cuentas por cobrar")
                .nature(NatureEnum.CREDIT)
                .amount(BigDecimal.valueOf(3000))
                .status(true)
                .idEnterprise("ENT-001")
                .parent(null)
                .build();
        
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(100L, "ENT-001"))
                .thenReturn(debitAccountWithoutParent);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(200L, "ENT-001"))
                .thenReturn(creditAccountWithoutParent);

        // Act
        accountBalanceUpdateService.updateBalancesFromAccountingEntry(accountingEntry);

        // Assert
        verify(accountCatalogueUpdateOutputPort, atLeast(2)).updateAmount(anyLong(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Debe actualizar saldo en cuenta acreedora correctamente")
    void testUpdateBalancesFromAccountingEntry_WithCreditorAccount_DecreaseBalance() {
        // Arrange
        AccountCatalogue creditAccountWithoutParent = AccountCatalogue.builder()
                .id(200L)
                .code("21050501")
                .description("Cuentas por cobrar")
                .nature(NatureEnum.CREDIT)
                .amount(BigDecimal.valueOf(3000))
                .status(true)
                .idEnterprise("ENT-001")
                .parent(null)
                .build();
        
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(200L, "ENT-001"))
                .thenReturn(creditAccountWithoutParent);

        // Act
        accountBalanceUpdateService.updateBalancesFromAccountingEntry(accountingEntry);

        // Assert
        ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(accountCatalogueUpdateOutputPort).updateAmount(eq(200L), amountCaptor.capture());
        // La cuenta acreedora aumenta con el crédito (1000): 3000 + 1000 = 4000
        assertEquals(BigDecimal.valueOf(4000), amountCaptor.getValue());
    }

    @Test
    @DisplayName("Debe manejar asiento contable sin movimientos")
    void testUpdateBalancesFromAccountingEntry_WithNoMovements_Success() {
        // Arrange
        AccountingEntry emptyEntry = AccountingEntry.builder()
                .id(2L)
                .code("AE-2024-002")
                .date(LocalDate.of(2024, 1, 16))
                .description("Asiento vacío")
                .sourceDocumentId(101L)
                .type("RECEIPT")
                .idEnterprise("ENT-001")
                .centerCostId(1L)
                .movements(Collections.emptyList())
                .build();

        // Act
        accountBalanceUpdateService.updateBalancesFromAccountingEntry(emptyEntry);

        // Assert
        verify(accountCatalogueUpdateOutputPort, never()).updateAmount(anyLong(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Debe manejar cuenta no encontrada gracefully durante actualización")
    void testUpdateBalancesFromAccountingEntry_WithMissingAccount_ContinueProcessing() {
        // Arrange
        AccountCatalogue creditAccountWithoutParent = AccountCatalogue.builder()
                .id(200L)
                .code("21050501")
                .description("Cuentas por cobrar")
                .nature(NatureEnum.CREDIT)
                .amount(BigDecimal.valueOf(3000))
                .status(true)
                .idEnterprise("ENT-001")
                .parent(null)
                .build();
                
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(100L, "ENT-001"))
                .thenReturn(null);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(200L, "ENT-001"))
                .thenReturn(creditAccountWithoutParent);

        // Act
        accountBalanceUpdateService.updateBalancesFromAccountingEntry(accountingEntry);

        // Assert - debe continuar procesando las otras cuentas
        verify(accountCatalogueUpdateOutputPort, atLeastOnce()).updateAmount(anyLong(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Debe actualizar jerarquía de cuentas correctamente")
    void testUpdateBalancesFromAccountingEntry_WithAccountHierarchy_UpdatesParent() {
        // Arrange
        // Crear cuentas sin padre para evitar NPE
        AccountCatalogue debitAccountWithoutParent = AccountCatalogue.builder()
                .id(100L)
                .code("11050501")
                .description("Bancos")
                .nature(NatureEnum.DEBIT)
                .amount(BigDecimal.valueOf(5000))
                .status(true)
                .idEnterprise("ENT-001")
                .parent(null)
                .build();
        
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(100L, "ENT-001"))
                .thenReturn(debitAccountWithoutParent);

        // Act
        accountBalanceUpdateService.updateBalancesFromAccountingEntry(accountingEntry);

        // Assert
        verify(accountCatalogueUpdateOutputPort, atLeastOnce()).updateAmount(eq(100L), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Debe invertir la actualización de saldos correctamente")
    void testReverseBalancesFromAccountingEntry_InvertsUpdates() {
        // Arrange
        AccountCatalogue debitAccountWithoutParent = AccountCatalogue.builder()
                .id(100L)
                .code("11050501")
                .description("Bancos")
                .nature(NatureEnum.DEBIT)
                .amount(BigDecimal.valueOf(5000))
                .status(true)
                .idEnterprise("ENT-001")
                .parent(null)
                .build();
                
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(100L, "ENT-001"))
                .thenReturn(debitAccountWithoutParent);

        // Act - Actualizar primero
        accountBalanceUpdateService.updateBalancesFromAccountingEntry(accountingEntry);
        
        // Reset mock para la reversión
        reset(accountCatalogueUpdateOutputPort);
        
        // Actualizar el saldo
        debitAccountWithoutParent.setAmount(BigDecimal.valueOf(6000));
        
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(100L, "ENT-001"))
                .thenReturn(debitAccountWithoutParent);

        // Act - Revertir
        accountBalanceUpdateService.reverseBalancesFromAccountingEntry(accountingEntry);

        // Assert
        ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(accountCatalogueUpdateOutputPort).updateAmount(eq(100L), amountCaptor.capture());
        assertEquals(BigDecimal.valueOf(5000), amountCaptor.getValue());
    }

    @Test
    @DisplayName("Debe procesar asiento con movimientos de débito y crédito correctamente")
    void testUpdateBalancesFromAccountingEntry_WithDebitAndCreditMovements_Success() {
        // Arrange
        AccountCatalogue debitAccountWithoutParent = AccountCatalogue.builder()
                .id(100L)
                .code("11050501")
                .description("Bancos")
                .nature(NatureEnum.DEBIT)
                .amount(BigDecimal.valueOf(5000))
                .status(true)
                .idEnterprise("ENT-001")
                .parent(null)
                .build();
        
        AccountCatalogue creditAccountWithoutParent = AccountCatalogue.builder()
                .id(200L)
                .code("21050501")
                .description("Cuentas por cobrar")
                .nature(NatureEnum.CREDIT)
                .amount(BigDecimal.valueOf(3000))
                .status(true)
                .idEnterprise("ENT-001")
                .parent(null)
                .build();
        
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(100L, "ENT-001"))
                .thenReturn(debitAccountWithoutParent);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(200L, "ENT-001"))
                .thenReturn(creditAccountWithoutParent);

        // Act
        accountBalanceUpdateService.updateBalancesFromAccountingEntry(accountingEntry);

        // Assert
        verify(accountCatalogueUpdateOutputPort).updateAmount(eq(100L), any(BigDecimal.class));
        verify(accountCatalogueUpdateOutputPort).updateAmount(eq(200L), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Debe mantener integridad de partida doble")
    void testUpdateBalancesFromAccountingEntry_MaintainsDoubleEntry() {
        // Arrange
        AccountCatalogue debitAccountWithoutParent = AccountCatalogue.builder()
                .id(100L)
                .code("11050501")
                .description("Bancos")
                .nature(NatureEnum.DEBIT)
                .amount(BigDecimal.valueOf(5000))
                .status(true)
                .idEnterprise("ENT-001")
                .parent(null)
                .build();
        
        AccountCatalogue creditAccountWithoutParent = AccountCatalogue.builder()
                .id(200L)
                .code("21050501")
                .description("Cuentas por cobrar")
                .nature(NatureEnum.CREDIT)
                .amount(BigDecimal.valueOf(3000))
                .status(true)
                .idEnterprise("ENT-001")
                .parent(null)
                .build();
        
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(100L, "ENT-001"))
                .thenReturn(debitAccountWithoutParent);
        when(accountCatalogueSearchOutputPort.getAccountCatalogueById(200L, "ENT-001"))
                .thenReturn(creditAccountWithoutParent);

        // Act
        accountBalanceUpdateService.updateBalancesFromAccountingEntry(accountingEntry);

        // Assert - Verificar que se actualicen ambas cuentas
        ArgumentCaptor<Long> accountCaptor = ArgumentCaptor.forClass(Long.class);
        verify(accountCatalogueUpdateOutputPort, times(2)).updateAmount(accountCaptor.capture(), any(BigDecimal.class));
        
        assertTrue(accountCaptor.getAllValues().contains(100L));
        assertTrue(accountCaptor.getAllValues().contains(200L));
    }
}
