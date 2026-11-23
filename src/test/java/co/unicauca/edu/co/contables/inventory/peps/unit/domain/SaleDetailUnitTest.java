package co.unicauca.edu.co.contables.inventory.peps.unit.domain;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Balance;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.SaleDetail;

/**
 * @brief Unit tests for SaleDetail domain model
 * 
 * Tests creation methods, validation, calculations, and business rules
 * for sale detail records.
 */
@DisplayName("SaleDetail Domain Model Unit Tests")
class SaleDetailUnitTest {
    
    private int validQuantity;
    private BigDecimal validUnitPrice;
    private BigDecimal validTotalPrice;
    
    @BeforeEach
    void setUp() {
        validQuantity = 10;
        validUnitPrice = new BigDecimal("15.50");
        validTotalPrice = new BigDecimal("155.00");
    }
    
    // ==================== Constructor & Builder Tests ====================
    
    @Test
    @DisplayName("Should create SaleDetail using builder")
    void testBuilder_ValidData_CreatesInstance() {
        // Act
        SaleDetail saleDetail = SaleDetail.builder()
                .quantityUsed(validQuantity)
                .unitPrice(validUnitPrice)
                .totalPrice(validTotalPrice)
                .build();
        
        // Assert
        assertNotNull(saleDetail);
        assertEquals(validQuantity, saleDetail.getQuantityUsed());
        assertEquals(validUnitPrice, saleDetail.getUnitPrice());
        assertEquals(validTotalPrice, saleDetail.getTotalPrice());
    }
    
    @Test
    @DisplayName("Should create SaleDetail using no-args constructor")
    void testNoArgsConstructor_CreatesEmptyInstance() {
        // Act
        SaleDetail saleDetail = new SaleDetail();
        
        // Assert
        assertNotNull(saleDetail);
        assertEquals(0, saleDetail.getQuantityUsed());
        assertNull(saleDetail.getUnitPrice());
        assertNull(saleDetail.getTotalPrice());
    }
    
    @Test
    @DisplayName("Should create SaleDetail using all-args constructor")
    void testAllArgsConstructor_ValidData_CreatesInstance() {
        // Act
        SaleDetail saleDetail = new SaleDetail(validQuantity, validUnitPrice, validTotalPrice);
        
        // Assert
        assertEquals(validQuantity, saleDetail.getQuantityUsed());
        assertEquals(validUnitPrice, saleDetail.getUnitPrice());
        assertEquals(validTotalPrice, saleDetail.getTotalPrice());
    }
    
    // ==================== fromLotConsumption() Tests ====================
    
    @Test
    @DisplayName("Should create SaleDetail from lot consumption with valid data")
    void testFromLotConsumption_ValidData_CreatesCorrectly() {
        // Arrange
        int quantity = 5;
        BigDecimal unitPrice = new BigDecimal("10.00");
        
        // Act
        SaleDetail saleDetail = SaleDetail.fromLotConsumption(quantity, unitPrice);
        
        // Assert
        assertNotNull(saleDetail);
        assertEquals(5, saleDetail.getQuantityUsed());
        assertEquals(0, new BigDecimal("10.00").compareTo(saleDetail.getUnitPrice()));
        assertEquals(0, new BigDecimal("50.00").compareTo(saleDetail.getTotalPrice()));
    }
    
    @Test
    @DisplayName("Should calculate total price correctly in fromLotConsumption")
    void testFromLotConsumption_CalculatesTotalPrice() {
        // Arrange
        int quantity = 100;
        BigDecimal unitPrice = new BigDecimal("7.25");
        
        // Act
        SaleDetail saleDetail = SaleDetail.fromLotConsumption(quantity, unitPrice);
        
        // Assert
        BigDecimal expectedTotal = new BigDecimal("725.00");
        assertEquals(0, expectedTotal.compareTo(saleDetail.getTotalPrice()));
    }
    
    @Test
    @DisplayName("Should throw exception when quantity is zero")
    void testFromLotConsumption_ZeroQuantity_ThrowsException() {
        // Arrange
        BigDecimal unitPrice = new BigDecimal("10.00");
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SaleDetail.fromLotConsumption(0, unitPrice)
        );
        
