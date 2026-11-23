package co.unicauca.edu.co.contables.inventory.kardex.unit.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;

public class KardexUnitTest {
    
    private Kardex kardex;
    
    @BeforeEach
    void setUp() {
        kardex = new Kardex();
    }
    
    @Test
    @DisplayName("Should add current date when calling addDate")
    void testAddDate() {
        // Arrange
        ZonedDateTime before = ZonedDateTime.now(ZoneId.of("America/Bogota")).truncatedTo(ChronoUnit.SECONDS);
        
        // Act
        kardex.addDate();
        
        // Assert
        ZonedDateTime after = ZonedDateTime.now(ZoneId.of("America/Bogota")).truncatedTo(ChronoUnit.SECONDS);
        assertNotNull(kardex.getDate());
        ZonedDateTime kardexDate = kardex.getDate().truncatedTo(ChronoUnit.SECONDS);
        assertTrue(kardexDate.equals(before) || kardexDate.isAfter(before) && kardexDate.isBefore(after) 
                || kardexDate.equals(after));
    }
    
    @Test
    @DisplayName("Should correctly calculate the balance when adding a purchase")
    void testAddPurchase() {
        // Arrange
        kardex.setQuantity(10);
        kardex.setUnitPrice(new BigDecimal("25.50"));
        kardex.setType(MovementType.PURCHASE);
        kardex.setFactCode(null); // Para generar código de ajuste
        int lastQuantity = 5;
        BigDecimal lastTotalBalance = new BigDecimal("100.00");
        
        // Act
        kardex.addPurchase(lastQuantity, lastTotalBalance);
        
        // Assert
        assertEquals(15L, kardex.getBalanceQuantity());
        BigDecimal expectedTotal = new BigDecimal("355.00"); // 100 + (10 * 25.50)
        assertEquals(expectedTotal, kardex.getTotalBalance());
        
        // The average unit price should be the total divided by the quantity
        BigDecimal expectedUnitPrice = expectedTotal.divide(new BigDecimal("15"), 2, RoundingMode.HALF_UP);
        assertEquals(expectedUnitPrice, kardex.getBalanceUnitPrice());
        
        // Verify details were updated
        assertNotNull(kardex.getDetails());
        assertTrue(kardex.getDetails().contains("Compra - Factura:"));
        
        // Verify fact code was generated (should not be null)
        assertNotNull(kardex.getFactCode());
    }
    
    @Test
    @DisplayName("Should handle a purchase with zero balance quantity correctly")
    void testAddPurchaseWithZeroBalanceQuantity() {
        // Arrange
        kardex.setQuantity(10);
        kardex.setUnitPrice(new BigDecimal("25.50"));
        int lastQuantity = -10; // To make the resulting balance zero
        BigDecimal lastTotalBalance = new BigDecimal("100.00");
        
        // Act
        kardex.addPurchase(lastQuantity, lastTotalBalance);
        
        // Assert
        assertEquals(0L, kardex.getBalanceQuantity());
        assertEquals(BigDecimal.ZERO, kardex.getBalanceUnitPrice());
    }
    
    @Test
    @DisplayName("Should correctly calculate the balance when adding a sale")
    void testAddSale() {
        // Arrange
        kardex.setQuantity(5);
        kardex.setType(MovementType.SALE);
        kardex.setFactCode("FACT001");
        int lastQuantity = 20;
        BigDecimal lastUnitPrice = new BigDecimal("15.00");
        BigDecimal lastTotalBalance = new BigDecimal("300.00");
        
        // Act
        kardex.addSale(lastQuantity, lastUnitPrice, lastTotalBalance);
        
        // Assert
        assertEquals(15L, kardex.getBalanceQuantity());
        assertEquals(lastUnitPrice, kardex.getBalanceUnitPrice());
        assertEquals(lastUnitPrice, kardex.getUnitPrice());
        
        BigDecimal expectedTotal = new BigDecimal("225.00"); // 300 - (5 * 15)
        assertEquals(expectedTotal, kardex.getTotalBalance());
        
        // Verify details were updated
        assertNotNull(kardex.getDetails());
        assertTrue(kardex.getDetails().contains("Venta - Factura: FACT001"));
    }
    
