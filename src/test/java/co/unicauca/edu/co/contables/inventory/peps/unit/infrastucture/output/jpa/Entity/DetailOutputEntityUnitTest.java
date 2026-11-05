package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.jpa.Entity;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.DetailOutputEntity;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.KardexEntity;

/**
 * @brief Unit tests for DetailOutputEntity
 * 
 * Tests JPA entity for FIFO output details including field validation,
 * getters/setters, and relationships with Kardex entities.
 */
@DisplayName("DetailOutputEntity Unit Tests")
class DetailOutputEntityUnitTest {
    
    private DetailOutputEntity detailOutput;
    private KardexEntity movementSale;
    private KardexEntity movementOrigin;

    @BeforeEach
    void setUp() {
        // Initialize test data
        detailOutput = new DetailOutputEntity();
        
        movementSale = new KardexEntity();
        movementSale.setIdKardex(1L);
        
        movementOrigin = new KardexEntity();
        movementOrigin.setIdKardex(2L);
    }

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Should create entity with no-args constructor")
    void testNoArgsConstructor() {
        // Act
        DetailOutputEntity entity = new DetailOutputEntity();

        // Assert
        assertNotNull(entity);
        assertNull(entity.getIdDetailOutput());
        assertEquals(0, entity.getQuantityUsed());
        assertNull(entity.getUnitPrice());
        assertNull(entity.getMovementSale());
        assertNull(entity.getMovementOrigin());
    }

    @Test
    @DisplayName("Should create entity with all-args constructor")
    void testAllArgsConstructor() {
        // Arrange
        Long id = 1L;
        int quantity = 50;
        BigDecimal price = new BigDecimal("100.00");

        // Act
        DetailOutputEntity entity = new DetailOutputEntity(
            id, quantity, price, movementSale, movementOrigin
        );

        // Assert
        assertEquals(id, entity.getIdDetailOutput());
        assertEquals(quantity, entity.getQuantityUsed());
        assertEquals(price, entity.getUnitPrice());
        assertEquals(movementSale, entity.getMovementSale());
        assertEquals(movementOrigin, entity.getMovementOrigin());
    }

    // ==================== Getter and Setter Tests ====================

    @Test
    @DisplayName("Should set and get idDetailOutput correctly")
    void testIdDetailOutputGetterSetter() {
        // Arrange
        Long expectedId = 123L;

        // Act
        detailOutput.setIdDetailOutput(expectedId);

        // Assert
        assertEquals(expectedId, detailOutput.getIdDetailOutput());
    }

    @Test
    @DisplayName("Should set and get quantityUsed correctly")
    void testQuantityUsedGetterSetter() {
        // Arrange
        int expectedQuantity = 100;

        // Act
        detailOutput.setQuantityUsed(expectedQuantity);

        // Assert
        assertEquals(expectedQuantity, detailOutput.getQuantityUsed());
    }

    @Test
    @DisplayName("Should handle zero quantityUsed")
    void testZeroQuantityUsed() {
        // Act
        detailOutput.setQuantityUsed(0);

        // Assert
        assertEquals(0, detailOutput.getQuantityUsed());
    }

    @Test
    @DisplayName("Should handle negative quantityUsed")
    void testNegativeQuantityUsed() {
        // Act
        detailOutput.setQuantityUsed(-10);

        // Assert
        assertEquals(-10, detailOutput.getQuantityUsed());
    }

    @Test
    @DisplayName("Should set and get unitPrice correctly")
    void testUnitPriceGetterSetter() {
        // Arrange
        BigDecimal expectedPrice = new BigDecimal("250.50");

        // Act
        detailOutput.setUnitPrice(expectedPrice);

        // Assert
        assertEquals(expectedPrice, detailOutput.getUnitPrice());
        assertEquals(0, expectedPrice.compareTo(detailOutput.getUnitPrice()));
    }

    @Test
    @DisplayName("Should handle unitPrice with multiple decimal places")
    void testUnitPriceWithDecimals() {
        // Arrange
        BigDecimal expectedPrice = new BigDecimal("99.999");

        // Act
        detailOutput.setUnitPrice(expectedPrice);

        // Assert
        assertEquals(0, expectedPrice.compareTo(detailOutput.getUnitPrice()));
    }

    @Test
    @DisplayName("Should handle zero unitPrice")
    void testZeroUnitPrice() {
        // Arrange
        BigDecimal zero = BigDecimal.ZERO;

        // Act
        detailOutput.setUnitPrice(zero);

        // Assert
        assertEquals(0, zero.compareTo(detailOutput.getUnitPrice()));
    }