        assertEquals("Quantity used must be positive", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when quantity is negative")
    void testFromLotConsumption_NegativeQuantity_ThrowsException() {
        // Arrange
        BigDecimal unitPrice = new BigDecimal("10.00");
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SaleDetail.fromLotConsumption(-5, unitPrice)
        );
        
        assertEquals("Quantity used must be positive", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when unit price is null")
    void testFromLotConsumption_NullUnitPrice_ThrowsException() {
        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> SaleDetail.fromLotConsumption(10, null)
        );
        
        assertEquals("Unit price cannot be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when unit price is negative")
    void testFromLotConsumption_NegativeUnitPrice_ThrowsException() {
        // Arrange
        BigDecimal negativePrice = new BigDecimal("-10.00");
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SaleDetail.fromLotConsumption(10, negativePrice)
        );
        
        assertEquals("Unit price cannot be negative", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should accept zero unit price")
    void testFromLotConsumption_ZeroUnitPrice_Accepted() {
        // Arrange
        BigDecimal zeroPrice = BigDecimal.ZERO;
        
        // Act
        SaleDetail saleDetail = SaleDetail.fromLotConsumption(10, zeroPrice);
        
        // Assert
        assertNotNull(saleDetail);
        assertEquals(0, BigDecimal.ZERO.compareTo(saleDetail.getTotalPrice()));
    }
    
    // ==================== fromBalance() Tests ====================
    
    @Test
    @DisplayName("Should create SaleDetail from balance with sufficient quantity")
    void testFromBalance_SufficientQuantity_CreatesCorrectly() {
        // Arrange
        Balance balance = Balance.builder()
                .quantity(20)
                .unitPrice(new BigDecimal("12.50"))
                .totalPrice(new BigDecimal("250.00"))
                .build();
        int quantityConsumed = 10;
        
        // Act
        SaleDetail saleDetail = SaleDetail.fromBalance(balance, quantityConsumed);
        
        // Assert
        assertNotNull(saleDetail);
        assertEquals(10, saleDetail.getQuantityUsed());
        assertEquals(0, new BigDecimal("12.50").compareTo(saleDetail.getUnitPrice()));
        assertEquals(0, new BigDecimal("125.00").compareTo(saleDetail.getTotalPrice()));
    }
    
    @Test
    @DisplayName("Should create SaleDetail consuming full balance")
    void testFromBalance_ConsumeFullBalance_CreatesCorrectly() {
        // Arrange
        Balance balance = Balance.builder()
                .quantity(15)
                .unitPrice(new BigDecimal("8.00"))
                .totalPrice(new BigDecimal("120.00"))
                .build();
        
        // Act
        SaleDetail saleDetail = SaleDetail.fromBalance(balance, 15);
        
        // Assert
        assertEquals(15, saleDetail.getQuantityUsed());
        assertEquals(0, new BigDecimal("120.00").compareTo(saleDetail.getTotalPrice()));
    }
    
    @Test
    @DisplayName("Should throw exception when balance cannot satisfy demand")
    void testFromBalance_InsufficientQuantity_ThrowsException() {
        // Arrange
        Balance balance = Balance.builder()
                .quantity(5)
                .unitPrice(new BigDecimal("10.00"))
                .totalPrice(new BigDecimal("50.00"))
                .build();
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SaleDetail.fromBalance(balance, 10)
        );
        
        assertEquals("Balance cannot satisfy demand", exception.getMessage());
    }
    
    // ==================== getAverageUnitPrice() Tests ====================
    
    @Test
    @DisplayName("Should calculate average unit price correctly")
    void testGetAverageUnitPrice_ValidData_CalculatesCorrectly() {
        // Arrange
        SaleDetail saleDetail = SaleDetail.builder()
                .quantityUsed(10)
                .unitPrice(new BigDecimal("15.50"))
                .totalPrice(new BigDecimal("155.00"))
                .build();
        
        // Act
        BigDecimal average = saleDetail.getAverageUnitPrice();
        
        // Assert
        assertEquals(0, new BigDecimal("15.50").compareTo(average));
    }
    
    @Test
    @DisplayName("Should round average unit price to 2 decimals")
    void testGetAverageUnitPrice_RoundsCorrectly() {
        // Arrange
        SaleDetail saleDetail = SaleDetail.builder()
                .quantityUsed(3)
                .unitPrice(new BigDecimal("10.00"))
                .totalPrice(new BigDecimal("10.00"))
                .build();
        
        // Act
        BigDecimal average = saleDetail.getAverageUnitPrice();
        
        // Assert
        BigDecimal expected = new BigDecimal("3.33");
        assertEquals(0, expected.compareTo(average));
    }
    
    @Test
    @DisplayName("Should return zero when quantity is zero")
    void testGetAverageUnitPrice_ZeroQuantity_ReturnsZero() {
        // Arrange
        SaleDetail saleDetail = SaleDetail.builder()
                .quantityUsed(0)
                .totalPrice(new BigDecimal("100.00"))
                .build();
        
        // Act
        BigDecimal average = saleDetail.getAverageUnitPrice();
        
        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(average));
    }
    
    @Test
    @DisplayName("Should return zero when quantity is negative")
    void testGetAverageUnitPrice_NegativeQuantity_ReturnsZero() {
        // Arrange
        SaleDetail saleDetail = SaleDetail.builder()
                .quantityUsed(-5)
                .totalPrice(new BigDecimal("50.00"))
                .build();
        
        // Act
        BigDecimal average = saleDetail.getAverageUnitPrice();
        
        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(average));
    }
    
