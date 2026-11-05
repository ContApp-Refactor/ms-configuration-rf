package co.unicauca.edu.co.contables.inventory.peps.unit.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.DetailOutput;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;

public class DetailOutputUnitTest {
    private Product product;
    private BigDecimal validUnitPrice;
    private Kardex purchaseMovement;
    private Kardex saleMovement;

    @BeforeEach
    void setup(){
        // Arrange
        product = new Product();
        product.setProductId(1L);
        product.setName("Test Product");
        product.setState(true);
        
        validUnitPrice = new BigDecimal("10.50");


        purchaseMovement = Kardex.createPurchase(
            "1001", 
            "Purchase", 
            100, 
            validUnitPrice, 
            product
        );

        saleMovement = Kardex.createSale(
            "2001", 
            "Sale", 
            50, 
            validUnitPrice, 
            product
        );

    }


    @Test
    @DisplayName("create detail output successfully")
    void testCreate_ValidParameters() {
        // Arrange
        int amountUsed = 30;
        
        // Act
        DetailOutput detail = DetailOutput.create(
            amountUsed, 
            validUnitPrice, 
            saleMovement, 
            purchaseMovement
        );
        
        // Assert
        assertAll("Detail creation validation",
            () -> assertEquals(amountUsed, detail.getQuantityUsed()),
            () -> assertEquals(0, validUnitPrice.compareTo(detail.getUnitPrice())),
            () -> assertEquals(saleMovement, detail.getMovementSale()),
            () -> assertEquals(purchaseMovement, detail.getMovementOrigin())
        );
    }
    
    @Test
    @DisplayName("throw exception when amount used is zero")
    void testCreate_ZeroAmount() {
        // Arrange
        int invalidAmount = 0;
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DetailOutput.create(invalidAmount, validUnitPrice, saleMovement, purchaseMovement)
        );
        