    @Test
    @DisplayName("Should handle a sale with negative balance correctly")
    void testAddSaleWithNegativeBalance() {
        // Arrange
        kardex.setQuantity(25);
        int lastQuantity = 20; // To make the resulting balance negative
        BigDecimal lastUnitPrice = new BigDecimal("15.00");
        BigDecimal lastTotalBalance = new BigDecimal("300.00");
        
        // Act
        kardex.addSale(lastQuantity, lastUnitPrice, lastTotalBalance);
        
        // Assert
        assertEquals(-5L, kardex.getBalanceQuantity());
        assertEquals(BigDecimal.ZERO, kardex.getBalanceUnitPrice());
    }
    
    @Test
    @DisplayName("Should handle a sale with zero balance correctly")
    void testAddSaleWithZeroBalance() {
        // Arrange
        kardex.setQuantity(20);
        int lastQuantity = 20; // To make the resulting balance zero
        BigDecimal lastUnitPrice = new BigDecimal("15.00");
        BigDecimal lastTotalBalance = new BigDecimal("300.00");
        kardex.setType(MovementType.SALE);
        
        // Act
        kardex.addSale(lastQuantity, lastUnitPrice, lastTotalBalance);
        
        // Assert
        assertEquals(0L, kardex.getBalanceQuantity());
        assertEquals(BigDecimal.ZERO, kardex.getBalanceUnitPrice());
        assertEquals(BigDecimal.ZERO, kardex.getTotalBalance());
    }
    
    @Test
    @DisplayName("Should correctly calculate the balance when returning a sale")
    void testReturnOnSale() {
        // Arrange
        kardex.setQuantity(5);
        kardex.setType(MovementType.SALESRETURN);
        int lastQuantity = 15;
        BigDecimal lastUnitPrice = new BigDecimal("20.00");
        BigDecimal lastTotalBalance = new BigDecimal("300.00");
        
        // Act
        kardex.returnOnSale(lastQuantity, lastUnitPrice, lastTotalBalance);
        
        // Assert
        assertEquals(20L, kardex.getBalanceQuantity());
        BigDecimal expectedTotal = new BigDecimal("400.00"); // 300 + (5 * 20)
        assertEquals(expectedTotal, kardex.getTotalBalance());
        
        BigDecimal expectedUnitPrice = expectedTotal.divide(new BigDecimal("20"), 2, RoundingMode.HALF_UP);
        assertEquals(expectedUnitPrice, kardex.getBalanceUnitPrice());
    }
    
    @Test
    @DisplayName("Should handle a sale return with zero balance correctly")
    void testReturnOnSaleWithZeroBalance() {
        // Arrange
        kardex.setQuantity(5);
        int lastQuantity = -5; // To make the resulting balance zero
        BigDecimal lastUnitPrice = new BigDecimal("20.00");
        BigDecimal lastTotalBalance = new BigDecimal("0.00");
        
        // Act
        kardex.returnOnSale(lastQuantity, lastUnitPrice, lastTotalBalance);
        
        // Assert
        assertEquals(0L, kardex.getBalanceQuantity());
        assertEquals(BigDecimal.ZERO, kardex.getBalanceUnitPrice());
    }
    
    @Test
    @DisplayName("Should correctly calculate the balance when returning a purchase")
    void testReturnOnPurchase() {
        // Arrange
        kardex.setQuantity(5);
        kardex.setUnitPrice(new BigDecimal("20.00"));
        kardex.setType(MovementType.PURCHASERETURN);
        int lastQuantity = 25;
        BigDecimal lastTotalBalance = new BigDecimal("500.00");
        
        // Act
        kardex.returnOnPurchase(lastQuantity, lastTotalBalance);
        
        // Assert
        assertEquals(20L, kardex.getBalanceQuantity()); // 25 - 5
        BigDecimal expectedTotal = new BigDecimal("400.00"); // 500 - (5 * 20)
        assertEquals(expectedTotal, kardex.getTotalBalance());
        
        BigDecimal expectedUnitPrice = expectedTotal.divide(new BigDecimal("20"), 2, RoundingMode.HALF_UP); 
        assertEquals(expectedUnitPrice, kardex.getBalanceUnitPrice());
    }
    
