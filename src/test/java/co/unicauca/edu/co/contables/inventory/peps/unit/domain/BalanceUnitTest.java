package co.unicauca.edu.co.contables.inventory.peps.unit.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Balance;

public class BalanceUnitTest {
    private BigDecimal validUnitPrice;

    @BeforeEach
    void setup(){
        validUnitPrice = new BigDecimal("10.50");
    }

    @Test
    @DisplayName("create balance from movement successfully")
    void testFromMovement_validparameters(){
        // Arrange
        int quantity = 100;
        BigDecimal unitPrice = new BigDecimal("15.00");
        BigDecimal expectedTotal = new BigDecimal("1500.00");

        // Act
        Balance balance = Balance.fromMovement(quantity, unitPrice);
        

        // Assert
        assertAll("Balance creation validation",
            () -> assertEquals(quantity, balance.getQuantity()),
            () -> assertEquals(0, unitPrice.compareTo(balance.getUnitPrice())),
            () -> assertEquals(0, expectedTotal.compareTo(balance.getTotalPrice()))
        );
        
    }

    @Test
    @DisplayName("calculate total price correctly")
    void testFromMovement_CalculatesTotalPrice() {
        // Arrange
        int quantity = 50;
        BigDecimal unitPrice = new BigDecimal("25.75");
        BigDecimal expectedTotal = new BigDecimal("1287.50");
        
        // Act
        Balance balance = Balance.fromMovement(quantity, unitPrice);
        
        // Assert
        assertEquals(0, expectedTotal.compareTo(balance.getTotalPrice()));
    }
    
    @Test
    @DisplayName("throw exception when quantity is negative")
    void testFromMovement_NegativeQuantity() {
        // Arrange
        int negativeQuantity = -10;
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Balance.fromMovement(negativeQuantity, validUnitPrice)
        );
        