        assertEquals("Amount used must be greater than zero", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when amount used is negative")
    void testCreate_NegativeAmount() {
        // Arrange
        int negativeAmount = -10;
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DetailOutput.create(negativeAmount, validUnitPrice, saleMovement, purchaseMovement)
        );
        
        assertEquals("Amount used must be greater than zero", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when unit price is null")
    void testCreate_NullUnitPrice() {
        // Arrange
        int amountUsed = 30;
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DetailOutput.create(amountUsed, null, saleMovement, purchaseMovement)
        );
        
        assertEquals("Unit price cannot be negative", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when unit price is negative")
    void testCreate_NegativeUnitPrice() {
        // Arrange
        int amountUsed = 30;
        BigDecimal negativePrice = new BigDecimal("-5.00");
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DetailOutput.create(amountUsed, negativePrice, saleMovement, purchaseMovement)
        );
        
        assertEquals("Unit price cannot be negative", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when sale movement is null")
    void testCreate_NullSaleMovement() {
        // Arrange
        int amountUsed = 30;
        
        // Act y Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> DetailOutput.create(amountUsed, validUnitPrice, null, purchaseMovement)
        );
        
        assertEquals("Sale movement cannot be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when origin movement is null")
    void testCreate_NullOriginMovement() {
        // Arrange
        int amountUsed = 30;
        
        // Act y Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> DetailOutput.create(amountUsed, validUnitPrice, saleMovement, null)
        );
        
        assertEquals("Origin movement cannot be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when FIFO rule is violated")
    void testCreate_FIFOViolation() {
        // Arrange - Crear origen con fecha posterior a la venta
        Kardex newerPurchase = Kardex.createPurchase("1002", "Purchase", 100, validUnitPrice, product);
        // Forzar una fecha anterior en la venta
        Kardex olderSale = Kardex.createSale("2002", "Sale", 50, validUnitPrice, product);
        olderSale.setDate(ZonedDateTime.now().minusDays(1));
        newerPurchase.setDate(ZonedDateTime.now());
        
        int amountUsed = 30;
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> DetailOutput.create(amountUsed, validUnitPrice, olderSale, newerPurchase)
        );
        
        assertEquals("FIFO violation: origin movement is newer than sale movement", 
            exception.getMessage());
    }

    @Test
    @DisplayName("calculate total value correctly")
    void testGetTotalValue_ValidQuantityAndPrice() {
        // Arrange
        int amountUsed = 40;
        BigDecimal unitPrice = new BigDecimal("12.50");
        DetailOutput detail = DetailOutput.create(amountUsed, unitPrice, saleMovement, purchaseMovement);
        BigDecimal expectedTotal = new BigDecimal("500.00");
        
        // Act
        BigDecimal actualTotal = detail.getTotalValue();
        
        // Assert
        assertEquals(0, expectedTotal.compareTo(actualTotal));
    }
    
    @Test
    @DisplayName("calculate total value for single unit-(return unitprice)")
    void testGetTotalValue_SingleUnit() {
        // Arrange
        int amountUsed = 1;
        BigDecimal unitPrice = new BigDecimal("25.75");
        DetailOutput detail = DetailOutput.create(amountUsed, unitPrice, saleMovement, purchaseMovement);
        
        // Act
        BigDecimal actualTotal = detail.getTotalValue();
        
        // Assert
        assertEquals(0, unitPrice.compareTo(actualTotal));
    }
    
    @Test
    @DisplayName("calculate total value with large quantity -(return total)")
    void testGetTotalValue_LargeQuantity() {
        // Arrange
        Kardex largePurchase = Kardex.createPurchase("1003", "Purchase", 5000, validUnitPrice, product);
        Kardex largeSale = Kardex.createSale("2003", "Sale", 1000, validUnitPrice, product);
        int amountUsed = 1000;
        BigDecimal unitPrice = new BigDecimal("5.00");
        DetailOutput detail = DetailOutput.create(amountUsed, unitPrice, largeSale, largePurchase);
        BigDecimal expectedTotal = new BigDecimal("5000.00");
        
        // Act
        BigDecimal actualTotal = detail.getTotalValue();
        
        // Assert
        assertEquals(0, expectedTotal.compareTo(actualTotal));
    }


    @Test
    @DisplayName("return true when belongs to sale movement")
    void testBelongsToSaleMovement_CorrectSale() {
        // Arrange
        DetailOutput detail = DetailOutput.create(30, validUnitPrice, saleMovement, purchaseMovement);
        
        // Act y Assert
        assertTrue(detail.belongsToSaleMovement(saleMovement));
    }
    
    @Test
    @DisplayName("return false when does not belong to sale movement")
    void testBelongsToSaleMovement_DifferentSale() {
        // Arrange
        saleMovement.setIdKardex(1L); 
        
        DetailOutput detail = DetailOutput.create(30, validUnitPrice, saleMovement, purchaseMovement);
        
        Kardex differentSale = Kardex.createSale("2999", "Other Sale", 20, validUnitPrice, product);
        differentSale.setIdKardex(2L); // Asignar ID diferente
        
        // Act y Assert
        assertFalse(detail.belongsToSaleMovement(differentSale));
    }

    
    @Test
    @DisplayName("return false when sale movement is null")
    void testBelongsToSaleMovement_NullSaleInDetail() {
        // Arrange
        DetailOutput detail = new DetailOutput();
        detail.setMovementSale(null);
        
        // Act y Assert
        assertFalse(detail.belongsToSaleMovement(saleMovement));
    }
    

    @Test
    @DisplayName("return true when FIFO is valid - origin before sale")
    void testIsValidFIFO_OriginBeforeSale() {
        // Arrange
        Kardex earlierPurchase = Kardex.createPurchase("1004", "Purchase", 100, validUnitPrice, product);
        earlierPurchase.setDate(ZonedDateTime.now().minusDays(1));
        Kardex laterSale = Kardex.createSale("2004", "Sale", 50, validUnitPrice, product);
        laterSale.setDate(ZonedDateTime.now());
        
        DetailOutput detail = DetailOutput.create(30, validUnitPrice, laterSale, earlierPurchase);
        
        // Act y Assert
        assertTrue(detail.isValidFIFO());
    }
    
    @Test
    @DisplayName("return true when dates are equal")
    void testIsValidFIFO_SameDate() {
        // Arrange
        ZonedDateTime sameDate = ZonedDateTime.now();
        purchaseMovement.setDate(sameDate);
        saleMovement.setDate(sameDate);
        
        DetailOutput detail = DetailOutput.create(30, validUnitPrice, saleMovement, purchaseMovement);
        
        // Act y Assert
        assertTrue(detail.isValidFIFO());
    }
    
    @Test
    @DisplayName("return false when FIFO is violated")
    void testIsValidFIFO_OriginAfterSale() {
        // Arrange
        Kardex newerPurchase = Kardex.createPurchase("1005", "Purchase", 100, validUnitPrice, product);
        newerPurchase.setDate(ZonedDateTime.now().plusDays(1));
        Kardex olderSale = Kardex.createSale("2005", "Sale", 50, validUnitPrice, product);
        olderSale.setDate(ZonedDateTime.now());
        
        DetailOutput detail = new DetailOutput();
        detail.setQuantityUsed(30);
        detail.setUnitPrice(validUnitPrice);
        detail.setMovementSale(olderSale);
        detail.setMovementOrigin(newerPurchase);
        
        // Act y Assert
        assertFalse(detail.isValidFIFO());
    }
    
    @Test
    @DisplayName("return false when origin movement is null")
    void testIsValidFIFO_NullOrigin() {
        // Arrange
        DetailOutput detail = new DetailOutput();
        detail.setMovementSale(saleMovement);
        detail.setMovementOrigin(null);
        
        // Act y Assert
        assertFalse(detail.isValidFIFO());
    }
    
    @Test
    @DisplayName("return false when sale movement is null")
    void testIsValidFIFO_NullSale() {
        // Arrange
        DetailOutput detail = new DetailOutput();
        detail.setMovementSale(null);
        detail.setMovementOrigin(purchaseMovement);
        
        // Act y Assert
        assertFalse(detail.isValidFIFO());
    }
    

    @Test
    @DisplayName("return true when both movements belong to same product")
    void testBelongsToSameProduct_SameProduct() {
        // Arrange
        DetailOutput detail = DetailOutput.create(30, validUnitPrice, saleMovement, purchaseMovement);
        
        // Act y Assert
        assertTrue(detail.belongsToSameProduct());
    }
    
    @Test
    @DisplayName("return false when movements belong to different products")
    void testBelongsToSameProduct_DifferentProducts() {
        // Arrange
        Product differentProduct = new Product();
        differentProduct.setProductId(999L);
        differentProduct.setName("Different Product");
        differentProduct.setState(true);
        
        Kardex purchaseDifferentProduct = Kardex.createPurchase(
            "1006", 
            "Purchase", 
            100, 
            validUnitPrice, 
            differentProduct
        );
        

        DetailOutput detail = new DetailOutput();
        detail.setQuantityUsed(30);
        detail.setUnitPrice(validUnitPrice);
        detail.setMovementSale(saleMovement);
        detail.setMovementOrigin(purchaseDifferentProduct);
        
        // Act y Assert
        assertFalse(detail.belongsToSameProduct());
    }
    
    @Test
    @DisplayName("return false when origin movement is null")
    void testBelongsToSameProduct_NullOrigin() {
        // Arrange
        DetailOutput detail = new DetailOutput();
        detail.setMovementSale(saleMovement);
        detail.setMovementOrigin(null);
        
        // Act y Assert
        assertFalse(detail.belongsToSameProduct());
    }
    
    @Test
    @DisplayName("return false when sale movement is null")
    void testBelongsToSameProduct_NullSale() {
        // Arrange
        DetailOutput detail = new DetailOutput();
        detail.setMovementSale(null);
        detail.setMovementOrigin(purchaseMovement);
        
        // Act y Assert
        assertFalse(detail.belongsToSameProduct());
    }
    
    @Test
    @DisplayName("return false when origin product is null")
    void testBelongsToSameProduct_NullOriginProduct() {
        // Arrange
        Kardex purchaseNoProduct = Kardex.createPurchase("1007", "Purchase", 100, validUnitPrice, product);
        purchaseNoProduct.setProduct(null);
        
        DetailOutput detail = new DetailOutput();
        detail.setMovementSale(saleMovement);
        detail.setMovementOrigin(purchaseNoProduct);
        
        // Act t Assert
        assertFalse(detail.belongsToSameProduct());
    }
    
    @Test
    @DisplayName("validate consistency successfully")
    void testValidateConsistency_ValidDetail_NoExceptionThrown() {
        // Arrange
        DetailOutput detail = DetailOutput.create(30, validUnitPrice, saleMovement, purchaseMovement);
        
        // Act y Assert
        assertDoesNotThrow(() -> detail.validateConsistency());
    }
    
    @Test
    @DisplayName("throw exception when FIFO is violated in consistency check")
    void testValidateConsistency_FIFOViolation() {
        // Arrange
        Kardex newerPurchase = Kardex.createPurchase("1008", "Purchase", 100, validUnitPrice, product);
        newerPurchase.setDate(ZonedDateTime.now().plusDays(1));
        Kardex olderSale = Kardex.createSale("2008", "Sale", 50, validUnitPrice, product);
        olderSale.setDate(ZonedDateTime.now());
        
        DetailOutput detail = new DetailOutput();
        detail.setQuantityUsed(30);
        detail.setUnitPrice(validUnitPrice);
        detail.setMovementSale(olderSale);
        detail.setMovementOrigin(newerPurchase);
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> detail.validateConsistency()
        );
        
        assertEquals("FIFO violation: origin movement is newer than sale movement", 
            exception.getMessage());
    }
    @Test
    @DisplayName("Should throw exception when products do not match")
    void testValidateConsistency_DifferentProducts_ThrowsException() {
        // Arrange
        Product differentProduct = new Product();
        differentProduct.setProductId(888L);
        differentProduct.setName("Different");
        differentProduct.setState(true);
        
        Kardex purchaseDifferentProduct = Kardex.createPurchase(
            "1009", 
            "Purchase", 
            100, 
            validUnitPrice, 
            differentProduct
        );
    
        purchaseDifferentProduct.setDate(ZonedDateTime.now().minusDays(1));
        saleMovement.setDate(ZonedDateTime.now());
        
        DetailOutput detail = new DetailOutput();
        detail.setQuantityUsed(30);
        detail.setUnitPrice(validUnitPrice);
        detail.setMovementSale(saleMovement);
        detail.setMovementOrigin(purchaseDifferentProduct);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> detail.validateConsistency()
        );
        
        assertEquals("Origin and sale movements must belong to the same product", 
            exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when origin has insufficient quantity")
    void testValidateConsistency_InsufficientQuantity_ThrowsException() {
        // Arrange
        Kardex limitedPurchase = Kardex.createPurchase("1010", "Purchase", 10, validUnitPrice, product);

        limitedPurchase.setDate(ZonedDateTime.now().minusDays(1));
        saleMovement.setDate(ZonedDateTime.now());
        limitedPurchase.reduceAvailableQuantity(10); // Available: 0
        
        DetailOutput detail = new DetailOutput();
        detail.setQuantityUsed(5);
        detail.setUnitPrice(validUnitPrice);
        detail.setMovementSale(saleMovement);
        detail.setMovementOrigin(limitedPurchase);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> detail.validateConsistency()
        );
        
        assertEquals("Origin movement does not have enough available quantity", 
            exception.getMessage());
    }

   

    @Test
    @DisplayName("be equal when same instance -(return true)")
    void testEquals_SameInstance_ReturnsTrue() {
        // Arrange
        DetailOutput detail = DetailOutput.create(30, validUnitPrice, saleMovement, purchaseMovement);
        
        // Act y Assert
        assertEquals(detail, detail);
    }
    
    @Test
    @DisplayName("be equal when same ID-(return true)")
    void testEquals_SameId() {
        // Arrange
        DetailOutput detail1 = DetailOutput.create(30, validUnitPrice, saleMovement, purchaseMovement);
        detail1.setIdDetailOutput(1L);
        
        DetailOutput detail2 = DetailOutput.create(50, validUnitPrice, saleMovement, purchaseMovement);
        detail2.setIdDetailOutput(1L);
        
        // Act y Assert
        assertEquals(detail1, detail2);
    }
    
    @Test
    @DisplayName("not be equal when different IDs-(return false)")
    void testEquals_DifferentIds() {
        // Arrange
        DetailOutput detail1 = DetailOutput.create(30, validUnitPrice, saleMovement, purchaseMovement);
        detail1.setIdDetailOutput(1L);
        
        DetailOutput detail2 = DetailOutput.create(30, validUnitPrice, saleMovement, purchaseMovement);
        detail2.setIdDetailOutput(2L);
        
        // Act y Assert
        assertNotEquals(detail1, detail2);
    }
    
    @Test
    @DisplayName("not be equal to null-(return false)")
    void testEquals_NullObject() {
        // Arrange
        DetailOutput detail = DetailOutput.create(30, validUnitPrice, saleMovement, purchaseMovement);
        
        // Act y Assert
        assertNotEquals(detail, null);
    }
    
    @Test
    @DisplayName("not be equal to different class-(return false)")
    void testEquals_DifferentClass() {
        // Arrange
        DetailOutput detail = DetailOutput.create(30, validUnitPrice, saleMovement, purchaseMovement);
        String differentObject = "Not a DetailOutput";
        
        // Act y Assert
        assertNotEquals(detail, differentObject);
    }
    
    @Test
    @DisplayName("return same hashCode for equal objects")
    void testHashCode_EqualObjects_ReturnsSameHash() {
        // Arrange
        DetailOutput detail1 = DetailOutput.create(30, validUnitPrice, saleMovement, purchaseMovement);
        detail1.setIdDetailOutput(1L);
        
        DetailOutput detail2 = DetailOutput.create(50, validUnitPrice, saleMovement, purchaseMovement);
        detail2.setIdDetailOutput(1L);
        
        // Act y Assert
        assertEquals(detail1.hashCode(), detail2.hashCode());
    }
    
    @Test
    @DisplayName("return consistent hashCode")
    void testHashCode_MultipleInvocations_ReturnsConsistent() {
        // Arrange
        DetailOutput detail = DetailOutput.create(30, validUnitPrice, saleMovement, purchaseMovement);
        detail.setIdDetailOutput(1L);
        
        // Act
        int hash1 = detail.hashCode();
        int hash2 = detail.hashCode();
        int hash3 = detail.hashCode();
        
        // Assert
        assertAll("HashCode consistency",
            () -> assertEquals(hash1, hash2),
            () -> assertEquals(hash2, hash3)
        );
    }
    

    @Test
    @DisplayName("return formatted string with all fields")
    void testToString_ValidDetail_ReturnsFormattedString() {
        // Arrange
        DetailOutput detail = DetailOutput.create(30, validUnitPrice, saleMovement, purchaseMovement);
        detail.setIdDetailOutput(5L);
        
        // Act
        String result = detail.toString();
        
        // Assert
        assertAll("ToString validation",
            () -> assertTrue(result.contains("id=5")),
            () -> assertTrue(result.contains("amountUsed=30")),
            () -> assertTrue(result.contains("unitPrice=10.5")),
            () -> assertTrue(result.startsWith("DetailOutput{")),
            () -> assertTrue(result.endsWith("}"))
        );
    }
    
    @Test
    @DisplayName("return string with null ID")
    void testToString_NullId_ReturnsStringWithNull() {
        // Arrange
        DetailOutput detail = DetailOutput.create(30, validUnitPrice, saleMovement, purchaseMovement);
        
        // Act
        String result = detail.toString();
        
        // Assert
        assertTrue(result.contains("id=null"));
    }
    

    @Test
    @DisplayName("Should create detail using builder")
    void testBuilder_ValidValues() {
        // Arrange y Act
        DetailOutput detail = DetailOutput.builder()
            .quantityUsed(25)
            .unitPrice(new BigDecimal("15.00"))
            .movementSale(saleMovement)
            .movementOrigin(purchaseMovement)
            .build();
        
        // Assert
        assertAll("Builder validation",
            () -> assertEquals(25, detail.getQuantityUsed()),
            () -> assertEquals(0, new BigDecimal("15.00").compareTo(detail.getUnitPrice())),
            () -> assertEquals(saleMovement, detail.getMovementSale()),
            () -> assertEquals(purchaseMovement, detail.getMovementOrigin())
        );
    }
    

    
    
}