    @Test
    @DisplayName("Should handle a purchase return with negative balance correctly")
    void testReturnOnPurchaseWithNegativeBalance() {
        // Arrange
        kardex.setQuantity(30);
        kardex.setUnitPrice(new BigDecimal("20.00"));
        int lastQuantity = 25; // To make the resulting balance negative
        BigDecimal lastTotalBalance = new BigDecimal("500.00");
        
        // Act
        kardex.returnOnPurchase(lastQuantity, lastTotalBalance);
        
        // Assert
        assertEquals(-5L, kardex.getBalanceQuantity());
        assertEquals(BigDecimal.ZERO, kardex.getBalanceUnitPrice());
    }
    
    @Test
    @DisplayName("Should handle a purchase return with zero balance correctly")
    void testReturnOnPurchaseWithZeroBalance() {
        // Arrange
        kardex.setQuantity(25);
        kardex.setUnitPrice(new BigDecimal("20.00"));
        int lastQuantity = 25; // To make the resulting balance zero
        BigDecimal lastTotalBalance = new BigDecimal("500.00");
        
        // Act
        kardex.returnOnPurchase(lastQuantity, lastTotalBalance);
        
        // Assert
        assertEquals(0L, kardex.getBalanceQuantity());
        assertEquals(BigDecimal.ZERO, kardex.getBalanceUnitPrice());
        assertEquals(BigDecimal.ZERO, kardex.getTotalBalance());
    }
    
    @Test
    @DisplayName("Should reset balances when calling resetBalancesIfZero")
    void testResetBalancesIfZero() {
        // Arrange
        kardex.setBalanceQuantity(10);
        kardex.setBalanceUnitPrice(new BigDecimal("25.00"));
        kardex.setTotalBalance(new BigDecimal("250.00"));
        
        // Act
        kardex.resetBalancesIfZero();
        
        // Assert
        assertEquals(0L, kardex.getBalanceQuantity());
        assertEquals(BigDecimal.ZERO, kardex.getBalanceUnitPrice());
        assertEquals(BigDecimal.ZERO, kardex.getTotalBalance());
    }

    @Test
    @DisplayName("Should update details when details exist")
    void testUpdateDetailIfNotNullWithExistingDetails() {
        // Arrange
        kardex.setType(MovementType.SALE);
        kardex.setFactCode("500");
        kardex.setDetails("Existing details");

        // Act
        kardex.updateDetailIfNotNull();

        // Assert
        assertTrue(kardex.getDetails().contains("Existing details"));
        assertTrue(kardex.getDetails().contains("Venta - Factura: 500"));
        assertTrue(kardex.getDetails().contains("|"));
    }

    @Test
    @DisplayName("Should create details when null")
    void testUpdateDetailIfNotNullWithNullDetails() {
        // Arrange
        kardex.setType(MovementType.SALE);
        kardex.setFactCode("500");
        kardex.setDetails(null);

        // Act
        kardex.updateDetailIfNotNull();

        // Assert
        assertEquals("Venta - Factura: 500", kardex.getDetails());
    }

    @Test
    @DisplayName("Should create details when empty")
    void testUpdateDetailIfNotNullWithEmptyDetails() {
        // Arrange
        kardex.setType(MovementType.SALE);
        kardex.setFactCode("500");
        kardex.setDetails("");

        // Act
        kardex.updateDetailIfNotNull();

        // Assert
        assertEquals("Venta - Factura: 500", kardex.getDetails());
    }
    
    @Test
    @DisplayName("Should generate adjustment fact code when factCode is null")
    void testGenerateAdjustmentFactCode() {
        // Arrange
        kardex.setFactCode(null);
        
        // Act
        kardex.generateAdjustmentFactCode();
        
        // Assert
        assertNotNull(kardex.getFactCode());
        assertTrue(kardex.getFactCode().startsWith("A"));
        assertTrue(kardex.getFactCode().length() > 1);
    }
    
    @Test
    @DisplayName("Should not override existing fact code")
    void testGenerateAdjustmentFactCodeWithExistingCode() {
        // Arrange
        String existingCode = "FACT001";
        kardex.setFactCode(existingCode);
        
        // Act
        kardex.generateAdjustmentFactCode();
        
        // Assert
        assertEquals(existingCode, kardex.getFactCode());
    }
}
