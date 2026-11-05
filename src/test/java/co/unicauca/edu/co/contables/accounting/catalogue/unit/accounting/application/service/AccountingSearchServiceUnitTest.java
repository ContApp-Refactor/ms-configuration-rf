package co.unicauca.edu.co.contables.accounting.catalogue.unit.accounting.application.service;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IAccountingSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output.IAccountingSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.service.AccountingSearchService;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.AccountingEntryStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingEntry;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingMovement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountingSearchServiceUnitTest {

    @Mock
    private IAccountingSearchOutputPort accountingSearchOutputPort;

    @InjectMocks
    private AccountingSearchService accountingSearchService;

    private AccountingEntry accountingEntry;
    private AccountingMovement movement1;
    private AccountingMovement movement2;

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
                .status(AccountingEntryStatus.ACTIVE)
                .sourceDocumentId(100L)
                .type("RECEIPT")
                .idEnterprise("ENT-001")
                .centerCostId(1L)
                .movements(Arrays.asList(movement1, movement2))
                .build();
    }

    @Test
    @DisplayName("Debe encontrar asiento contable por ID exitosamente")
    void testFindAccountingEntryByIdSuccess() {
        // Arrange
        when(accountingSearchOutputPort.findById(1L)).thenReturn(Optional.of(accountingEntry));

        // Act
        AccountingEntry result = accountingSearchService.findAccountingEntryById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("AE-2024-001", result.getCode());
        assertEquals(AccountingEntryStatus.ACTIVE, result.getStatus());
        verify(accountingSearchOutputPort).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando asiento contable no existe por ID")
    void testFindAccountingEntryById_WithNonExistentId_ThrowsException() {
        // Arrange
        when(accountingSearchOutputPort.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () -> accountingSearchService.findAccountingEntryById(99L));
        verify(accountingSearchOutputPort).findById(99L);
    }

    @Test
    @DisplayName("Debe encontrar asiento contable por ID de recibo exitosamente")
    void testFindAccountingEntryByReceiptIdSuccess() {
        // Arrange
        Long receiptId = 100L;
        when(accountingSearchOutputPort.findByReceiptId(receiptId)).thenReturn(Optional.of(accountingEntry));

        // Act
        AccountingEntry result = accountingSearchService.findAccountingEntryByReceiptId(receiptId);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getSourceDocumentId());
        assertEquals("RECEIPT", result.getType());
        verify(accountingSearchOutputPort).findByReceiptId(receiptId);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando asiento contable no existe por ID de recibo")
    void testFindAccountingEntryByReceiptId_WithNonExistentReceiptId_ThrowsException() {
        // Arrange
        when(accountingSearchOutputPort.findByReceiptId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () -> accountingSearchService.findAccountingEntryByReceiptId(999L));
        verify(accountingSearchOutputPort).findByReceiptId(999L);
    }

    @Test
    @DisplayName("Debe encontrar asiento contable por ID de documento fuente y tipo")
    void testFindAccountingEntryBySourceDocumentIdAndTypeSuccess() {
        // Arrange
        Long sourceDocumentId = 100L;
        String type = "RECEIPT";
        when(accountingSearchOutputPort.findBySourceDocumentIdAndType(sourceDocumentId, type))
                .thenReturn(Optional.of(accountingEntry));

        // Act
        AccountingEntry result = accountingSearchService.findAccountingEntryBySourceDocumentIdAndType(sourceDocumentId, type);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getSourceDocumentId());
        assertEquals("RECEIPT", result.getType());
        verify(accountingSearchOutputPort).findBySourceDocumentIdAndType(sourceDocumentId, type);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando asiento contable no existe por ID de documento fuente y tipo")
    void testFindAccountingEntryBySourceDocumentIdAndType_WithNonExistent_ThrowsException() {
        // Arrange
        when(accountingSearchOutputPort.findBySourceDocumentIdAndType(999L, "WRITEOFF"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(Exception.class, () -> 
            accountingSearchService.findAccountingEntryBySourceDocumentIdAndType(999L, "WRITEOFF"));
        verify(accountingSearchOutputPort).findBySourceDocumentIdAndType(999L, "WRITEOFF");
    }

    @Test
    @DisplayName("Debe obtener movimientos contables por ID de cuenta")
    void testFindMovementsByAccountIdSuccess() {
        // Arrange
        Long accountId = 100L;
        List<AccountingMovement> movements = Collections.singletonList(movement1);
        when(accountingSearchOutputPort.findMovementsByAccountId(accountId)).thenReturn(movements);

        // Act
        List<AccountingMovement> result = accountingSearchService.findMovementsByAccountId(accountId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getAccount());
        assertEquals(BigDecimal.valueOf(1000), result.get(0).getDebit());
        verify(accountingSearchOutputPort).findMovementsByAccountId(accountId);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay movimientos para la cuenta")
    void testFindMovementsByAccountId_WithNoMovements_ReturnsEmptyList() {
        // Arrange
        Long accountId = 999L;
        when(accountingSearchOutputPort.findMovementsByAccountId(accountId)).thenReturn(Collections.emptyList());

        // Act
        List<AccountingMovement> result = accountingSearchService.findMovementsByAccountId(accountId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(accountingSearchOutputPort).findMovementsByAccountId(accountId);
    }

    @Test
    @DisplayName("Debe obtener movimientos contables por ID de tercero")
    void testFindMovementsByThirdPartyIdSuccess() {
        // Arrange
        Long thirdPartyId = 50L;
        List<AccountingMovement> movements = Arrays.asList(movement1, movement2);
        when(accountingSearchOutputPort.findMovementsByThirdPartyId(thirdPartyId)).thenReturn(movements);

        // Act
        List<AccountingMovement> result = accountingSearchService.findMovementsByThirdPartyId(thirdPartyId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(50L, result.get(0).getThirdPartyId());
        verify(accountingSearchOutputPort).findMovementsByThirdPartyId(thirdPartyId);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay movimientos para el tercero")
    void testFindMovementsByThirdPartyId_WithNoMovements_ReturnsEmptyList() {
        // Arrange
        Long thirdPartyId = 999L;
        when(accountingSearchOutputPort.findMovementsByThirdPartyId(thirdPartyId))
                .thenReturn(Collections.emptyList());

        // Act
        List<AccountingMovement> result = accountingSearchService.findMovementsByThirdPartyId(thirdPartyId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(accountingSearchOutputPort).findMovementsByThirdPartyId(thirdPartyId);
    }

    @Test
    @DisplayName("Debe encontrar movimientos por ID de tercero con múltiples resultados")
    void testFindMovementsByThirdPartyId_WithMultipleResults_Success() {
        // Arrange
        Long thirdPartyId = 50L;
        AccountingMovement movement3 = AccountingMovement.builder()
                .id(3L)
                .account(300L)
                .thirdPartyId(50L)
                .description("Pago de deuda")
                .debit(BigDecimal.valueOf(500))
                .credit(BigDecimal.ZERO)
                .build();

        List<AccountingMovement> movements = Arrays.asList(movement1, movement2, movement3);
        when(accountingSearchOutputPort.findMovementsByThirdPartyId(thirdPartyId)).thenReturn(movements);

        // Act
        List<AccountingMovement> result = accountingSearchService.findMovementsByThirdPartyId(thirdPartyId);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(m -> m.getThirdPartyId().equals(50L)));
        verify(accountingSearchOutputPort).findMovementsByThirdPartyId(thirdPartyId);
    }

    @Test
    @DisplayName("Debe retornar asiento contable con movimientos de crédito y débito")
    void testFindAccountingEntryById_WithBalancedMovements_Success() {
        // Arrange
        when(accountingSearchOutputPort.findById(1L)).thenReturn(Optional.of(accountingEntry));

        // Act
        AccountingEntry result = accountingSearchService.findAccountingEntryById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getMovements().size());
        
        BigDecimal totalDebit = result.getMovements().stream()
                .map(AccountingMovement::getDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCredit = result.getMovements().stream()
                .map(AccountingMovement::getCredit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        assertEquals(totalDebit, totalCredit);
        verify(accountingSearchOutputPort).findById(1L);
    }

    @Test
    @DisplayName("Debe encontrar asiento contable con información de empresa")
    void testFindAccountingEntryById_WithEnterpriseInfo_Success() {
        // Arrange
        when(accountingSearchOutputPort.findById(1L)).thenReturn(Optional.of(accountingEntry));

        // Act
        AccountingEntry result = accountingSearchService.findAccountingEntryById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("ENT-001", result.getIdEnterprise());
        assertEquals(1L, result.getCenterCostId());
        verify(accountingSearchOutputPort).findById(1L);
    }
}
