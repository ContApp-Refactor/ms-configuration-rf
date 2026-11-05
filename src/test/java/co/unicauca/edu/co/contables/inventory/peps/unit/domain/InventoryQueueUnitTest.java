package co.unicauca.edu.co.contables.inventory.peps.unit.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Balance;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.InventoryQueue;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.SaleDetail;

public class InventoryQueueUnitTest {
    private InventoryQueue queue;
    private Balance balance1;
    private Balance balance2;
    private Balance balance3;

    @BeforeEach
    void setUp(){
        queue = new InventoryQueue();
        
        balance1 = Balance.fromMovement(100, new BigDecimal("10.00"));
        balance2 = Balance.fromMovement(50, new BigDecimal("12.00"));
        balance3 = Balance.fromMovement(75, new BigDecimal("15.00"));

    }


    @Test
    @DisplayName("add balance successfully")
    void testAddBalance_ValidBalance() {
        // Act
        queue.addBalance(balance1);
        
        // Assert
        assertEquals(1, queue.size());
        assertEquals(100, queue.getTotalQuantity());
    }
    
    @Test
    @DisplayName("add multiple balances in FIFO order")
    void testAddBalance_MultipleBalances() {
        // Act
        queue.addBalance(balance1);
        queue.addBalance(balance2);
        queue.addBalance(balance3);
        
        // Assert
        assertEquals(3, queue.size());
        assertEquals(225, queue.getTotalQuantity());
        
        Optional<Balance> first = queue.getFirstBalance();
        assertTrue(first.isPresent());
        assertEquals(100, first.get().getQuantity());
    }
    
    @Test
    @DisplayName("throw exception when adding null balance")
    void testAddBalance_NullBalance() {
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> queue.addBalance(null)
        );
        
        assertEquals("Balance cannot be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when adding empty balance")
    void testAddBalance_EmptyBalance() {
        // Arrange
        Balance emptyBalance = Balance.fromMovement(10, new BigDecimal("10.00"));
        emptyBalance.reduceQuantity(10);
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> queue.addBalance(emptyBalance)
        );
        