        assertEquals("Quantity cannot be negative", exception.getMessage());
    }

    @Test
    @DisplayName("throw exception when unit price is null")
    void testFromMovement_NullUnitPrice() {
        // Arrange
        int quantity = 100;
        
        // Act y Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> Balance.fromMovement(quantity, null)
        );
        
        assertEquals("Unit price cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("throw exception when unit price is negative")
    void testFromMovement_NegativeUnitPrice() {
        // Arrange
        int quantity = 100;
        BigDecimal negativePrice = new BigDecimal("-5.00");
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Balance.fromMovement(quantity, negativePrice)
        );
        
        assertEquals("Unit price cannot be negative", exception.getMessage());
    }

    @Test
    @DisplayName("create balance with zero quantity -sucess")
    void testFromMovement_ZeroQuantity() {
        // Arrange
        int quantity = 0;
        
        // Act
        Balance balance = Balance.fromMovement(quantity, validUnitPrice);
        
        // Assert
        assertEquals(0, balance.getQuantity());
        assertEquals(0, BigDecimal.ZERO.compareTo(balance.getTotalPrice()));
    }

    @Test
    @DisplayName("create copy of balance successfully")
    void testCopy_ValidBalance_CreatesIndependentCopy() {
        // Arrange
        Balance original = Balance.fromMovement(100, new BigDecimal("20.00"));
        
        // Act
        Balance copy = Balance.copy(original);
        
        // Assert
        assertAll("Copy validation",
            () -> assertNotSame(original, copy),
            () -> assertEquals(original.getQuantity(), copy.getQuantity()),
            () -> assertEquals(0, original.getUnitPrice().compareTo(copy.getUnitPrice())),
            () -> assertEquals(0, original.getTotalPrice().compareTo(copy.getTotalPrice()))
        );
    }
    
    @Test
    @DisplayName("return null when copying null balance")
    void testCopy_NullBalance() {
        // Act
        Balance copy = Balance.copy(null);
        
        // Assert
        assertNull(copy);
    }
    
    @Test
    @DisplayName("create independent copy that does not affect original")
    void testCopy_ModifyingCopy_DoesNotAffectOriginal() {
        // Arrange
        Balance original = Balance.fromMovement(100, new BigDecimal("10.00"));
        Balance copy = Balance.copy(original);
        
        // Act
        copy.reduceQuantity(50);
        
        // Assert
        assertEquals(100, original.getQuantity());
        assertEquals(50, copy.getQuantity());
    }
    
    @Test
    @DisplayName("reduce quantity successfully")
    void testReduceQuantity_ValidAmount() {
        // Arrange
        Balance balance = Balance.fromMovement(100, new BigDecimal("10.00"));
        int amountToReduce = 30;
        
        // Act
        balance.reduceQuantity(amountToReduce);
        
        // Assert
        assertEquals(70, balance.getQuantity());
        assertEquals(0, new BigDecimal("700.00").compareTo(balance.getTotalPrice()));
    }
    
    @Test
    @DisplayName("recalculate total price after reduction -success")
    void testReduceQuantity_RecalculatesTotalPrice() {
        // Arrange
        Balance balance = Balance.fromMovement(50, new BigDecimal("15.50"));
        BigDecimal expectedTotal = new BigDecimal("310.00");
        
        // Act
        balance.reduceQuantity(30);
        
        // Assert
        assertEquals(0, expectedTotal.compareTo(balance.getTotalPrice()));
    }
    
    @Test
    @DisplayName("throw exception when reducing zero amount")
    void testReduceQuantity_ZeroAmount() {
        // Arrange
        Balance balance = Balance.fromMovement(100, validUnitPrice);
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> balance.reduceQuantity(0)
        );
        
        assertEquals("Amount to reduce must be positive", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when reducing negative amount")
    void testReduceQuantity_NegativeAmount() {
        // Arrange
        Balance balance = Balance.fromMovement(100, validUnitPrice);
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> balance.reduceQuantity(-10)
        );
        
        assertEquals("Amount to reduce must be positive", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when reducing more than available")
    void testReduceQuantity_ExceedsAvailable() {
        // Arrange
        Balance balance = Balance.fromMovement(50, validUnitPrice);
        int excessiveAmount = 100;
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> balance.reduceQuantity(excessiveAmount)
        );
        
        assertTrue(exception.getMessage().contains("Cannot reduce 100 from balance with only 50 available"));
    }
    
    @Test
    @DisplayName("reduce to zero successfully")
    void testReduceQuantity_ReduceToZero() {
        // Arrange
        Balance balance = Balance.fromMovement(100, validUnitPrice);
        
        // Act
        balance.reduceQuantity(100);
        
        // Assert
        assertEquals(0, balance.getQuantity());
        assertEquals(0, BigDecimal.ZERO.compareTo(balance.getTotalPrice()));
    }
    

    @Test
    @DisplayName("Should return true when can satisfy demand")
    void testCanSatisfyDemand_SufficientQuantity_ReturnsTrue() {
        // Arrange
        Balance balance = Balance.fromMovement(100, validUnitPrice);
        int demand = 50;
        
        // Act y Assert
        assertTrue(balance.canSatisfyDemand(demand));
    }
    
    @Test
    @DisplayName("return true when demand equals available quantity")
    void testCanSatisfyDemand_ExactQuantity() {
        // Arrange
        Balance balance = Balance.fromMovement(100, validUnitPrice);
        int demand = 100;
        
        // Act y Assert
        assertTrue(balance.canSatisfyDemand(demand));
    }
    
    @Test
    @DisplayName("return false when cannot satisfy demand")
    void testCanSatisfyDemand_InsufficientQuantity() {
        // Arrange
        Balance balance = Balance.fromMovement(50, validUnitPrice);
        int demand = 100;
        
        // Act y Assert
        assertFalse(balance.canSatisfyDemand(demand));
    }
    
    @Test
    @DisplayName("return false when demand exceeds available after reduction")
    void testCanSatisfyDemand_AfterReduction() {
        // Arrange
        Balance balance = Balance.fromMovement(100, validUnitPrice);
        balance.reduceQuantity(80); 
        int demand = 50;
        
        // Act y Assert
        assertFalse(balance.canSatisfyDemand(demand));
    }
    
    @Test
    @DisplayName("return false when balance has quantity")
    void testIsEmpty_WithQuantity() {
        // Arrange
        Balance balance = Balance.fromMovement(100, validUnitPrice);
        
        // Act & Assert
        assertFalse(balance.isEmpty());
    }
    
    @Test
    @DisplayName("Should return true when balance has zero quantity")
    void testIsEmpty_ZeroQuantity() {
        // Arrange
        Balance balance = Balance.fromMovement(0, validUnitPrice);
        
        // Act & Assert
        assertTrue(balance.isEmpty());
    }
    
    @Test
    @DisplayName("return true after reducing all quantity")
    void testIsEmpty_AfterFullReduction() {
        // Arrange
        Balance balance = Balance.fromMovement(100, validUnitPrice);
        
        // Act
        balance.reduceQuantity(100);
        
        // Assert
        assertTrue(balance.isEmpty());
    }
    
    @Test
    @DisplayName("return false with minimum quantity")
    void testIsEmpty_OneUnit() {
        // Arrange
        Balance balance = Balance.fromMovement(1, validUnitPrice);
        
        // Act y Assert
        assertFalse(balance.isEmpty());
    }

    @Test
    @DisplayName("calculate price for specific quantity")
    void testGetPriceForQuantity_ValidQuantity() {
        // Arrange
        Balance balance = Balance.fromMovement(100, new BigDecimal("12.50"));
        int requestedQty = 40;
        BigDecimal expectedPrice = new BigDecimal("500.00");
        
        // Act
        BigDecimal actualPrice = balance.getPriceForQuantity(requestedQty);
        
        // Assert
        assertEquals(0, expectedPrice.compareTo(actualPrice));
    }
    
    @Test
    @DisplayName("calculate price for full quantity(return total)")
    void testGetPriceForQuantity_FullQuantity() {
        // Arrange
        Balance balance = Balance.fromMovement(100, new BigDecimal("10.00"));
        
        // Act
        BigDecimal price = balance.getPriceForQuantity(100);
        
        // Assert
        assertEquals(0, balance.getTotalPrice().compareTo(price));
    }
    
    @Test
    @DisplayName("throw exception when quantity exceeds available")
    void testGetPriceForQuantity_ExceedsAvailable() {
        // Arrange
        Balance balance = Balance.fromMovement(50, validUnitPrice);
        int excessiveQty = 100;
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> balance.getPriceForQuantity(excessiveQty)
        );
        
        assertEquals("Requested quantity exceeds available quantity", exception.getMessage());
    }
    
    @Test
    @DisplayName("return zero for zero quantity")
    void testGetPriceForQuantity_ZeroQuantity() {
        // Arrange
        Balance balance = Balance.fromMovement(100, validUnitPrice);
        
        // Act
        BigDecimal price = balance.getPriceForQuantity(0);
        
        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(price));
    }

    @Test
    @DisplayName("recalculate total price correctly")
    void testRecalculateTotal_AfterManualChange() {
        // Arrange
        Balance balance = Balance.fromMovement(100, new BigDecimal("10.00"));
        balance.setQuantity(50);
        BigDecimal expectedTotal = new BigDecimal("500.00");
        
        // Act
        balance.recalculateTotal();
        
        // Assert
        assertEquals(0, expectedTotal.compareTo(balance.getTotalPrice()));
    }
    
    @Test
    @DisplayName("recalculate to zero when quantity is zero")
    void testRecalculateTotal_ZeroQuantity() {
        // Arrange
        Balance balance = Balance.fromMovement(100, validUnitPrice);
        balance.setQuantity(0);
        
        // Act
        balance.recalculateTotal();
        
        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(balance.getTotalPrice()));
    }


    @Test
    @DisplayName("return true when unit prices match")
    void testMatchesUnitPrice_SamePrice() {
        // Arrange
        Balance balance = Balance.fromMovement(100, new BigDecimal("15.50"));
        BigDecimal priceToCheck = new BigDecimal("15.50");
        
        // Act y Assert
        assertTrue(balance.matchesUnitPrice(priceToCheck));
    }
    
    @Test
    @DisplayName("return false when unit prices do not match")
    void testMatchesUnitPrice_DifferentPrice() {
        // Arrange
        Balance balance = Balance.fromMovement(100, new BigDecimal("15.50"));
        BigDecimal differentPrice = new BigDecimal("20.00");
        
        // Act y Assert
        assertFalse(balance.matchesUnitPrice(differentPrice));
    }
    
    @Test
    @DisplayName("match prices with different scales (return true)")
    void testMatchesUnitPrice_DifferentScales() {
        // Arrange
        Balance balance = Balance.fromMovement(100, new BigDecimal("10.50"));
        BigDecimal priceWithDifferentScale = new BigDecimal("10.500");
        
        // Act y Assert
        assertTrue(balance.matchesUnitPrice(priceWithDifferentScale));
    }
    
  
    @Test
    @DisplayName("be equal when same instance (return true)")
    void testEquals_SameInstance() {
        // Arrange
        Balance balance = Balance.fromMovement(100, validUnitPrice);
        
        // Act y Assert
        assertEquals(balance, balance);
    }
    
    @Test
    @DisplayName("be equal when all fields match (returns true)")
    void testEquals_SameValues() {
        // Arrange
        Balance balance1 = Balance.fromMovement(100, new BigDecimal("10.00"));
        Balance balance2 = Balance.fromMovement(100, new BigDecimal("10.00"));
        
        // Act y Assert
        assertEquals(balance1, balance2);
    }
    
    @Test
    @DisplayName("not be equal when quantity differs -(return false)")
    void testEquals_DifferentQuantity() {
        // Arrange
        Balance balance1 = Balance.fromMovement(100, validUnitPrice);
        Balance balance2 = Balance.fromMovement(50, validUnitPrice);
        
        // Act y Assert
        assertNotEquals(balance1, balance2);
    }
    
    @Test
    @DisplayName("not be equal when unit price differs -(return false)")
    void testEquals_DifferentUnitPrice() {
        // Arrange
        Balance balance1 = Balance.fromMovement(100, new BigDecimal("10.00"));
        Balance balance2 = Balance.fromMovement(100, new BigDecimal("15.00"));
        
        // Act y Assert
        assertNotEquals(balance1, balance2);
    }
    
    @Test
    @DisplayName("not be equal to null -(return false)")
    void testEquals_NullObject() {
        // Arrange
        Balance balance = Balance.fromMovement(100, validUnitPrice);
        
        // Act y Assert
        assertNotEquals(balance, null);
    }
    
    @Test
    @DisplayName("not be equal to different class -(return false)")
    void testEquals_DifferentClass() {
        // Arrange
        Balance balance = Balance.fromMovement(100, validUnitPrice);
        String differentObject = "Not a Balance";
        
        // Act y Assert
        assertNotEquals(balance, differentObject);
    }
    
    @Test
    @DisplayName("return same hashCode for equal objects")
    void testHashCode_EqualObjects_ReturnsSameHash() {
        // Arrange
        Balance balance1 = Balance.fromMovement(100, new BigDecimal("10.00"));
        Balance balance2 = Balance.fromMovement(100, new BigDecimal("10.00"));
        
        // Act y Assert
        assertEquals(balance1.hashCode(), balance2.hashCode());
    }
    
    @Test
    @DisplayName("return consistent hashCode")
    void testHashCode_MultipleInvocations_ReturnsConsistent() {
        // Arrange
        Balance balance = Balance.fromMovement(100, validUnitPrice);
        
        // Act
        int hash1 = balance.hashCode();
        int hash2 = balance.hashCode();
        int hash3 = balance.hashCode();
        
        // Assert
        assertAll("HashCode consistency",
            () -> assertEquals(hash1, hash2),
            () -> assertEquals(hash2, hash3)
        );
    }
    
    @Test
    @DisplayName("return formatted string with all fields")
    void testToString_ValidBalance_ReturnsFormattedString() {
        // Arrange
        Balance balance = Balance.fromMovement(100, new BigDecimal("10.50"));
        
        // Act
        String result = balance.toString();
        
        // Assert
        assertAll("ToString validation",
            () -> assertTrue(result.contains("quantity=100")),
            () -> assertTrue(result.contains("unitPrice=10.5")),
            () -> assertTrue(result.contains("totalPrice=")),
            () -> assertTrue(result.startsWith("Balance{")),
            () -> assertTrue(result.endsWith("}"))
        );
    }
    
    @Test
    @DisplayName("return correct string after reduction")
    void testToString_AfterReduction_ReflectsChanges() {
        // Arrange
        Balance balance = Balance.fromMovement(100, new BigDecimal("10.00"));
        balance.reduceQuantity(40);
        
        // Act
        String result = balance.toString();
        
        // Assert
        assertTrue(result.contains("quantity=60"));
    }
    
    @Test
    @DisplayName("create balance using builder")
    void testBuilder_ValidValues() {
        // Arrange & Act
        Balance balance = Balance.builder()
            .quantity(50)
            .unitPrice(new BigDecimal("20.00"))
            .totalPrice(new BigDecimal("1000.00"))
            .build();
        
        // Assert
        assertAll("Builder validation",
            () -> assertEquals(50, balance.getQuantity()),
            () -> assertEquals(0, new BigDecimal("20.00").compareTo(balance.getUnitPrice())),
            () -> assertEquals(0, new BigDecimal("1000.00").compareTo(balance.getTotalPrice()))
        );
    }
    





    
}
