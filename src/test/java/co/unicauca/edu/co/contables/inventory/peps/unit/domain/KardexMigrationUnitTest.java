package co.unicauca.edu.co.contables.inventory.peps.unit.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.inventory.peps.domain.enums.MovementType;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.KardexMigration;

/**
 * @brief Unit tests for KardexMigration
 * 
 * Tests domain model for Kardex migration records including validation,
 * getters/setters, and business logic.
 */
@DisplayName("KardexMigration Unit Tests")
class KardexMigrationUnitTest {
    
    private KardexMigration kardexMigration;

    @BeforeEach
    void setUp() {
        kardexMigration = new KardexMigration();
    }

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Should create instance with no-args constructor")
    void testNoArgsConstructor() {
        // Act
        KardexMigration migration = new KardexMigration();

        // Assert
        assertNotNull(migration);
        assertNull(migration.getProductId());
        assertNull(migration.getQuantity());
        assertNull(migration.getFactCode());
        assertNull(migration.getUnitPrice());
        assertNull(migration.getDetails());
        assertNull(migration.getType());
        assertNull(migration.getBalanceQuantity());
        assertNull(migration.getBalanceUnitPrice());
        assertNull(migration.getTotalBalance());
    }

    @Test
    @DisplayName("Should create instance with all-args constructor")
    void testAllArgsConstructor() {
        // Arrange
        Long productId = 1L;
        Long quantity = 100L;
        String factCode = "FACT-001";
        BigDecimal unitPrice = new BigDecimal("50.00");
        String details = "Initial inventory";
        MovementType type = MovementType.PURCHASE;
        Long balanceQuantity = 100L;
        BigDecimal balanceUnitPrice = new BigDecimal("50.00");
        BigDecimal totalBalance = new BigDecimal("5000.00");

        // Act
        KardexMigration migration = new KardexMigration(
            productId, quantity, factCode, unitPrice, details,
            type, balanceQuantity, balanceUnitPrice, totalBalance
        );

        // Assert
        assertEquals(productId, migration.getProductId());
        assertEquals(quantity, migration.getQuantity());
        assertEquals(factCode, migration.getFactCode());
        assertEquals(unitPrice, migration.getUnitPrice());
        assertEquals(details, migration.getDetails());
        assertEquals(type, migration.getType());
        assertEquals(balanceQuantity, migration.getBalanceQuantity());
        assertEquals(balanceUnitPrice, migration.getBalanceUnitPrice());
        assertEquals(totalBalance, migration.getTotalBalance());
    }

    // ==================== Product ID Tests ====================

    @Test
    @DisplayName("Should set and get productId correctly")
    void testProductIdGetterSetter() {
        // Arrange
        Long expectedProductId = 12345L;

        // Act
        kardexMigration.setProductId(expectedProductId);

        // Assert
        assertEquals(expectedProductId, kardexMigration.getProductId());
    }

    @Test
    @DisplayName("Should handle null productId")
    void testNullProductId() {
        // Act
        kardexMigration.setProductId(null);

        // Assert
        assertNull(kardexMigration.getProductId());
    }

    // ==================== Quantity Tests ====================

    @Test
    @DisplayName("Should set and get quantity correctly")
    void testQuantityGetterSetter() {
        // Arrange
        Long expectedQuantity = 250L;

        // Act
        kardexMigration.setQuantity(expectedQuantity);

        // Assert
        assertEquals(expectedQuantity, kardexMigration.getQuantity());
    }

    @Test
    @DisplayName("Should handle null quantity")
    void testNullQuantity() {
        // Act
        kardexMigration.setQuantity(null);

        // Assert
        assertNull(kardexMigration.getQuantity());
    }

    @Test
    @DisplayName("Should handle zero quantity")
    void testZeroQuantity() {
        // Act
        kardexMigration.setQuantity(0L);

        // Assert
        assertEquals(0L, kardexMigration.getQuantity());
    }

    @Test
    @DisplayName("Should handle large quantity values")
    void testLargeQuantity() {
        // Arrange
        Long largeQuantity = Long.MAX_VALUE;

        // Act
        kardexMigration.setQuantity(largeQuantity);

        // Assert
        assertEquals(largeQuantity, kardexMigration.getQuantity());
    }

    // ==================== Fact Code Tests ====================

    @Test
    @DisplayName("Should set and get factCode correctly")
    void testFactCodeGetterSetter() {
        // Arrange
        String expectedFactCode = "FACT-2024-001";

        // Act
        kardexMigration.setFactCode(expectedFactCode);

        // Assert
        assertEquals(expectedFactCode, kardexMigration.getFactCode());
    }