    // ==================== recalculateTotal() Tests ====================
    
    @Test
    @DisplayName("Should recalculate total price correctly")
    void testRecalculateTotal_UpdatesTotalPrice() {
        // Arrange
        SaleDetail saleDetail = SaleDetail.builder()
                .quantityUsed(10)
                .unitPrice(new BigDecimal("5.00"))
                .totalPrice(BigDecimal.ZERO) // Wrong initial value
                .build();
        
        // Act
        saleDetail.recalculateTotal();
        
        // Assert
        assertEquals(0, new BigDecimal("50.00").compareTo(saleDetail.getTotalPrice()));
    }
    
    @Test
    @DisplayName("Should update total when quantity changes")
    void testRecalculateTotal_AfterQuantityChange_UpdatesCorrectly() {
        // Arrange
        SaleDetail saleDetail = SaleDetail.builder()
                .quantityUsed(10)
                .unitPrice(new BigDecimal("8.00"))
                .totalPrice(new BigDecimal("80.00"))
                .build();
        
        // Act
        saleDetail.setQuantityUsed(15);
        saleDetail.recalculateTotal();
        
        // Assert
        assertEquals(0, new BigDecimal("120.00").compareTo(saleDetail.getTotalPrice()));
    }
    
    @Test
    @DisplayName("Should update total when unit price changes")
    void testRecalculateTotal_AfterPriceChange_UpdatesCorrectly() {
        // Arrange
        SaleDetail saleDetail = SaleDetail.builder()
                .quantityUsed(5)
                .unitPrice(new BigDecimal("10.00"))
                .totalPrice(new BigDecimal("50.00"))
                .build();
        
        // Act
        saleDetail.setUnitPrice(new BigDecimal("12.00"));
        saleDetail.recalculateTotal();
        
        // Assert
        assertEquals(0, new BigDecimal("60.00").compareTo(saleDetail.getTotalPrice()));
    }
    
    // ==================== isValid() Tests ====================
    
    @Test
    @DisplayName("Should return true for valid sale detail")
    void testIsValid_ValidData_ReturnsTrue() {
        // Arrange
        SaleDetail saleDetail = SaleDetail.fromLotConsumption(10, new BigDecimal("5.00"));
        
        // Act & Assert
        assertTrue(saleDetail.isValid());
    }
    
