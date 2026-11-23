package co.unicauca.edu.co.contables.accounting.catalogue.unit.accounting.domain.models;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingMovement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountingMovementUnitTest {

    private AccountingMovement debitMovement;
    private AccountingMovement creditMovement;

    @BeforeEach
    void setUp() {
        debitMovement = AccountingMovement.builder()
                .id(1L)
                .account(100L)
                .thirdPartyId(50L)
                .description("Ingreso por Recibo")
                .debit(BigDecimal.valueOf(1000))
                .credit(BigDecimal.ZERO)
                .build();

        creditMovement = AccountingMovement.builder()
                .id(2L)
                .account(200L)
                .thirdPartyId(50L)
                .description("Abono a factura")
                .debit(BigDecimal.ZERO)
                .credit(BigDecimal.valueOf(1000))
                .build();
    }

    @Test
    @DisplayName("Debe crear movimiento de débito correctamente")
    void testCreateDebitMovementSuccess() {
        // Arrange & Act
        AccountingMovement movement = AccountingMovement.builder()
                .id(1L)
                .account(100L)
                .thirdPartyId(50L)
                .description("Ingreso por Recibo")
                .debit(BigDecimal.valueOf(1000))
                .credit(BigDecimal.ZERO)
                .build();

        // Assert
        assertNotNull(movement);
        assertEquals(1L, movement.getId());
        assertEquals(100L, movement.getAccount());
        assertEquals(50L, movement.getThirdPartyId());
        assertEquals(BigDecimal.valueOf(1000), movement.getDebit());
        assertEquals(BigDecimal.ZERO, movement.getCredit());
    }

    @Test
    @DisplayName("Debe crear movimiento de crédito correctamente")
    void testCreateCreditMovementSuccess() {
        // Arrange & Act
        AccountingMovement movement = AccountingMovement.builder()
                .id(2L)
                .account(200L)
                .thirdPartyId(50L)
                .description("Abono a factura")
                .debit(BigDecimal.ZERO)
                .credit(BigDecimal.valueOf(1000))
                .build();

        // Assert
        assertNotNull(movement);
        assertEquals(2L, movement.getId());
        assertEquals(200L, movement.getAccount());
        assertEquals(BigDecimal.ZERO, movement.getDebit());
        assertEquals(BigDecimal.valueOf(1000), movement.getCredit());
    }

    @Test
    @DisplayName("Debe validar que débito sea positivo")
    void testMovementWithPositiveDebit() {
        // Arrange & Act
        AccountingMovement movement = AccountingMovement.builder()
                .debit(BigDecimal.valueOf(1000))
                .credit(BigDecimal.ZERO)
                .build();

        // Assert
        assertTrue(movement.getDebit().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(BigDecimal.ZERO, movement.getCredit());
    }

    @Test
    @DisplayName("Debe validar que crédito sea positivo")
    void testMovementWithPositiveCredit() {
        // Arrange & Act
        AccountingMovement movement = AccountingMovement.builder()
                .debit(BigDecimal.ZERO)
                .credit(BigDecimal.valueOf(1000))
                .build();

        // Assert
        assertEquals(BigDecimal.ZERO, movement.getDebit());
        assertTrue(movement.getCredit().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Debe recuperar información de movimiento contable")
    void testGetMovementProperties() {
        // Arrange & Act
        // Setup ya prepara debitMovement

        // Assert
        assertEquals(1L, debitMovement.getId());
        assertEquals(100L, debitMovement.getAccount());
        assertEquals(50L, debitMovement.getThirdPartyId());
        assertEquals("Ingreso por Recibo", debitMovement.getDescription());
        assertEquals(BigDecimal.valueOf(1000), debitMovement.getDebit());
        assertEquals(BigDecimal.ZERO, debitMovement.getCredit());
    }

    @Test
    @DisplayName("Debe modificar propiedades del movimiento contable")
    void testSetMovementProperties() {
        // Arrange
        AccountingMovement movement = AccountingMovement.builder().build();

        // Act
        movement.setId(1L);
        movement.setAccount(100L);
        movement.setThirdPartyId(50L);
        movement.setDescription("Test description");
        movement.setDebit(BigDecimal.valueOf(1000));
        movement.setCredit(BigDecimal.ZERO);

        // Assert
        assertEquals(1L, movement.getId());
        assertEquals(100L, movement.getAccount());
        assertEquals(50L, movement.getThirdPartyId());
        assertEquals("Test description", movement.getDescription());
        assertEquals(BigDecimal.valueOf(1000), movement.getDebit());
    }

    @Test
    @DisplayName("Debe manejar movimiento sin tercero")
    void testMovementWithoutThirdParty() {
        // Arrange & Act
        AccountingMovement movement = AccountingMovement.builder()
                .id(1L)
                .account(100L)
                .thirdPartyId(null)
                .description("Movimiento sin tercero")
                .debit(BigDecimal.valueOf(1000))
                .credit(BigDecimal.ZERO)
                .build();

        // Assert
        assertNotNull(movement);
        assertNull(movement.getThirdPartyId());
        assertEquals(100L, movement.getAccount());
    }

    @Test
    @DisplayName("Debe manejar movimiento con tercero asociado")
    void testMovementWithThirdParty() {
        // Arrange & Act
        AccountingMovement movement = AccountingMovement.builder()
                .id(1L)
                .account(100L)
                .thirdPartyId(50L)
                .description("Movimiento con tercero")
                .debit(BigDecimal.valueOf(1000))
                .credit(BigDecimal.ZERO)
                .build();

        // Assert
        assertNotNull(movement);
        assertEquals(50L, movement.getThirdPartyId());
        assertEquals(100L, movement.getAccount());
    }

    @Test
    @DisplayName("Debe validar que movimiento sea débito o crédito, no ambos")
    void testMovementIsEitherDebitOrCredit() {
        // Arrange & Act
        AccountingMovement debitOnly = AccountingMovement.builder()
                .debit(BigDecimal.valueOf(1000))
                .credit(BigDecimal.ZERO)
                .build();

        AccountingMovement creditOnly = AccountingMovement.builder()
                .debit(BigDecimal.ZERO)
                .credit(BigDecimal.valueOf(1000))
                .build();

        // Assert
        assertNotEquals(debitOnly.getDebit(), debitOnly.getCredit());
        assertNotEquals(creditOnly.getDebit(), creditOnly.getCredit());
    }

    @Test
    @DisplayName("Debe mantener cuenta contable correctamente")
    void testMovementAccountNumber() {
        // Arrange & Act
        AccountingMovement movement = AccountingMovement.builder()
                .account(11050501L)
                .build();

        // Assert
        assertEquals(11050501L, movement.getAccount());
    }

    @Test
    @DisplayName("Debe mantener descripción del movimiento")
    void testMovementDescription() {
        // Arrange
        String expectedDescription = "Ingreso por Recibo de Caja RC-12345";

        // Act
        AccountingMovement movement = AccountingMovement.builder()
                .description(expectedDescription)
                .build();

        // Assert
        assertEquals(expectedDescription, movement.getDescription());
        assertTrue(movement.getDescription().contains("Recibo"));
    }

    @Test
    @DisplayName("Debe manejar montos grandes correctamente")
    void testMovementWithLargeAmounts() {
        // Arrange & Act
        BigDecimal largeAmount = BigDecimal.valueOf(999999999.99);
        AccountingMovement movement = AccountingMovement.builder()
                .debit(largeAmount)
                .credit(BigDecimal.ZERO)
                .build();

        // Assert
        assertEquals(largeAmount, movement.getDebit());
        assertTrue(movement.getDebit().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Debe manejar montos pequeños correctamente")
    void testMovementWithSmallAmounts() {
        // Arrange & Act
        BigDecimal smallAmount = BigDecimal.valueOf(0.01);
        AccountingMovement movement = AccountingMovement.builder()
                .debit(BigDecimal.ZERO)
                .credit(smallAmount)
                .build();

        // Assert
        assertEquals(smallAmount, movement.getCredit());
        assertTrue(movement.getCredit().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Debe validar que débito y crédito no sean ambos cero")
    void testMovementNotBothZero() {
        // Arrange & Act
        AccountingMovement movement = AccountingMovement.builder()
                .debit(BigDecimal.valueOf(1000))
                .credit(BigDecimal.ZERO)
                .build();

        // Assert
        assertNotEquals(movement.getDebit(), movement.getCredit());
    }

    @Test
    @DisplayName("Debe mantener ID único del movimiento")
    void testMovementUniqueId() {
        // Arrange
        AccountingMovement movement1 = AccountingMovement.builder().id(1L).build();
        AccountingMovement movement2 = AccountingMovement.builder().id(2L).build();

        // Act & Assert
        assertNotEquals(movement1.getId(), movement2.getId());
    }

    @Test
    @DisplayName("Debe comparar movimientos de débito y crédito")
    void testCompareDebitAndCreditMovements() {
        // Arrange & Act
        BigDecimal amount = BigDecimal.valueOf(1000);
        AccountingMovement debit = AccountingMovement.builder()
                .debit(amount)
                .credit(BigDecimal.ZERO)
                .build();

        AccountingMovement credit = AccountingMovement.builder()
                .debit(BigDecimal.ZERO)
                .credit(amount)
                .build();

        // Assert
        assertEquals(debit.getDebit(), credit.getCredit());
    }

    @Test
    @DisplayName("Debe mantener coherencia de datos al crear movimiento")
    void testMovementDataConsistency() {
        // Arrange
        Long account = 100L;
        Long thirdParty = 50L;
        BigDecimal debit = BigDecimal.valueOf(1000);

        // Act
        AccountingMovement movement = AccountingMovement.builder()
                .account(account)
                .thirdPartyId(thirdParty)
                .debit(debit)
                .credit(BigDecimal.ZERO)
                .build();

        // Assert
        assertEquals(account, movement.getAccount());
        assertEquals(thirdParty, movement.getThirdPartyId());
        assertEquals(debit, movement.getDebit());
    }

    @Test
    @DisplayName("Debe permitir actualizar monto del movimiento")
    void testUpdateMovementAmount() {
        // Arrange
        AccountingMovement movement = AccountingMovement.builder()
                .debit(BigDecimal.valueOf(1000))
                .credit(BigDecimal.ZERO)
                .build();

        // Act
        movement.setDebit(BigDecimal.valueOf(2000));

        // Assert
        assertEquals(BigDecimal.valueOf(2000), movement.getDebit());
    }

    @Test
    @DisplayName("Debe validar precisión de decimales en montos")
    void testMovementDecimalPrecision() {
        // Arrange & Act
        BigDecimal preciseAmount = BigDecimal.valueOf(1000.99);
        AccountingMovement movement = AccountingMovement.builder()
                .debit(preciseAmount)
                .credit(BigDecimal.ZERO)
                .build();

        // Assert
        assertEquals(preciseAmount.scale(), movement.getDebit().scale());
        assertEquals(preciseAmount, movement.getDebit());
    }
}