        assertEquals("Cannot add empty balance", exception.getMessage());
    }
    
 
    @Test
    @DisplayName("add balance at the beginning")
    void testAddBalanceFirst_ValidBalance_AddsAtBeginning() {
        // Arrange
        queue.addBalance(balance1);
        queue.addBalance(balance2);
        
        // Act
        queue.addBalanceFirst(balance3);
        
        // Assert
        assertEquals(3, queue.size());
        Optional<Balance> first = queue.getFirstBalance();
        assertTrue(first.isPresent());
        assertEquals(75, first.get().getQuantity());
    }
    
    @Test
    @DisplayName("throw exception when adding null balance first")
    void testAddBalanceFirst_NullBalance() {
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> queue.addBalanceFirst(null)
        );
        
        assertEquals("Balance cannot be null", exception.getMessage());
    }
    
   
    @Test
    @DisplayName("consume quantity using FIFO")
    void testConsumeQuantity_SufficientStock_ConsumesFromFirst() {
        // Arrange
        queue.addBalance(balance1); 
        queue.addBalance(balance2); 
        
        // Act
        List<SaleDetail> details = queue.consumeQuantity(30);
        
        // Assert
        assertEquals(1, details.size());
        assertEquals(30, details.get(0).getQuantityUsed());
        assertEquals(70, balance1.getQuantity()); 
        assertEquals(120, queue.getTotalQuantity()); 
    }
    
    @Test
    @DisplayName("consume from multiple balances when needed")
    void testConsumeQuantity_MultipleBalances() {
        // Arrange
        queue.addBalance(balance1); 
        queue.addBalance(balance2); 
        
        // Act
        List<SaleDetail> details = queue.consumeQuantity(120);
        
        // Assert
        assertEquals(2, details.size());
        assertEquals(100, details.get(0).getQuantityUsed());
        assertEquals(20, details.get(1).getQuantityUsed());
        assertEquals(1, queue.size());
        assertEquals(30, queue.getTotalQuantity()); 
    }
    
    @Test
    @DisplayName("remove balance when fully consumed")
    void testConsumeQuantity_FullyConsumesBalance_RemovesFromQueue() {
        // Arrange
        queue.addBalance(balance1); 
        
        // Act
        List<SaleDetail> details = queue.consumeQuantity(100);
        
        // Assert
        assertEquals(1, details.size());
        assertEquals(100, details.get(0).getQuantityUsed());
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.getTotalQuantity());
    }
    
    @Test
    @DisplayName("throw exception when quantity is zero")
    void testConsumeQuantity_ZeroQuantity() {
        // Arrange
        queue.addBalance(balance1);
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> queue.consumeQuantity(0)
        );
        
        assertEquals("Quantity to consume must be positive", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when quantity is negative")
    void testConsumeQuantity_NegativeQuantity() {
        // Arrange
        queue.addBalance(balance1);
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> queue.consumeQuantity(-10)
        );
        
        assertEquals("Quantity to consume must be positive", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when insufficient stock")
    void testConsumeQuantity_InsufficientStock() {
        // Arrange
        queue.addBalance(balance1);
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> queue.consumeQuantity(150)
        );
        
        assertTrue(exception.getMessage().contains("Insufficient stock"));
        assertTrue(exception.getMessage().contains("Required: 150"));
        assertTrue(exception.getMessage().contains("Available: 100"));
    }
    

    @Test
    @DisplayName("consume quantity for report without stock validation-(consumes successfully)")
    void testConsumeQuantityForReport_ValidQuantity() {
        // Arrange
        queue.addBalance(balance1); 
        
        // Act
        List<SaleDetail> details = queue.consumeQuantityForReport(50);
        
        // Assert
        assertEquals(1, details.size());
        assertEquals(50, details.get(0).getQuantityUsed());
        assertEquals(50, queue.getTotalQuantity());
    }
    
    @Test
    @DisplayName("throw exception when report quantity is invalid")
    void testConsumeQuantityForReport_NegativeQuantity() {
        // Arrange
        queue.addBalance(balance1);
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> queue.consumeQuantityForReport(-5)
        );
        
        assertEquals("Quantity to consume must be positive", exception.getMessage());
    }
    

    @Test
    @DisplayName("remove quantity by matching unit price")
    void testRemoveByUnitPrice_MatchingPrice() {
        // Arrange
        queue.addBalance(balance1); 
        queue.addBalance(balance2);
        
        // Act
        List<SaleDetail> details = queue.removeByUnitPrice(30, new BigDecimal("10.00"));
        
        // Assert
        assertEquals(1, details.size());
        assertEquals(30, details.get(0).getQuantityUsed());
        assertEquals(70, balance1.getQuantity());
        assertEquals(120, queue.getTotalQuantity());
    }
    
    @Test
    @DisplayName("skip balances with different unit price")
    void testRemoveByUnitPrice_DifferentPrice_SkipsBalance() {
        // Arrange
        queue.addBalance(balance1); 
        queue.addBalance(balance2); 
        queue.addBalance(balance3); 
        
        // Act
        List<SaleDetail> details = queue.removeByUnitPrice(40, new BigDecimal("12.00"));
        
        // Assert
        assertEquals(1, details.size());
        assertEquals(40, details.get(0).getQuantityUsed());
        assertEquals(100, balance1.getQuantity()); 
        assertEquals(10, balance2.getQuantity()); 
        assertEquals(185, queue.getTotalQuantity());
    }
    
    @Test
    @DisplayName("throw exception when removing zero quantity by price")
    void testRemoveByUnitPrice_ZeroQuantity() {
        // Arrange
        queue.addBalance(balance1);
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> queue.removeByUnitPrice(0, new BigDecimal("10.00"))
        );
        
        assertEquals("Quantity to remove must be positive", exception.getMessage());
    }
    
    
    @Test
    @DisplayName("return true when has enough stock")
    void testHasEnoughStock_SufficientQuantity() {
        // Arrange
        queue.addBalance(balance1); 
        
        // Act y Assert
        assertTrue(queue.hasEnoughStock(50));
        assertTrue(queue.hasEnoughStock(100));
    }
    
    @Test
    @DisplayName("return false when insufficient stock")
    void testHasEnoughStock_InsufficientQuantity() {
        // Arrange
        queue.addBalance(balance1); 
        
        // Act y Assert
        assertFalse(queue.hasEnoughStock(150));
    }
    
    @Test
    @DisplayName("return false when queue is empty")
    void testHasEnoughStock_EmptyQueue() {
        // Act & Assert
        assertFalse(queue.hasEnoughStock(1));
    }
    
    
    @Test
    @DisplayName("calculate total quantity correctly")
    void testGetTotalQuantity_MultipleBalances() {
        // Arrange
        queue.addBalance(balance1); 
        queue.addBalance(balance2); 
        queue.addBalance(balance3); 
        
        // Act
        int total = queue.getTotalQuantity();
        
        // Assert
        assertEquals(225, total);
    }
    
    @Test
    @DisplayName("return zero for empty queue")
    void testGetTotalQuantity_EmptyQueue() {
        // Act y Assert
        assertEquals(0, queue.getTotalQuantity());
    }
    
    @Test
    @DisplayName("update total quantity after consumption")
    void testGetTotalQuantity_AfterConsumption_ReturnsUpdatedTotal() {
        // Arrange
        queue.addBalance(balance1); 
        queue.consumeQuantity(30);
        
        // Act
        int total = queue.getTotalQuantity();
        
        // Assert
        assertEquals(70, total);
    }
    
    @Test
    @DisplayName(" calculate total value correctly")
    void testGetTotalValue_MultipleBalances() {
        // Arrange
        queue.addBalance(balance1); 
        queue.addBalance(balance2); 
        
        // Act
        BigDecimal totalValue = queue.getTotalValue();
        
        // Assert
        assertEquals(0, new BigDecimal("1600.00").compareTo(totalValue));
    }
    
    @Test
    @DisplayName("Should return zero value for empty queue")
    void testGetTotalValue_EmptyQueue_ReturnsZero() {
        // Act
        BigDecimal totalValue = queue.getTotalValue();
        
        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(totalValue));
    }
    
    @Test
    @DisplayName("return true when queue is empty")
    void testIsEmpty_EmptyQueue() {
        // Act y Assert
        assertTrue(queue.isEmpty());
    }
    
    @Test
    @DisplayName("return false when queue has balances")
    void testIsEmpty_WithBalances() {
        // Arrange
        queue.addBalance(balance1);
        
        // Act y Assert
        assertFalse(queue.isEmpty());
    }
    
    @Test
    @DisplayName("return true after consuming all stock")
    void testIsEmpty_AfterFullConsumption() {
        // Arrange
        queue.addBalance(balance1);
        queue.consumeQuantity(100);
        
        // Act y Assert
        assertTrue(queue.isEmpty());
    }
    

    @Test
    @DisplayName("return independent copy of balances")
    void testGetBalancesCopy_WithBalances_ReturnsIndependentCopy() {
        // Arrange
        queue.addBalance(balance1);
        queue.addBalance(balance2);
        
        // Act
        List<Balance> copy = queue.getBalancesCopy();
        
        // Assert
        assertEquals(2, copy.size());
        
        // Modify copy
        copy.get(0).reduceQuantity(10);
        
        // Original should be unchanged
        assertEquals(100, balance1.getQuantity());
    }
    
    @Test
    @DisplayName("return empty list for empty queue")
    void testGetBalancesCopy_EmptyQueue() {
        // Act
        List<Balance> copy = queue.getBalancesCopy();
        
        // Assert
        assertNotNull(copy);
        assertTrue(copy.isEmpty());
    }
    
    @Test
    @DisplayName("return unmodifiable list")
    void testGetBalancesReadOnly_WithBalances() {
        // Arrange
        queue.addBalance(balance1);
        
        // Act
        List<Balance> readOnly = queue.getBalancesReadOnly();
        
        // Assert
        assertEquals(1, readOnly.size());
        assertThrows(UnsupportedOperationException.class, 
            () -> readOnly.add(balance2));
    }
    

    @Test
    @DisplayName("return first balance")
    void testGetFirstBalance_WithBalances() {
        // Arrange
        queue.addBalance(balance1);
        queue.addBalance(balance2);
        
        // Act
        Optional<Balance> first = queue.getFirstBalance();
        
        // Assert
        assertTrue(first.isPresent());
        assertEquals(100, first.get().getQuantity());
    }
    
    @Test
    @DisplayName("return empty optional when queue is empty")
    void testGetFirstBalance_EmptyQueue() {
        // Act
        Optional<Balance> first = queue.getFirstBalance();
        
        // Assert
        assertFalse(first.isPresent());
    }
    
 
    @Test
    @DisplayName("clear all balances")
    void testClear_WithBalances_RemovesAll() {
        // Arrange
        queue.addBalance(balance1);
        queue.addBalance(balance2);
        queue.addBalance(balance3);
        
        // Act
        queue.clear();
        
        // Assert
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
        assertEquals(0, queue.getTotalQuantity());
    }
    
 
    @Test
    @DisplayName("return correct size")
    void testSize_MultipleBalances() {
        // Arrange
        queue.addBalance(balance1);
        queue.addBalance(balance2);
        queue.addBalance(balance3);
        
        // Act y Assert
        assertEquals(3, queue.size());
    }
    
    @Test
    @DisplayName("return zero for empty queue")
    void testSize_EmptyQueue() {
        // Act y Assert
        assertEquals(0, queue.size());
    }
    
    @Test
    @DisplayName("update size after consumption")
    void testSize_AfterConsumption_ReturnsUpdatedSize() {
        // Arrange
        queue.addBalance(balance1);
        queue.addBalance(balance2);
        queue.consumeQuantity(100); // Fully consumes balance1
        
        // Act & Assert
        assertEquals(1, queue.size());
    }
    
    @Test
    @DisplayName("return formatted string")
    void testToString_WithBalances_ReturnsFormattedString() {
        // Arrange
        queue.addBalance(balance1);
        queue.addBalance(balance2);
        
        // Act
        String result = queue.toString();
        
        // Assert
        assertAll("ToString validation",
            () -> assertTrue(result.contains("size=2")),
            () -> assertTrue(result.contains("totalQuantity=150")),
            () -> assertTrue(result.contains("totalValue=")),
            () -> assertTrue(result.startsWith("InventoryQueue{"))
        );
    }
    
  
    @Test
    @DisplayName("maintain FIFO order through multiple operations")
    void testFIFOOrder_ComplexScenario_MaintainsCorrectOrder() {
        // Arrange
        queue.addBalance(balance1); 
        queue.addBalance(balance2); 
        queue.addBalance(balance3); 
        

        List<SaleDetail> details = queue.consumeQuantity(120);
        
        // Assert
        assertAll("FIFO order validation",
            () -> assertEquals(2, details.size()),
            () -> assertEquals(0, new BigDecimal("10.00").compareTo(details.get(0).getUnitPrice())),
            () -> assertEquals(0, new BigDecimal("12.00").compareTo(details.get(1).getUnitPrice())),
            () -> assertEquals(2, queue.size()),
            () -> assertEquals(105, queue.getTotalQuantity())
        );
    }


    



    
}