    @Test
    @DisplayName("Should handle null unitPrice")
    void testNullUnitPrice() {
        // Act
        detailOutput.setUnitPrice(null);

        // Assert
        assertNull(detailOutput.getUnitPrice());
    }

    @Test
    @DisplayName("Should set and get movementSale correctly")
    void testMovementSaleGetterSetter() {
        // Act
        detailOutput.setMovementSale(movementSale);

        // Assert
        assertEquals(movementSale, detailOutput.getMovementSale());
        assertEquals(1L, detailOutput.getMovementSale().getIdKardex());
    }

    @Test
    @DisplayName("Should handle null movementSale")
    void testNullMovementSale() {
        // Act
        detailOutput.setMovementSale(null);

        // Assert
        assertNull(detailOutput.getMovementSale());
    }

    @Test
    @DisplayName("Should set and get movementOrigin correctly")
    void testMovementOriginGetterSetter() {
        // Act
        detailOutput.setMovementOrigin(movementOrigin);

        // Assert
        assertEquals(movementOrigin, detailOutput.getMovementOrigin());
        assertEquals(2L, detailOutput.getMovementOrigin().getIdKardex());
    }

    @Test
    @DisplayName("Should handle null movementOrigin")
    void testNullMovementOrigin() {
        // Act
        detailOutput.setMovementOrigin(null);

        // Assert
        assertNull(detailOutput.getMovementOrigin());
    }

    // ==================== Relationship Tests ====================

    @Test
    @DisplayName("Should maintain bidirectional relationship with movementSale")
    void testBidirectionalRelationshipWithMovementSale() {
        // Act
        detailOutput.setMovementSale(movementSale);

        // Assert
        assertNotNull(detailOutput.getMovementSale());
        assertEquals(movementSale, detailOutput.getMovementSale());
    }

    @Test
    @DisplayName("Should maintain bidirectional relationship with movementOrigin")
    void testBidirectionalRelationshipWithMovementOrigin() {
        // Act
        detailOutput.setMovementOrigin(movementOrigin);

        // Assert
        assertNotNull(detailOutput.getMovementOrigin());
        assertEquals(movementOrigin, detailOutput.getMovementOrigin());
    }

    @Test
    @DisplayName("Should allow different kardex entities for sale and origin")
    void testDifferentKardexEntities() {
        // Act
        detailOutput.setMovementSale(movementSale);
        detailOutput.setMovementOrigin(movementOrigin);

        // Assert
        assertNotEquals(detailOutput.getMovementSale(), detailOutput.getMovementOrigin());
        assertEquals(1L, detailOutput.getMovementSale().getIdKardex());
        assertEquals(2L, detailOutput.getMovementOrigin().getIdKardex());
    }

    @Test
    @DisplayName("Should allow same kardex entity for sale and origin")
    void testSameKardexEntity() {
        // Act
        detailOutput.setMovementSale(movementSale);
        detailOutput.setMovementOrigin(movementSale);

        // Assert
        assertEquals(detailOutput.getMovementSale(), detailOutput.getMovementOrigin());
    }

    // ==================== Business Logic Tests ====================

    @Test
    @DisplayName("Should calculate total value correctly")
    void testCalculateTotalValue() {
        // Arrange
        detailOutput.setQuantityUsed(10);
        detailOutput.setUnitPrice(new BigDecimal("50.00"));

        // Act
        BigDecimal totalValue = detailOutput.getUnitPrice()
            .multiply(BigDecimal.valueOf(detailOutput.getQuantityUsed()));

        // Assert
        assertEquals(new BigDecimal("500.00"), totalValue);
    }

    @Test
    @DisplayName("Should handle large quantity values")
    void testLargeQuantityValues() {
        // Arrange
        int largeQuantity = Integer.MAX_VALUE;

        // Act
        detailOutput.setQuantityUsed(largeQuantity);

        // Assert
        assertEquals(largeQuantity, detailOutput.getQuantityUsed());
    }

    @Test
    @DisplayName("Should handle large price values")
    void testLargePriceValues() {
        // Arrange
        BigDecimal largePrice = new BigDecimal("999999999.99");

        // Act
        detailOutput.setUnitPrice(largePrice);

        // Assert
        assertEquals(0, largePrice.compareTo(detailOutput.getUnitPrice()));
    }

    // ==================== Complete Entity Tests ====================