    @Test
    @DisplayName("Should handle null factCode")
    void testNullFactCode() {
        // Act
        kardexMigration.setFactCode(null);

        // Assert
        assertNull(kardexMigration.getFactCode());
    }

    @Test
    @DisplayName("Should handle empty factCode")
    void testEmptyFactCode() {
        // Act
        kardexMigration.setFactCode("");

        // Assert
        assertEquals("", kardexMigration.getFactCode());
    }

    // ==================== Unit Price Tests ====================

    @Test
    @DisplayName("Should set and get unitPrice correctly")
    void testUnitPriceGetterSetter() {
        // Arrange
        BigDecimal expectedPrice = new BigDecimal("125.75");

        // Act
        kardexMigration.setUnitPrice(expectedPrice);

        // Assert
        assertEquals(0, expectedPrice.compareTo(kardexMigration.getUnitPrice()));
    }

    @Test
    @DisplayName("Should handle null unitPrice")
    void testNullUnitPrice() {
        // Act
        kardexMigration.setUnitPrice(null);

        // Assert
        assertNull(kardexMigration.getUnitPrice());
    }

    @Test
    @DisplayName("Should handle zero unitPrice")
    void testZeroUnitPrice() {
        // Act
        kardexMigration.setUnitPrice(BigDecimal.ZERO);

        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(kardexMigration.getUnitPrice()));
    }

    @Test
    @DisplayName("Should handle unitPrice with decimals")
    void testUnitPriceWithDecimals() {
        // Arrange
        BigDecimal price = new BigDecimal("99.9999");

        // Act
        kardexMigration.setUnitPrice(price);

        // Assert
        assertEquals(0, price.compareTo(kardexMigration.getUnitPrice()));
    }

    // ==================== Details Tests ====================

    @Test
    @DisplayName("Should set and get details correctly")
    void testDetailsGetterSetter() {
        // Arrange
        String expectedDetails = "Migration from legacy system";

        // Act
        kardexMigration.setDetails(expectedDetails);

        // Assert
        assertEquals(expectedDetails, kardexMigration.getDetails());
    }

    @Test
    @DisplayName("Should handle null details")
    void testNullDetails() {
        // Act
        kardexMigration.setDetails(null);

        // Assert
        assertNull(kardexMigration.getDetails());
    }

    @Test
    @DisplayName("Should handle empty details")
    void testEmptyDetails() {
        // Act
        kardexMigration.setDetails("");

        // Assert
        assertEquals("", kardexMigration.getDetails());
    }

    // ==================== Movement Type Tests ====================

    @Test
    @DisplayName("Should set and get PURCHASE type")
    void testPurchaseType() {
        // Act
        kardexMigration.setType(MovementType.PURCHASE);

        // Assert
        assertEquals(MovementType.PURCHASE, kardexMigration.getType());
    }

    @Test
    @DisplayName("Should set and get SALE type")
    void testSaleType() {
        // Act
        kardexMigration.setType(MovementType.SALE);

        // Assert
        assertEquals(MovementType.SALE, kardexMigration.getType());
    }

    @Test
    @DisplayName("Should handle null type")
    void testNullType() {
        // Act
        kardexMigration.setType(null);

        // Assert
        assertNull(kardexMigration.getType());
    }

    // ==================== Balance Quantity Tests ====================

    @Test
    @DisplayName("Should set and get balanceQuantity correctly")
    void testBalanceQuantityGetterSetter() {
        // Arrange
        Long expectedBalance = 75L;

        // Act
        kardexMigration.setBalanceQuantity(expectedBalance);

        // Assert
        assertEquals(expectedBalance, kardexMigration.getBalanceQuantity());
    }

    @Test
    @DisplayName("Should handle null balanceQuantity")
    void testNullBalanceQuantity() {
        // Act
        kardexMigration.setBalanceQuantity(null);

        // Assert
        assertNull(kardexMigration.getBalanceQuantity());
    }

    @Test
    @DisplayName("Should handle zero balanceQuantity")
    void testZeroBalanceQuantity() {
        // Act
        kardexMigration.setBalanceQuantity(0L);

        // Assert
        assertEquals(0L, kardexMigration.getBalanceQuantity());
    }

    // ==================== Balance Unit Price Tests ====================

    @Test
    @DisplayName("Should set and get balanceUnitPrice correctly")
    void testBalanceUnitPriceGetterSetter() {
        // Arrange
        BigDecimal expectedPrice = new BigDecimal("200.50");

        // Act
        kardexMigration.setBalanceUnitPrice(expectedPrice);

        // Assert
        assertEquals(0, expectedPrice.compareTo(kardexMigration.getBalanceUnitPrice()));
    }

    @Test
    @DisplayName("Should handle null balanceUnitPrice")
    void testNullBalanceUnitPrice() {
        // Act
        kardexMigration.setBalanceUnitPrice(null);

        // Assert
        assertNull(kardexMigration.getBalanceUnitPrice());
    }

    // ==================== Total Balance Tests ====================

    @Test
    @DisplayName("Should set and get totalBalance correctly")
    void testTotalBalanceGetterSetter() {
        // Arrange
        BigDecimal expectedTotal = new BigDecimal("15037.50");

        // Act
        kardexMigration.setTotalBalance(expectedTotal);

        // Assert
        assertEquals(0, expectedTotal.compareTo(kardexMigration.getTotalBalance()));
    }

    @Test
    @DisplayName("Should handle null totalBalance")
    void testNullTotalBalance() {
        // Act
        kardexMigration.setTotalBalance(null);

        // Assert
        assertNull(kardexMigration.getTotalBalance());
    }

    // ==================== Validation Tests ====================

    @Test
    @DisplayName("validate() should pass with valid data")
    void testValidateWithValidData() {
        // Arrange
        kardexMigration.setProductId(1L);
        kardexMigration.setQuantity(100L);
        kardexMigration.setBalanceQuantity(50L);
        kardexMigration.setUnitPrice(new BigDecimal("10.00"));

        // Act & Assert
        assertDoesNotThrow(() -> kardexMigration.validate());
    }

    @Test
    @DisplayName("validate() should throw when productId is null")
    void testValidateThrowsWhenProductIdNull() {
        // Arrange
        kardexMigration.setProductId(null);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> kardexMigration.validate()
        );
        assertEquals("Product ID cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("validate() should throw when quantity is negative")
    void testValidateThrowsWhenQuantityNegative() {
        // Arrange
        kardexMigration.setProductId(1L);
        kardexMigration.setQuantity(-10L);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> kardexMigration.validate()
        );
        assertEquals("Quantity cannot be negative", exception.getMessage());
    }

    @Test
    @DisplayName("validate() should throw when balanceQuantity is negative")
    void testValidateThrowsWhenBalanceQuantityNegative() {
        // Arrange
        kardexMigration.setProductId(1L);
        kardexMigration.setQuantity(100L);
        kardexMigration.setBalanceQuantity(-5L);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> kardexMigration.validate()
        );
        assertEquals("Balance quantity cannot be negative", exception.getMessage());
    }

    @Test
    @DisplayName("validate() should throw when unitPrice is negative")
    void testValidateThrowsWhenUnitPriceNegative() {
        // Arrange
        kardexMigration.setProductId(1L);
        kardexMigration.setQuantity(100L);
        kardexMigration.setBalanceQuantity(50L);
        kardexMigration.setUnitPrice(new BigDecimal("-10.00"));

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> kardexMigration.validate()
        );
        assertEquals("Unit price cannot be negative", exception.getMessage());
    }

    @Test
    @DisplayName("validate() should pass when optional fields are null")
    void testValidateWithNullOptionalFields() {
        // Arrange
        kardexMigration.setProductId(1L);
        kardexMigration.setQuantity(null);
        kardexMigration.setBalanceQuantity(null);
        kardexMigration.setUnitPrice(null);

        // Act & Assert
        assertDoesNotThrow(() -> kardexMigration.validate());
    }

    @Test
    @DisplayName("validate() should pass with zero values")
    void testValidateWithZeroValues() {
        // Arrange
        kardexMigration.setProductId(1L);
        kardexMigration.setQuantity(0L);
        kardexMigration.setBalanceQuantity(0L);
        kardexMigration.setUnitPrice(BigDecimal.ZERO);

        // Act & Assert
        assertDoesNotThrow(() -> kardexMigration.validate());
    }

    // ==================== hasBalanceStock Tests ====================

    @Test
    @DisplayName("hasBalanceStock() should return true when balanceQuantity > 0")
    void testHasBalanceStockReturnsTrueWhenPositive() {
        // Arrange
        kardexMigration.setBalanceQuantity(50L);

        // Act
        boolean result = kardexMigration.hasBalanceStock();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("hasBalanceStock() should return false when balanceQuantity is zero")
    void testHasBalanceStockReturnsFalseWhenZero() {
        // Arrange
        kardexMigration.setBalanceQuantity(0L);

        // Act
        boolean result = kardexMigration.hasBalanceStock();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("hasBalanceStock() should return false when balanceQuantity is null")
    void testHasBalanceStockReturnsFalseWhenNull() {
        // Arrange
        kardexMigration.setBalanceQuantity(null);

        // Act
        boolean result = kardexMigration.hasBalanceStock();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("hasBalanceStock() should return false when balanceQuantity is negative")
    void testHasBalanceStockReturnsFalseWhenNegative() {
        // Arrange
        kardexMigration.setBalanceQuantity(-10L);

        // Act
        boolean result = kardexMigration.hasBalanceStock();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("hasBalanceStock() should return true with large balance")
    void testHasBalanceStockWithLargeBalance() {
        // Arrange
        kardexMigration.setBalanceQuantity(Long.MAX_VALUE);

        // Act
        boolean result = kardexMigration.hasBalanceStock();

        // Assert
        assertTrue(result);
    }

    // ==================== calculateMovementValue Tests ====================

    @Test
    @DisplayName("calculateMovementValue() should return correct value")
    void testCalculateMovementValueReturnsCorrectValue() {
        // Arrange
        kardexMigration.setQuantity(10L);
        kardexMigration.setUnitPrice(new BigDecimal("100.50"));

        // Act
        BigDecimal result = kardexMigration.calculateMovementValue();

        // Assert
        assertEquals(new BigDecimal("1005.00"), result);
    }

    @Test
    @DisplayName("calculateMovementValue() should return zero when quantity is null")
    void testCalculateMovementValueReturnsZeroWhenQuantityNull() {
        // Arrange
        kardexMigration.setQuantity(null);
        kardexMigration.setUnitPrice(new BigDecimal("100.00"));

        // Act
        BigDecimal result = kardexMigration.calculateMovementValue();

        // Assert
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("calculateMovementValue() should return zero when unitPrice is null")
    void testCalculateMovementValueReturnsZeroWhenUnitPriceNull() {
        // Arrange
        kardexMigration.setQuantity(10L);
        kardexMigration.setUnitPrice(null);

        // Act
        BigDecimal result = kardexMigration.calculateMovementValue();

        // Assert
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("calculateMovementValue() should return zero when both are null")
    void testCalculateMovementValueReturnsZeroWhenBothNull() {
        // Arrange
        kardexMigration.setQuantity(null);
        kardexMigration.setUnitPrice(null);

        // Act
        BigDecimal result = kardexMigration.calculateMovementValue();

        // Assert
        assertEquals(BigDecimal.ZERO, result);
    }

   

    @Test
    @DisplayName("calculateMovementValue() should handle large numbers")
    void testCalculateMovementValueWithLargeNumbers() {
        // Arrange
        kardexMigration.setQuantity(1000000L);
        kardexMigration.setUnitPrice(new BigDecimal("9999.99"));

        // Act
        BigDecimal result = kardexMigration.calculateMovementValue();

        // Assert
        assertEquals(new BigDecimal("9999990000.00"), result);
    }

    @Test
    @DisplayName("calculateMovementValue() should handle decimal quantities")
    void testCalculateMovementValueWithDecimals() {
        // Arrange
        kardexMigration.setQuantity(5L);
        kardexMigration.setUnitPrice(new BigDecimal("33.33"));

        // Act
        BigDecimal result = kardexMigration.calculateMovementValue();

        // Assert
        assertEquals(new BigDecimal("166.65"), result);
    }

    // ==================== Complete Entity Tests ====================

    @Test
    @DisplayName("Should create complete valid migration record")
    void testCompleteValidMigrationRecord() {
        // Arrange & Act
        kardexMigration.setProductId(1L);
        kardexMigration.setQuantity(100L);
        kardexMigration.setFactCode("FACT-2024-001");
        kardexMigration.setUnitPrice(new BigDecimal("50.00"));
        kardexMigration.setDetails("Initial migration from legacy system");
        kardexMigration.setType(MovementType.PURCHASE);
        kardexMigration.setBalanceQuantity(100L);
        kardexMigration.setBalanceUnitPrice(new BigDecimal("50.00"));
        kardexMigration.setTotalBalance(new BigDecimal("5000.00"));

        // Assert
        assertNotNull(kardexMigration);
        assertEquals(1L, kardexMigration.getProductId());
        assertEquals(100L, kardexMigration.getQuantity());
        assertEquals("FACT-2024-001", kardexMigration.getFactCode());
        assertEquals(0, new BigDecimal("50.00").compareTo(kardexMigration.getUnitPrice()));
        assertEquals("Initial migration from legacy system", kardexMigration.getDetails());
        assertEquals(MovementType.PURCHASE, kardexMigration.getType());
        assertEquals(100L, kardexMigration.getBalanceQuantity());
        assertEquals(0, new BigDecimal("50.00").compareTo(kardexMigration.getBalanceUnitPrice()));
        assertEquals(0, new BigDecimal("5000.00").compareTo(kardexMigration.getTotalBalance()));
        assertDoesNotThrow(() -> kardexMigration.validate());
        assertTrue(kardexMigration.hasBalanceStock());
    }

    @Test
    @DisplayName("Should handle migration with partial data")
    void testMigrationWithPartialData() {
        // Arrange & Act
        kardexMigration.setProductId(2L);
        kardexMigration.setQuantity(50L);
        kardexMigration.setUnitPrice(new BigDecimal("25.00"));
        kardexMigration.setType(MovementType.SALE);

        // Assert
        assertEquals(2L, kardexMigration.getProductId());
        assertEquals(50L, kardexMigration.getQuantity());
        assertNull(kardexMigration.getFactCode());
        assertNull(kardexMigration.getDetails());
        assertNull(kardexMigration.getBalanceQuantity());
        assertDoesNotThrow(() -> kardexMigration.validate());
        assertFalse(kardexMigration.hasBalanceStock());
    }

    @Test
    @DisplayName("Should represent fully consumed inventory")
    void testFullyConsumedInventory() {
        // Arrange & Act
        kardexMigration.setProductId(3L);
        kardexMigration.setQuantity(100L);
        kardexMigration.setUnitPrice(new BigDecimal("30.00"));
        kardexMigration.setBalanceQuantity(0L);
        kardexMigration.setType(MovementType.PURCHASE);

        // Assert
        assertEquals(0L, kardexMigration.getBalanceQuantity());
        assertFalse(kardexMigration.hasBalanceStock());
        assertDoesNotThrow(() -> kardexMigration.validate());
    }

    @Test
    @DisplayName("Should calculate values correctly in complete scenario")
    void testCompleteScenarioCalculations() {
        // Arrange
        kardexMigration.setProductId(4L);
        kardexMigration.setQuantity(200L);
        kardexMigration.setUnitPrice(new BigDecimal("15.50"));
        kardexMigration.setBalanceQuantity(150L);
        kardexMigration.setType(MovementType.PURCHASE);

        // Act
        BigDecimal movementValue = kardexMigration.calculateMovementValue();
        boolean hasStock = kardexMigration.hasBalanceStock();

        // Assert
        assertEquals(new BigDecimal("3100.00"), movementValue);
        assertTrue(hasStock);
        assertDoesNotThrow(() -> kardexMigration.validate());
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle migration for different movement types")
    void testMigrationForDifferentMovementTypes() {
        // Test PURCHASE
        kardexMigration.setProductId(1L);
        kardexMigration.setType(MovementType.PURCHASE);
        assertEquals(MovementType.PURCHASE, kardexMigration.getType());

        // Test SALE
        kardexMigration.setType(MovementType.SALE);
        assertEquals(MovementType.SALE, kardexMigration.getType());

        // Test PURCHASERETURN
        kardexMigration.setType(MovementType.PURCHASERETURN);
        assertEquals(MovementType.PURCHASERETURN, kardexMigration.getType());

        // Test SALESRETURN
        kardexMigration.setType(MovementType.SALESRETURN);
        assertEquals(MovementType.SALESRETURN, kardexMigration.getType());
    }

    @Test
    @DisplayName("Should handle very precise decimal calculations")
    void testPreciseDecimalCalculations() {
        // Arrange
        kardexMigration.setQuantity(3L);
        kardexMigration.setUnitPrice(new BigDecimal("33.333333"));

        // Act
        BigDecimal result = kardexMigration.calculateMovementValue();

        // Assert
        assertEquals(new BigDecimal("99.999999"), result);
    }

    @Test
    @DisplayName("Should maintain data integrity across operations")
    void testDataIntegrityAcrossOperations() {
        // Arrange
        kardexMigration.setProductId(5L);
        Long firstProductId = kardexMigration.getProductId();
        
        kardexMigration.setQuantity(100L);
        kardexMigration.setUnitPrice(new BigDecimal("10.00"));
        
        Long secondProductId = kardexMigration.getProductId();

        // Assert
        assertEquals(firstProductId, secondProductId);
        assertEquals(5L, kardexMigration.getProductId());
        assertDoesNotThrow(() -> kardexMigration.validate());
    }
}