    @Test
    @DisplayName("Should return false when quantity is zero")
    void testIsValid_ZeroQuantity_ReturnsFalse() {
        // Arrange
        SaleDetail saleDetail = SaleDetail.builder()
                .quantityUsed(0)
                .unitPrice(new BigDecimal("10.00"))
                .totalPrice(BigDecimal.ZERO)
                .build();
        
        // Act & Assert
        assertFalse(saleDetail.isValid());
    }
    
    @Test
    @DisplayName("Should return false when quantity is negative")
    void testIsValid_NegativeQuantity_ReturnsFalse() {
        // Arrange
        SaleDetail saleDetail = SaleDetail.builder()
                .quantityUsed(-5)
                .unitPrice(new BigDecimal("10.00"))
                .totalPrice(new BigDecimal("-50.00"))
                .build();
        
        // Act & Assert
        assertFalse(saleDetail.isValid());
    }
    
    @Test
    @DisplayName("Should return false when unit price is null")
    void testIsValid_NullUnitPrice_ReturnsFalse() {
        // Arrange
        SaleDetail saleDetail = SaleDetail.builder()
                .quantityUsed(10)
                .unitPrice(null)
                .totalPrice(BigDecimal.ZERO)
                .build();
        
        // Act & Assert
        assertFalse(saleDetail.isValid());
    }
    
    @Test
    @DisplayName("Should return false when unit price is negative")
    void testIsValid_NegativeUnitPrice_ReturnsFalse() {
        // Arrange
        SaleDetail saleDetail = SaleDetail.builder()
                .quantityUsed(10)
                .unitPrice(new BigDecimal("-5.00"))
                .totalPrice(new BigDecimal("-50.00"))
                .build();
        
        // Act & Assert
        assertFalse(saleDetail.isValid());
    }
    
    @Test
    @DisplayName("Should return false when total price is null")
    void testIsValid_NullTotalPrice_ReturnsFalse() {
        // Arrange
        SaleDetail saleDetail = SaleDetail.builder()
                .quantityUsed(10)
                .unitPrice(new BigDecimal("5.00"))
                .totalPrice(null)
                .build();
        
        // Act & Assert
        assertFalse(saleDetail.isValid());
    }
    
    @Test
    @DisplayName("Should return true when unit price is zero")
    void testIsValid_ZeroUnitPrice_ReturnsTrue() {
        // Arrange
        SaleDetail saleDetail = SaleDetail.builder()
                .quantityUsed(10)
                .unitPrice(BigDecimal.ZERO)
                .totalPrice(BigDecimal.ZERO)
                .build();
        
        // Act & Assert
        assertTrue(saleDetail.isValid());
    }
    
    // ==================== equals() & hashCode() Tests ====================
    
    @Test
    @DisplayName("Should be equal to itself")
    void testEquals_SameObject_ReturnsTrue() {
        // Arrange
        SaleDetail saleDetail = SaleDetail.fromLotConsumption(10, new BigDecimal("5.00"));
        
        // Act & Assert
        assertEquals(saleDetail, saleDetail);
    }
    
    @Test
    @DisplayName("Should be equal to another instance with same values")
    void testEquals_SameValues_ReturnsTrue() {
        // Arrange
        SaleDetail saleDetail1 = SaleDetail.fromLotConsumption(10, new BigDecimal("5.00"));
        SaleDetail saleDetail2 = SaleDetail.fromLotConsumption(10, new BigDecimal("5.00"));
        
        // Act & Assert
        assertEquals(saleDetail1, saleDetail2);
        assertEquals(saleDetail1.hashCode(), saleDetail2.hashCode());
    }
    
    @Test
    @DisplayName("Should not be equal to null")
    void testEquals_Null_ReturnsFalse() {
        // Arrange
        SaleDetail saleDetail = SaleDetail.fromLotConsumption(10, new BigDecimal("5.00"));
        
        // Act & Assert
        assertNotEquals(null, saleDetail);
    }
    