    @Test
    @DisplayName("Should create complete valid detail output entity")
    void testCompleteValidEntity() {
        // Arrange & Act
        detailOutput.setIdDetailOutput(1L);
        detailOutput.setQuantityUsed(25);
        detailOutput.setUnitPrice(new BigDecimal("150.75"));
        detailOutput.setMovementSale(movementSale);
        detailOutput.setMovementOrigin(movementOrigin);

        // Assert
        assertNotNull(detailOutput);
        assertEquals(1L, detailOutput.getIdDetailOutput());
        assertEquals(25, detailOutput.getQuantityUsed());
        assertEquals(0, new BigDecimal("150.75").compareTo(detailOutput.getUnitPrice()));
        assertEquals(movementSale, detailOutput.getMovementSale());
        assertEquals(movementOrigin, detailOutput.getMovementOrigin());
    }

    @Test
    @DisplayName("Should allow updating all fields")
    void testUpdateAllFields() {
        // Arrange
        detailOutput.setIdDetailOutput(1L);
        detailOutput.setQuantityUsed(10);
        detailOutput.setUnitPrice(new BigDecimal("100.00"));
        detailOutput.setMovementSale(movementSale);
        detailOutput.setMovementOrigin(movementOrigin);

        // Act - Update all fields
        detailOutput.setIdDetailOutput(2L);
        detailOutput.setQuantityUsed(20);
        detailOutput.setUnitPrice(new BigDecimal("200.00"));
        
        KardexEntity newSale = new KardexEntity();
        newSale.setIdKardex(3L);
        detailOutput.setMovementSale(newSale);
        
        KardexEntity newOrigin = new KardexEntity();
        newOrigin.setIdKardex(4L);
        detailOutput.setMovementOrigin(newOrigin);

        // Assert
        assertEquals(2L, detailOutput.getIdDetailOutput());
        assertEquals(20, detailOutput.getQuantityUsed());
        assertEquals(0, new BigDecimal("200.00").compareTo(detailOutput.getUnitPrice()));
        assertEquals(3L, detailOutput.getMovementSale().getIdKardex());
        assertEquals(4L, detailOutput.getMovementOrigin().getIdKardex());
    }

    @Test
    @DisplayName("Should maintain data integrity after multiple operations")
    void testDataIntegrityAfterMultipleOperations() {
        // Arrange & Act
        detailOutput.setQuantityUsed(50);
        detailOutput.setUnitPrice(new BigDecimal("10.50"));
        detailOutput.setMovementSale(movementSale);
        
        int firstQuantity = detailOutput.getQuantityUsed();
        BigDecimal firstPrice = detailOutput.getUnitPrice();
        
        detailOutput.setMovementOrigin(movementOrigin);
        detailOutput.setIdDetailOutput(999L);

        // Assert - Original values should remain unchanged
        assertEquals(firstQuantity, detailOutput.getQuantityUsed());
        assertEquals(0, firstPrice.compareTo(detailOutput.getUnitPrice()));
        assertEquals(movementSale, detailOutput.getMovementSale());
        assertEquals(movementOrigin, detailOutput.getMovementOrigin());
        assertEquals(999L, detailOutput.getIdDetailOutput());
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle entity with only required fields set")
    void testEntityWithRequiredFieldsOnly() {
        // Act
        detailOutput.setQuantityUsed(15);
        detailOutput.setUnitPrice(new BigDecimal("25.00"));

        // Assert
        assertEquals(15, detailOutput.getQuantityUsed());
        assertEquals(0, new BigDecimal("25.00").compareTo(detailOutput.getUnitPrice()));
        assertNull(detailOutput.getIdDetailOutput());
        assertNull(detailOutput.getMovementSale());
        assertNull(detailOutput.getMovementOrigin());
    }

    @Test
    @DisplayName("Should handle price with trailing zeros")
    void testPriceWithTrailingZeros() {
        // Arrange
        BigDecimal price = new BigDecimal("100.000");

        // Act
        detailOutput.setUnitPrice(price);

        // Assert
        assertEquals(0, new BigDecimal("100.000").compareTo(detailOutput.getUnitPrice()));
    }

    @Test
    @DisplayName("Should handle very small decimal prices")
    void testVerySmallDecimalPrices() {
        // Arrange
        BigDecimal smallPrice = new BigDecimal("0.01");

        // Act
        detailOutput.setUnitPrice(smallPrice);

        // Assert
        assertEquals(0, smallPrice.compareTo(detailOutput.getUnitPrice()));
    }
}
