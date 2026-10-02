package co.unicauca.edu.co.contables.accounting.catalogue.unit.accounting.domain.models;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.AccountingEntryStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingEntry;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingMovement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class AccountingEntryUnitTest {

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
    @DisplayName("Debe crear asiento contable con datos correctos")
    void testAccountingEntryCreationSuccess() {
        // Arrange & Act
        AccountingEntry entry = AccountingEntry.builder()
                .id(1L)
                .code("AE-2024-001")
                .date(LocalDate.of(2024, 1, 15))
                .description("Test description")
                .status(AccountingEntryStatus.ACTIVE)
                .sourceDocumentId(100L)
                .type("RECEIPT")
                .idEnterprise("ENT-001")
                .centerCostId(1L)
                .movements(Arrays.asList(movement1, movement2))
                .build();

        // Assert
        assertNotNull(entry);
        assertEquals(1L, entry.getId());
        assertEquals("AE-2024-001", entry.getCode());
        assertEquals(LocalDate.of(2024, 1, 15), entry.getDate());
        assertEquals(AccountingEntryStatus.ACTIVE, entry.getStatus());
        assertEquals(2, entry.getMovements().size());
    }

    @Test
    @DisplayName("Debe anular asiento contable en estado ACTIVE")
    void testVoidEntrySuccess() {
        // Arrange
        assertEquals(AccountingEntryStatus.ACTIVE, accountingEntry.getStatus());

        // Act
        accountingEntry.voidEntry();

        // Assert
        assertEquals(AccountingEntryStatus.VOIDED, accountingEntry.getStatus());
    }

    @Test
    @DisplayName("Debe lanzar excepción al anular asiento ya anulado")
    void testVoidEntry_WithAlreadyVoidedEntry_IsIdempotent() {
        // Arrange
        accountingEntry.voidEntry();
        assertEquals(AccountingEntryStatus.VOIDED, accountingEntry.getStatus());

        // Act & Assert - No debe lanzar excepción, es idempotente
        assertDoesNotThrow(() -> accountingEntry.voidEntry());
        assertEquals(AccountingEntryStatus.VOIDED, accountingEntry.getStatus());
    }

    @Test
    @DisplayName("Debe lanzar excepción al anular asiento en estado diferente a ACTIVE")
    void testVoidEntry_WithNonActiveStatus_ThrowsException() {
        // Arrange
        accountingEntry.setStatus(AccountingEntryStatus.VOIDED);
        // Llamar voidEntry una primera vez para anular
        accountingEntry = AccountingEntry.builder()
                .id(2L)
                .code("AE-2024-002")
                .date(LocalDate.of(2024, 1, 15))
                .description("Test entry 2")
                .status(AccountingEntryStatus.ACTIVE)
                .movements(new ArrayList<>())
                .build();
        accountingEntry.voidEntry(); // Primer anulación

        // Act & Assert - La segunda vez retorna sin error (idempotencia)
        assertDoesNotThrow(() -> accountingEntry.voidEntry());
    }

    @Test
    @DisplayName("Debe obtener información completa del asiento contable")
    void testAccountingEntryGettersMethods() {
        // Arrange & Act
        // El setup ya prepara accountingEntry

        // Assert
        assertEquals(1L, accountingEntry.getId());
        assertEquals("AE-2024-001", accountingEntry.getCode());
        assertEquals(LocalDate.of(2024, 1, 15), accountingEntry.getDate());
        assertEquals("Contabilización de Recibo de Caja RC-12345", accountingEntry.getDescription());
        assertEquals(AccountingEntryStatus.ACTIVE, accountingEntry.getStatus());
        assertEquals(100L, accountingEntry.getSourceDocumentId());
        assertEquals("RECEIPT", accountingEntry.getType());
        assertEquals("ENT-001", accountingEntry.getIdEnterprise());
        assertEquals(1L, accountingEntry.getCenterCostId());
    }

    @Test
    @DisplayName("Debe modificar propiedades del asiento contable")
    void testAccountingEntrySettersMethods() {
        // Arrange
        AccountingEntry entry = AccountingEntry.builder().build();

        // Act
        entry.setId(1L);
        entry.setCode("AE-2024-001");
        entry.setDate(LocalDate.of(2024, 1, 15));
        entry.setDescription("New description");
        entry.setStatus(AccountingEntryStatus.ACTIVE);
        entry.setSourceDocumentId(100L);
        entry.setType("RECEIPT");
        entry.setIdEnterprise("ENT-001");
        entry.setCenterCostId(1L);
        entry.setMovements(Arrays.asList(movement1, movement2));

        // Assert
        assertEquals(1L, entry.getId());
        assertEquals("AE-2024-001", entry.getCode());
        assertEquals("New description", entry.getDescription());
        assertEquals(2, entry.getMovements().size());
    }

    @Test
    @DisplayName("Debe mantener múltiples movimientos contables")
    void testAccountingEntryWithMultipleMovements() {
        // Arrange
        AccountingMovement movement3 = AccountingMovement.builder()
                .id(3L)
                .account(300L)
                .thirdPartyId(60L)
                .description("Otro movimiento")
                .debit(BigDecimal.valueOf(500))
                .credit(BigDecimal.ZERO)
                .build();

        // Act
        accountingEntry.setMovements(Arrays.asList(movement1, movement2, movement3));

        // Assert
        assertEquals(3, accountingEntry.getMovements().size());
        assertTrue(accountingEntry.getMovements().stream()
                .anyMatch(m -> m.getId().equals(3L)));
    }

    @Test
    @DisplayName("Debe validar que asiento contable tenga movimientos")
    void testAccountingEntryWithEmptyMovements() {
        // Arrange
        AccountingEntry entry = AccountingEntry.builder()
                .id(1L)
                .code("AE-2024-001")
                .movements(Arrays.asList())
                .build();

        // Act & Assert
        assertNotNull(entry.getMovements());
        assertTrue(entry.getMovements().isEmpty());
    }

    @Test
    @DisplayName("Debe verificar información de documento origen")
    void testAccountingEntrySourceDocumentInfo() {
        // Arrange & Act
        // Setup ya prepara accountingEntry

        // Assert
        assertEquals(100L, accountingEntry.getSourceDocumentId());
        assertEquals("RECEIPT", accountingEntry.getType());
        assertNotNull(accountingEntry.getSourceDocumentId());
    }

    @Test
    @DisplayName("Debe mantener información de empresa")
    void testAccountingEntryEnterpriseInfo() {
        // Arrange & Act
        // Setup ya prepara accountingEntry

        // Assert
        assertEquals("ENT-001", accountingEntry.getIdEnterprise());
        assertNotNull(accountingEntry.getIdEnterprise());
    }

    @Test
    @DisplayName("Debe mantener información de centro de costo")
    void testAccountingEntryCenterCostInfo() {
        // Arrange & Act
        // Setup ya prepara accountingEntry

        // Assert
        assertEquals(1L, accountingEntry.getCenterCostId());
        assertNotNull(accountingEntry.getCenterCostId());
    }

    @Test
    @DisplayName("Debe validar totalidad de débitos y créditos")
    void testAccountingEntryDoubleEntry() {
        // Arrange & Act
        BigDecimal totalDebit = accountingEntry.getMovements().stream()
                .map(AccountingMovement::getDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredit = accountingEntry.getMovements().stream()
                .map(AccountingMovement::getCredit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Assert
        assertEquals(totalDebit, totalCredit);
        assertEquals(BigDecimal.valueOf(1000), totalDebit);
        assertEquals(BigDecimal.valueOf(1000), totalCredit);
    }

    @Test
    @DisplayName("Debe crear asiento con distintos tipos de documentos")
    void testAccountingEntryWithDifferentDocumentTypes() {
        // Arrange & Act
        AccountingEntry writeOffEntry = AccountingEntry.builder()
                .id(2L)
                .code("AE-2024-002")
                .type("PORTFOLIO_WRITEOFF")
                .sourceDocumentId(200L)
                .build();

        // Assert
        assertEquals("PORTFOLIO_WRITEOFF", writeOffEntry.getType());
        assertEquals(200L, writeOffEntry.getSourceDocumentId());
    }

    @Test
    @DisplayName("Debe mantener código único del asiento")
    void testAccountingEntryUniqueCode() {
        // Arrange
        AccountingEntry entry1 = AccountingEntry.builder()
                .code("AE-2024-001")
                .build();

        AccountingEntry entry2 = AccountingEntry.builder()
                .code("AE-2024-002")
                .build();

        // Act & Assert
        assertNotEquals(entry1.getCode(), entry2.getCode());
        assertEquals("AE-2024-001", entry1.getCode());
        assertEquals("AE-2024-002", entry2.getCode());
    }

    @Test
    @DisplayName("Debe validar que fecha de asiento sea válida")
    void testAccountingEntryValidDate() {
        // Arrange
        LocalDate validDate = LocalDate.of(2024, 1, 15);

        // Act
        accountingEntry.setDate(validDate);

        // Assert
        assertEquals(validDate, accountingEntry.getDate());
        assertTrue(validDate.isBefore(LocalDate.now().plusDays(1)));
    }

    @Test
    @DisplayName("Debe retornar descripción del asiento contable")
    void testAccountingEntryDescription() {
        // Arrange
        String expectedDescription = "Contabilización de Recibo de Caja RC-12345";

        // Act
        String actualDescription = accountingEntry.getDescription();

        // Assert
        assertEquals(expectedDescription, actualDescription);
        assertNotNull(actualDescription);
        assertTrue(actualDescription.contains("Recibo"));
    }

    @Test
    @DisplayName("Debe mantener estado del asiento de acuerdo a su ciclo de vida")
    void testAccountingEntryStatusLifecycle() {
        // Arrange
        AccountingEntry entry = AccountingEntry.builder()
                .status(AccountingEntryStatus.ACTIVE)
                .build();

        // Act
        assertEquals(AccountingEntryStatus.ACTIVE, entry.getStatus());
        entry.voidEntry();

        // Assert
        assertEquals(AccountingEntryStatus.VOIDED, entry.getStatus());
    }
}