    @Test
    @DisplayName("Should not be equal to different type")
    void testEquals_DifferentType_ReturnsFalse() {
        // Arrange
        SaleDetail saleDetail = SaleDetail.fromLotConsumption(10, new BigDecimal("5.00"));
        String differentType = "Not a SaleDetail";
        
        // Act & Assert
        assertNotEquals(saleDetail, differentType);
    }
    
    @Test
    @DisplayName("Should not be equal when quantity differs")
    void testEquals_DifferentQuantity_ReturnsFalse() {
        // Arrange
        SaleDetail saleDetail1 = SaleDetail.fromLotConsumption(10, new BigDecimal("5.00"));
        SaleDetail saleDetail2 = SaleDetail.fromLotConsumption(15, new BigDecimal("5.00"));
        
        // Act & Assert
        assertNotEquals(saleDetail1, saleDetail2);
    }
    
    @Test
    @DisplayName("Should not be equal when unit price differs")
    void testEquals_DifferentUnitPrice_ReturnsFalse() {
        // Arrange
        SaleDetail saleDetail1 = SaleDetail.fromLotConsumption(10, new BigDecimal("5.00"));
        SaleDetail saleDetail2 = SaleDetail.fromLotConsumption(10, new BigDecimal("6.00"));
        
        // Act & Assert
        assertNotEquals(saleDetail1, saleDetail2);
    }
    
    // ==================== toString() Tests ====================
    
    @Test
    @DisplayName("Should generate formatted string representation")
    void testToString_GeneratesCorrectFormat() {
        // Arrange
        SaleDetail saleDetail = SaleDetail.fromLotConsumption(10, new BigDecimal("5.50"));
        
        // Act
        String result = saleDetail.toString();
        
        // Assert
        assertTrue(result.contains("SaleDetail"));
        assertTrue(result.contains("quantityUsed=10"));
        assertTrue(result.contains("unitPrice=5.5"));
        assertTrue(result.contains("totalPrice=55.0"));
    }
    
    // ==================== Edge Cases & Business Rules ====================
    
    @Test
    @DisplayName("Should handle large quantities")
    void testFromLotConsumption_LargeQuantity_HandlesCorrectly() {
        // Arrange
        int largeQuantity = 1_000_000;
        BigDecimal unitPrice = new BigDecimal("0.01");
        
        // Act
        SaleDetail saleDetail = SaleDetail.fromLotConsumption(largeQuantity, unitPrice);
        
        // Assert
        assertEquals(largeQuantity, saleDetail.getQuantityUsed());
        assertEquals(0, new BigDecimal("10000.00").compareTo(saleDetail.getTotalPrice()));
    }
    
    @Test
    @DisplayName("Should handle very small unit prices")
    void testFromLotConsumption_SmallPrice_HandlesCorrectly() {
        // Arrange
        BigDecimal smallPrice = new BigDecimal("0.0001");
        
        // Act
        SaleDetail saleDetail = SaleDetail.fromLotConsumption(100, smallPrice);
        
        // Assert
        assertEquals(0, new BigDecimal("0.0100").compareTo(saleDetail.getTotalPrice()));
    }
    
    @Test
    @DisplayName("Should handle very large unit prices")
    void testFromLotConsumption_LargePrice_HandlesCorrectly() {
        // Arrange
        BigDecimal largePrice = new BigDecimal("999999.99");
        
        // Act
        SaleDetail saleDetail = SaleDetail.fromLotConsumption(2, largePrice);
        
        // Assert
        assertEquals(0, new BigDecimal("1999999.98").compareTo(saleDetail.getTotalPrice()));
    }
    
    @Test
    @DisplayName("Should maintain precision in calculations")
    void testFromLotConsumption_MaintainsPrecision() {
        // Arrange
        int quantity = 3;
        BigDecimal unitPrice = new BigDecimal("33.333333");
        
        // Act
        SaleDetail saleDetail = SaleDetail.fromLotConsumption(quantity, unitPrice);
        
        // Assert
        BigDecimal expectedTotal = new BigDecimal("99.999999");
        assertEquals(0, expectedTotal.compareTo(saleDetail.getTotalPrice()));
    }
}
