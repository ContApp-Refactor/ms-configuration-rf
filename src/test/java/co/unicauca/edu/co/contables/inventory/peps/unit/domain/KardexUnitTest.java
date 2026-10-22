package co.unicauca.edu.co.contables.inventory.peps.unit.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.inventory.peps.domain.enums.MovementType;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.DetailOutput;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;

public class KardexUnitTest {
    private Product product;
    private BigDecimal validPrice;

    @BeforeEach
    void setUp(){
       product=new Product();
       product.setProductId(1L);
        product.setName("Apple");
        product.setReference("REF-001");
        product.setEnterpriseId("f1ec3d7b-613e-4f06-ac6e-aabecf3cc31b");
        product.setPresentation("Fruit-Box");
        product.setState(true); 
        product.setRecordsKardex(new ArrayList<>());

        validPrice=new BigDecimal("10.50");
    }

    @Test
    @DisplayName("create")
    void testCreatePurchase(){
        //Arrange
        String factCode = "1001";
        String details="Purchase details";
        int quantity=100;

        //Act
        Kardex kardex=Kardex.createPurchase(factCode, details, quantity, validPrice, product);

        //Assert

        assertAll("Purchase creation validation",
            ()->assertEquals(factCode, kardex.getFactCode()),
            ()->assertEquals(details, kardex.getDetails()),
            ()->assertEquals(quantity, kardex.getQuantity()),
            ()->assertEquals(validPrice, kardex.getUnitPrice()),
            ()->assertEquals(MovementType.PURCHASE, kardex.getType()),
            ()->assertEquals(quantity,kardex.getAvailableQuantity()),
            ()->assertEquals(product, kardex.getProduct()),
            ()->assertNotNull(kardex.getDate()),
            ()->assertTrue(kardex.getDetailsOutput().isEmpty()),
            ()->assertTrue(kardex.getDetailsOrigin().isEmpty())

        );
    }

    @Test
    @DisplayName("Create sale movement ")
    void testCreateSale(){
        // Arrange
        String factCode = "2001";
        String details = "Sale details";
        int quantity = 50;

        // Act
        Kardex kardex = Kardex.createSale(factCode, details, quantity, validPrice, product);
        
        // Assert
        assertAll("Sale creation validation",
            () -> assertEquals(factCode, kardex.getFactCode()),
            () -> assertEquals(details, kardex.getDetails()), 
            () -> assertEquals(quantity, kardex.getQuantity()),
            () -> assertEquals(MovementType.SALE, kardex.getType()),
            () -> assertEquals(0, kardex.getAvailableQuantity()),
            () -> assertEquals(product, kardex.getProduct()),
            () -> assertNotNull(kardex.getDate()),
            () -> assertTrue(kardex.getDetailsOutput().isEmpty()),
            () -> assertTrue(kardex.getDetailsOrigin().isEmpty())
            
        );
    }

    @Test
    @DisplayName("create sale return")
    void testCreateSaleReturn(){
        // Arrange
        String factCode = "2001";
        String details = "Sale return details";
        int quantity = 50;

        // Act
        Kardex kardex=Kardex.createSaleReturn(factCode, details, quantity, validPrice, product);

        // Assert
          assertAll("Sale return creation validation",
            () -> assertEquals(factCode, kardex.getFactCode()),
            () -> assertEquals(details, kardex.getDetails()), 
            () -> assertEquals(quantity, kardex.getQuantity()),
            () -> assertEquals(MovementType.SALESRETURN, kardex.getType()),
            () -> assertEquals(0, kardex.getAvailableQuantity()),
            () -> assertEquals(product, kardex.getProduct()),
            () -> assertNotNull(kardex.getDate()),
            () -> assertTrue(kardex.getDetailsOutput().isEmpty()),
            () -> assertTrue(kardex.getDetailsOrigin().isEmpty())
            
        );


    }
    @Test
    @DisplayName("create purchase return")
    void testCreatePurchaseReturn(){

        // Arrange
        String factCode = "2001";
        String details = "Sale return details";
        int quantity = 50;

        // Act
        Kardex kardex=Kardex.createPurchaseReturn(factCode, details, quantity, validPrice, product);

         // Assert
          assertAll("Purchase return creation validation",
            () -> assertEquals(factCode, kardex.getFactCode()),
            () -> assertEquals(details, kardex.getDetails()), 
            () -> assertEquals(quantity, kardex.getQuantity()),
            () -> assertEquals(MovementType.PURCHASERETURN, kardex.getType()),
            () -> assertEquals(0, kardex.getAvailableQuantity()),
            () -> assertEquals(product, kardex.getProduct()),
            () -> assertNotNull(kardex.getDate()),
            () -> assertTrue(kardex.getDetailsOutput().isEmpty()),
            () -> assertTrue(kardex.getDetailsOrigin().isEmpty())
            
        );

    }

    @Test
    @DisplayName("create non-commercial entry")
    void testCreateNonCommercialEntry(){

         // Arrange
        String factCode = "2001";
        String details = "Sale return details";
        int quantity = 50;

        // Act
        Kardex kardex=Kardex.createNonCommercialEntry(factCode, details, quantity, validPrice, product);

        // Assert
        assertAll("Create non-commercial entry creation validation",
            ()->assertEquals(factCode, kardex.getFactCode()),
            ()->assertEquals(details, kardex.getDetails()),
            ()->assertEquals(quantity, kardex.getQuantity()),
            ()->assertEquals(validPrice, kardex.getUnitPrice()),
            ()->assertEquals(MovementType.NONCOMMERCIALENTRY, kardex.getType()),
            ()->assertEquals(quantity,kardex.getAvailableQuantity()),
            ()->assertEquals(product, kardex.getProduct()),
            ()->assertNotNull(kardex.getDate()),
            ()->assertTrue(kardex.getDetailsOutput().isEmpty()),
            ()->assertTrue(kardex.getDetailsOrigin().isEmpty())

        );

    }

    @Test
    @DisplayName("create non-commercial exit")
    void testCreateNonCommercialExit(){

        // Arrange
        String factCode = "2001";
        String details = "Sale details";
        int quantity = 50;

        // Act
        Kardex kardex = Kardex.createNonCommercialExit(factCode, details, quantity, validPrice, product);
        
        // Assert
        assertAll("Sale creation validation",
            () -> assertEquals(factCode, kardex.getFactCode()),
            () -> assertEquals(details, kardex.getDetails()), 
            () -> assertEquals(quantity, kardex.getQuantity()),
            () -> assertEquals(MovementType.NONCOMMERCIALEXIT, kardex.getType()),
            () -> assertEquals(0, kardex.getAvailableQuantity()),
            () -> assertEquals(product, kardex.getProduct()),
            () -> assertNotNull(kardex.getDate()),
            () -> assertTrue(kardex.getDetailsOutput().isEmpty()),
            () -> assertTrue(kardex.getDetailsOrigin().isEmpty())
            
        );
        
    }

    @Test
    @DisplayName("reduce available quantity successfully")
    void testReduceAvailableQuantity_ReduceCorrectly(){

        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "facture:2978", 100, validPrice, product);
        int amountToReduce = 30;

        //Act
        kardex.reduceAvailableQuantity(amountToReduce);

        // Assert
        assertEquals(70, kardex.getAvailableQuantity());
    }
    @Test
    @DisplayName("throw exception when reducing more than available")
    void testReduceAvailableQuantity_ExceedsAvailable(){
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "facture:2978", 100, validPrice, product);
        int excessiveAmount = 150;

        // Act & Assert
         IllegalArgumentException exception = assertThrows(
             IllegalArgumentException.class,
             () -> kardex.reduceAvailableQuantity(excessiveAmount)
        );
         assertTrue(exception.getMessage().contains("Insufficient available quantity"));


    }

    @Test
    @DisplayName("throw exception when quantity must be positive")
    void testReduceAvailableQuantity_PositiveAvailable(){
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "facture:2978", 100, validPrice, product);
        int NegativeQuantity = -10;

        // Act & Assert
         IllegalArgumentException exception = assertThrows(
             IllegalArgumentException.class,
             () -> kardex.reduceAvailableQuantity(NegativeQuantity )
        );
         assertTrue(exception.getMessage().contains("Amount to reduce must be positive"));


    }


    @Test
    @DisplayName("Should restore available quantity successfully")
    void testRestoreAvailableQuantity_ValidAmount_RestoresCorrectly() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        kardex.reduceAvailableQuantity(30);
        int amountToRestore = 20;

        // Act
        kardex.restoreAvailableQuantity(amountToRestore);

        // Assert
        assertEquals(90, kardex.getAvailableQuantity());
    }

    @Test
    @DisplayName("Should restore to exactly original quantity")
    void testRestoreAvailableQuantity_RestoreToOriginalQuantity_Success() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        kardex.reduceAvailableQuantity(100);
        int amountToRestore = 100;

        // Act
        kardex.restoreAvailableQuantity(amountToRestore);

        // Assert
        assertEquals(100, kardex.getAvailableQuantity());
    }

    @Test
    @DisplayName("Should restore minimum amount successfully")
    void testRestoreAvailableQuantity_RestoreOneUnit_Success() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        kardex.reduceAvailableQuantity(50); 
        int amountToRestore = 1;

        // Act
        kardex.restoreAvailableQuantity(amountToRestore);

        // Assert
        assertEquals(51, kardex.getAvailableQuantity());
    }

    @Test
    @DisplayName("Should throw exception when restoring zero amount")
    void testRestoreAvailableQuantity_ZeroAmount() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        kardex.reduceAvailableQuantity(30);
        int invalidAmount = 0;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardex.restoreAvailableQuantity(invalidAmount)
        );

        assertEquals("Amount to restore must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when restoring negative amount")
    void testRestoreAvailableQuantity_NegativeAmount() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        kardex.reduceAvailableQuantity(30);
        int negativeAmount = -10;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardex.restoreAvailableQuantity(negativeAmount)
        );

        assertEquals("Amount to restore must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when restoring more than original quantity")
    void testRestoreAvailableQuantity_ExceedsOriginalQuantity() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        kardex.reduceAvailableQuantity(20); 
        int excessiveAmount = 30; 

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardex.restoreAvailableQuantity(excessiveAmount)
        );

        assertEquals("Cannot restore more than original quantity", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when restoring from full stock")
    void testRestoreAvailableQuantity_FromFullStock() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        // Available is already 100 (full)
        int amountToRestore = 1;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardex.restoreAvailableQuantity(amountToRestore)
        );

        assertEquals("Cannot restore more than original quantity", exception.getMessage());
    }

    @Test
    @DisplayName("Should not modify original quantity when restoring")
    void testRestoreAvailableQuantity_OriginalQuantityUnchanged_Success() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        int originalQuantity = kardex.getQuantity();
        kardex.reduceAvailableQuantity(40);
        int amountToRestore = 20;

        // Act
        kardex.restoreAvailableQuantity(amountToRestore);

        // Assert
        assertAll("Restore validation",
            () -> assertEquals(originalQuantity, kardex.getQuantity(), "Original quantity should not change"),
            () -> assertEquals(80, kardex.getAvailableQuantity(), "Available quantity should be restored")
        );
    }

    @Test
    @DisplayName("Should return true when amount is valid and sufficient stock available")
    void testCanReduceQuantity_ValidAmountWithSufficientStock_ReturnsTrue() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        int amountToReduce = 50;

        // Act
        boolean result = kardex.canReduceQuantity(amountToReduce);

        // Assert
        assertTrue(result, "Should be able to reduce quantity when sufficient stock available");
    }

    @Test
    @DisplayName("Should return true when reducing exactly available quantity")
    void testCanReduceQuantity_ExactAvailableQuantity_ReturnsTrue() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        int exactAmount = 100;

        // Act
        boolean result = kardex.canReduceQuantity(exactAmount);

        // Assert
        assertTrue(result, "Should be able to reduce exact available quantity");
    }

    @Test
    @DisplayName("Should return true when reducing minimum amount")
    void testCanReduceQuantity_ReduceOneUnit_ReturnsTrue() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        int minAmount = 1;

        // Act
        boolean result = kardex.canReduceQuantity(minAmount);

        // Assert
        assertTrue(result, "Should be able to reduce minimum amount (1 unit)");
    }

    @Test
    @DisplayName("Should return false when amount exceeds available quantity")
    void testCanReduceQuantity_AmountExceedsAvailable_ReturnsFalse() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        int excessiveAmount = 150;

        // Act
        boolean result = kardex.canReduceQuantity(excessiveAmount);

        // Assert
        assertFalse(result, "Should not be able to reduce more than available quantity");
    }

    @Test
    @DisplayName("Should return false when amount is zero")
    void testCanReduceQuantity_ZeroAmount_ReturnsFalse() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        int zeroAmount = 0;

        // Act
        boolean result = kardex.canReduceQuantity(zeroAmount);

        // Assert
        assertFalse(result, "Should not be able to reduce zero amount");
    }

    @Test
    @DisplayName("Should return false when amount is negative")
    void testCanReduceQuantity_NegativeAmount_ReturnsFalse() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        int negativeAmount = -10;

        // Act
        boolean result = kardex.canReduceQuantity(negativeAmount);

        // Assert
        assertFalse(result, "Should not be able to reduce negative amount");
    }

    @Test
    @DisplayName("Should return false when no stock available")
    void testCanReduceQuantity_NoStockAvailable_ReturnsFalse() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        kardex.reduceAvailableQuantity(100);
        int amountToReduce = 1;

        // Act
        boolean result = kardex.canReduceQuantity(amountToReduce);

        // Assert
        assertFalse(result, "Should not be able to reduce when no stock available");
    }

    @Test
    @DisplayName("Should return true when reducing from partially reduced stock")
    void testCanReduceQuantity_PartiallyReducedStock_ReturnsTrue() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        kardex.reduceAvailableQuantity(30); 
        int amountToReduce = 50;

        // Act
        boolean result = kardex.canReduceQuantity(amountToReduce);

        // Assert
        assertTrue(result, "Should be able to reduce from partially reduced stock");
    }

    @Test
    @DisplayName("Should return false when amount exceeds partially reduced stock")
    void testCanReduceQuantity_ExceedsPartiallyReducedStock_ReturnsFalse() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        kardex.reduceAvailableQuantity(60); 
        int amountToReduce = 50;

        // Act
        boolean result = kardex.canReduceQuantity(amountToReduce);

        // Assert
        assertFalse(result, "Should not be able to reduce more than partially available stock");
    }
    @Test
    @DisplayName("Should return true when stock is available")
    void testHasAvailableStock_WithStock_ReturnsTrue() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);

        // Act & Assert
        assertTrue(kardex.hasAvailableStock());
    }

    @Test
    @DisplayName("Should return false when no stock available")
    void testHasAvailableStock_NoStock_ReturnsFalse() {
        // Arrange
        Kardex kardex = Kardex.createSale("2001", "Sale", 50, validPrice, product);

        // Act & Assert
        assertFalse(kardex.hasAvailableStock());
    }
    @DisplayName("Should return true for purchase movement")
    void testIsPurchase_PurchaseType_ReturnsTrue() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);

        // Act & Assert
        assertTrue(kardex.isPurchase());
    }

    @Test
    @DisplayName("Should return false for non-purchase movements")
    void testIsPurchase_NonPurchaseTypes_ReturnsFalse() {
        // Arrange
        Kardex sale = Kardex.createSale("2001", "Sale", 50, validPrice, product);
        Kardex purchaseReturn = Kardex.createPurchaseReturn("3001", "Return", 10, validPrice, product);

        // Act & Assert
        assertAll("Non-purchase validations",
            () -> assertFalse(sale.isPurchase()),
            () -> assertFalse(purchaseReturn.isPurchase())
        );
    }
    @Test
    @DisplayName("Should return true for sale movement")
    void testIsSale_SaleType_ReturnsTrue() {
        // Arrange
        Kardex kardex = Kardex.createSale("2001", "Sale", 50, validPrice, product);

        // Act & Assert
        assertTrue(kardex.isSale());
    }

    @Test
    @DisplayName("Should return false for non-sale movements")
    void testIsSale_NonSaleTypes_ReturnsFalse() {
        // Arrange
        Kardex purchase = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);

        // Act & Assert
        assertFalse(purchase.isSale());
    }
    @Test
    @DisplayName("Should return true for purchase return movement")
    void testIsPurchaseReturn_PurchaseReturnType_ReturnsTrue() {
        // Arrange
        Kardex kardex = Kardex.createPurchaseReturn("3001", "Return", 10, validPrice, product);

        // Act & Assert
        assertTrue(kardex.isPurchaseReturn());
    }

    @Test
    @DisplayName("Should return false for non-purchase return movements")
    void testIsPurchaseReturn_NonPurchaseReturnTypes_ReturnsFalse() {
        // Arrange
        Kardex purchase = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);

        // Act & Assert
        assertFalse(purchase.isPurchaseReturn());
    }
    @Test
    @DisplayName("Should return true for sale return movement")
    void testIsSaleReturn_SaleReturnType_ReturnsTrue() {
        // Arrange
        Kardex kardex = Kardex.createSaleReturn("4001", "Return", 10, validPrice, product);

        // Act & Assert
        assertTrue(kardex.isSaleReturn());
    }

    @Test
    @DisplayName("Should return false for non-sale return movements")
    void testIsSaleReturn_NonSaleReturnTypes_ReturnsFalse() {
        // Arrange
        Kardex sale = Kardex.createSale("2001", "Sale", 50, validPrice, product);

        // Act & Assert
        assertFalse(sale.isSaleReturn());
    }

    @Test
    @DisplayName("Should return true when product ID matches")
    void testBelongsToProduct_MatchingProductId_ReturnsTrue() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        Long productId = 1L;

        // Act & Assert
        assertTrue(kardex.belongsToProduct(productId));
    }

    @Test
    @DisplayName("Should return false when product ID does not match")
    void testBelongsToProduct_DifferentProductId_ReturnsFalse() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        Long differentProductId = 999L;

        // Act & Assert
        assertFalse(kardex.belongsToProduct(differentProductId));
    }

    @Test
    @DisplayName("Should return false when product is null")
    void testBelongsToProduct_NullProduct_ReturnsFalse() {
        // Arrange
        Kardex kardex = new Kardex();
        kardex.setProduct(null);
        Long productId = 1L;

        // Act & Assert
        assertFalse(kardex.belongsToProduct(productId));
    }
    @Test
    @DisplayName("Should calculate total value correctly")
    void testGetTotalValue_ValidQuantityAndPrice_ReturnsCorrectTotal() {
        // Arrange
        BigDecimal unitPrice = new BigDecimal("15.50");
        int quantity = 10;
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", quantity, unitPrice, product);
        BigDecimal expectedTotal = new BigDecimal("155.00");

        // Act
        BigDecimal actualTotal = kardex.getTotalValue();

        // Assert
        assertEquals(0, expectedTotal.compareTo(actualTotal), 
            "Total value should be 155.00");
    }

    @Test
    @DisplayName("Should calculate total value for single unit")
    void testGetTotalValue_SingleUnit_ReturnsUnitPrice() {
        // Arrange
        BigDecimal unitPrice = new BigDecimal("25.75");
        int quantity = 1;
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", quantity, unitPrice, product);

        // Act
        BigDecimal actualTotal = kardex.getTotalValue();

        // Assert
        assertEquals(0, unitPrice.compareTo(actualTotal), 
            "Total value for one unit should equal unit price");
    }

    @Test
    @DisplayName("Should calculate total value with large quantity")
    void testGetTotalValue_LargeQuantity_ReturnsCorrectTotal() {
        // Arrange
        BigDecimal unitPrice = new BigDecimal("5.00");
        int quantity = 1000;
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", quantity, unitPrice, product);
        BigDecimal expectedTotal = new BigDecimal("5000.00");

        // Act
        BigDecimal actualTotal = kardex.getTotalValue();

        // Assert
        assertEquals(0, expectedTotal.compareTo(actualTotal), 
            "Total value should be 5000.00");
    }
    @Test
    @DisplayName("Should calculate available value for full stock")
    void testGetAvailableValue_FullStock_ReturnsCorrectValue() {
        // Arrange
        BigDecimal unitPrice = new BigDecimal("10.00");
        int quantity = 100;
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", quantity, unitPrice, product);
        BigDecimal expectedValue = new BigDecimal("1000.00");

        // Act
        BigDecimal actualValue = kardex.getAvailableValue();

        // Assert
        assertEquals(0, expectedValue.compareTo(actualValue), 
            "Available value should be 1000.00");
    }

    @Test
    @DisplayName("Should calculate available value after partial reduction")
    void testGetAvailableValue_PartiallyReduced_ReturnsCorrectValue() {
        // Arrange
        BigDecimal unitPrice = new BigDecimal("20.00");
        int quantity = 100;
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", quantity, unitPrice, product);
        kardex.reduceAvailableQuantity(40);
        BigDecimal expectedValue = new BigDecimal("1200.00");

        // Act
        BigDecimal actualValue = kardex.getAvailableValue();

        // Assert
        assertEquals(0, expectedValue.compareTo(actualValue), 
            "Available value should be 1200.00 for 60 units");
    }

    @Test
    @DisplayName("Should return zero value when no stock available")
    void testGetAvailableValue_NoStock_ReturnsZero() {
        // Arrange
        BigDecimal unitPrice = new BigDecimal("15.00");
        int quantity = 100;
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", quantity, unitPrice, product);
        kardex.reduceAvailableQuantity(100);

        // Act
        BigDecimal actualValue = kardex.getAvailableValue();

        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(actualValue), 
            "Available value should be zero when no stock");
    }

    @Test
    @DisplayName("Should return zero value for sale movements")
    void testGetAvailableValue_SaleMovement_ReturnsZero() {
        // Arrange
        BigDecimal unitPrice = new BigDecimal("30.00");
        Kardex kardex = Kardex.createSale("2001", "Sale", 50, unitPrice, product);

        // Act
        BigDecimal actualValue = kardex.getAvailableValue();

        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(actualValue), 
            "Sale movements should have zero available value");
    }
  
        @Test
    @DisplayName("Should add detail output successfully")
    void testAddOutputDetail_ValidDetail_AddsToList() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        DetailOutput detail = new DetailOutput();
        detail.setQuantityUsed(10);;

        // Act
        kardex.addOutputDetail(detail);

        // Assert
        assertEquals(1, kardex.getDetailsOutput().size());
        assertTrue(kardex.getDetailsOutput().contains(detail));
    }

    @Test
    @DisplayName("Should throw exception when detail is null")
    void testAddOutputDetail_NullDetail_ThrowsException() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);

        // Act & Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> kardex.addOutputDetail(null)
        );
        
        assertEquals("Detail cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should add multiple details successfully")
    void testAddOutputDetail_MultipleDetails_AllAdded() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        DetailOutput detail1 = new DetailOutput();
        detail1.setQuantityUsed(10);
        DetailOutput detail2 = new DetailOutput();
        detail2.setQuantityUsed(20);

        // Act
        kardex.addOutputDetail(detail1);
        kardex.addOutputDetail(detail2);

        // Assert
        assertEquals(2, kardex.getDetailsOutput().size());
    }

    @Test
    @DisplayName("Should validate purchase return successfully")
    void testValidateForPurchaseReturn_ValidScenario_NoException() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        String factCode = "1001";
        int quantity = 50;

        // Act & Assert
        assertDoesNotThrow(() -> kardex.validateForPurchaseReturn(factCode, quantity));
    }

    @Test
    @DisplayName("Should throw exception when movement is not a purchase")
    void testValidateForPurchaseReturn_NotPurchaseType_ThrowsException() {
        // Arrange
        Kardex kardex = Kardex.createSale("2001", "Sale", 50, validPrice, product);
        String factCode = "2001";
        int quantity = 10;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardex.validateForPurchaseReturn(factCode, quantity)
        );
        
        assertEquals("Return must be for a purchase movement", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when invoice codes do not match")
    void testValidateForPurchaseReturn_FactCodeMismatch_ThrowsException() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        String differentFactCode = "9999";
        int quantity = 50;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardex.validateForPurchaseReturn(differentFactCode, quantity)
        );
        
        assertEquals("Invoice codes do not match", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when return quantity is zero")
    void testValidateForPurchaseReturn_ZeroQuantity_ThrowsException() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        String factCode = "1001";
        int invalidQuantity = 0;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardex.validateForPurchaseReturn(factCode, invalidQuantity)
        );
        
        assertEquals("Return quantity must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when return exceeds purchased quantity")
    void testValidateForPurchaseReturn_ExceedsPurchased_ThrowsException() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        String factCode = "1001";
        int excessiveQuantity = 150;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardex.validateForPurchaseReturn(factCode, excessiveQuantity)
        );
        
        assertTrue(exception.getMessage().contains("Cannot return more than purchased"));
    }

    @Test
    @DisplayName("Should throw exception when return exceeds available quantity")
    void testValidateForPurchaseReturn_ExceedsAvailable_ThrowsException() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        kardex.reduceAvailableQuantity(50); // Available: 50
        String factCode = "1001";
        int returnQuantity = 60;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardex.validateForPurchaseReturn(factCode, returnQuantity)
        );
        
        assertTrue(exception.getMessage().contains("Cannot return products already sold"));
    }

    @Test
    @DisplayName("Should validate sale return successfully")
    void testValidateForSaleReturn_ValidScenario_NoException() {
        // Arrange
        Kardex kardex = Kardex.createSale("2001", "Sale", 50, validPrice, product);
        String factCode = "2001";
        int quantity = 30;

        // Act & Assert
        assertDoesNotThrow(() -> kardex.validateForSaleReturn(factCode, quantity));
    }

    @Test
    @DisplayName("Should throw exception when movement is not a sale")
    void testValidateForSaleReturn_NotSaleType_ThrowsException() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        String factCode = "1001";
        int quantity = 10;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardex.validateForSaleReturn(factCode, quantity)
        );
        
        assertEquals("Return must be for a sale movement", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when sale invoice codes do not match")
    void testValidateForSaleReturn_FactCodeMismatch_ThrowsException() {
        // Arrange
        Kardex kardex = Kardex.createSale("2001", "Sale", 50, validPrice, product);
        String differentFactCode = "9999";
        int quantity = 30;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardex.validateForSaleReturn(differentFactCode, quantity)
        );
        
        assertEquals("Invoice codes do not match", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when sale return quantity is negative")
    void testValidateForSaleReturn_NegativeQuantity_ThrowsException() {
        // Arrange
        Kardex kardex = Kardex.createSale("2001", "Sale", 50, validPrice, product);
        String factCode = "2001";
        int negativeQuantity = -10;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardex.validateForSaleReturn(factCode, negativeQuantity)
        );
        
        assertEquals("Return quantity must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when return exceeds sold quantity")
    void testValidateForSaleReturn_ExceedsSold_ThrowsException() {
        // Arrange
        Kardex kardex = Kardex.createSale("2001", "Sale", 50, validPrice, product);
        String factCode = "2001";
        int excessiveQuantity = 80;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> kardex.validateForSaleReturn(factCode, excessiveQuantity)
        );
        
        assertTrue(exception.getMessage().contains("Cannot return more than sold"));
    }

    @Test
    @DisplayName("Should return empty list when details output is null")
    void testGetDetailsOutputReadOnly_NullList_ReturnsEmpty() {
        // Arrange
        Kardex kardex = new Kardex();
        kardex.setDetailsOutput(null);

        // Act
        List<DetailOutput> result = kardex.getDetailsOutputReadOnly();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return unmodifiable list")
    void testGetDetailsOutputReadOnly_ReturnsUnmodifiableList() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        DetailOutput detail = new DetailOutput();
        kardex.addOutputDetail(detail);

        // Act
        List<DetailOutput> result = kardex.getDetailsOutputReadOnly();

        // Assert
        assertThrows(UnsupportedOperationException.class, () -> result.add(new DetailOutput()));
    }

    @Test
    @DisplayName("Should return list with correct elements")
    void testGetDetailsOutputReadOnly_WithElements_ReturnsCorrectList() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        DetailOutput detail1 = new DetailOutput();
        DetailOutput detail2 = new DetailOutput();
        kardex.addOutputDetail(detail1);
        kardex.addOutputDetail(detail2);

        // Act
        List<DetailOutput> result = kardex.getDetailsOutputReadOnly();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(detail1));
        assertTrue(result.contains(detail2));
    }

    @Test
    @DisplayName("Should return empty list when details origin is null")
    void testGetDetailsOriginReadOnly_NullList_ReturnsEmpty() {
        // Arrange
        Kardex kardex = new Kardex();
        kardex.setDetailsOrigin(null);

        // Act
        List<DetailOutput> result = kardex.getDetailsOriginReadOnly();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return unmodifiable list for details origin")
    void testGetDetailsOriginReadOnly_ReturnsUnmodifiableList() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);

        // Act
        List<DetailOutput> result = kardex.getDetailsOriginReadOnly();

        // Assert
        assertThrows(UnsupportedOperationException.class, () -> result.add(new DetailOutput()));
    }

    @Test
    @DisplayName("Should return true for non-commercial entry movement")
    void testIsNonCommercialEntry_NonCommercialEntryType_ReturnsTrue() {
        // Arrange
        Kardex kardex = Kardex.createNonCommercialEntry("3001", "Entry", 50, validPrice, product);

        // Act & Assert
        assertTrue(kardex.isNonCommercialEntry());
    }

    @Test
    @DisplayName("Should return false for non-entry movements")
    void testIsNonCommercialEntry_OtherTypes_ReturnsFalse() {
        // Arrange
        Kardex purchase = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        Kardex sale = Kardex.createSale("2001", "Sale", 50, validPrice, product);
        Kardex exit = Kardex.createNonCommercialExit("3002", "Exit", 20, validPrice, product);

        // Act & Assert
        assertAll("Non-commercial entry validations",
            () -> assertFalse(purchase.isNonCommercialEntry()),
            () -> assertFalse(sale.isNonCommercialEntry()),
            () -> assertFalse(exit.isNonCommercialEntry())
        );
    }

    @Test
    @DisplayName("Should return true for non-commercial exit movement")
    void testIsNonCommercialExit_NonCommercialExitType_ReturnsTrue() {
        // Arrange
        Kardex kardex = Kardex.createNonCommercialExit("3002", "Exit", 20, validPrice, product);

        // Act & Assert
        assertTrue(kardex.isNonCommercialExit());
    }

    @Test
    @DisplayName("Should return false for non-exit movements")
    void testIsNonCommercialExit_OtherTypes_ReturnsFalse() {
        // Arrange
        Kardex purchase = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        Kardex entry = Kardex.createNonCommercialEntry("3001", "Entry", 50, validPrice, product);

        // Act & Assert
        assertAll("Non-commercial exit validations",
            () -> assertFalse(purchase.isNonCommercialExit()),
            () -> assertFalse(entry.isNonCommercialExit())
        );
    }

    @Test
    @DisplayName("Should be equal when same instance")
    void testEquals_SameInstance_ReturnsTrue() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);

        // Act & Assert
        assertEquals(kardex, kardex);
    }

    @Test
    @DisplayName("Should be equal when same ID")
    void testEquals_SameId_ReturnsTrue() {
        // Arrange
        Kardex kardex1 = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        kardex1.setIdKardex(1L);
        
        Kardex kardex2 = Kardex.createSale("2001", "Sale", 50, validPrice, product);
        kardex2.setIdKardex(1L);

        // Act & Assert
        assertEquals(kardex1, kardex2);
    }

    @Test
    @DisplayName("Should not be equal when different IDs")
    void testEquals_DifferentIds_ReturnsFalse() {
        // Arrange
        Kardex kardex1 = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        kardex1.setIdKardex(1L);
        
        Kardex kardex2 = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        kardex2.setIdKardex(2L);

        // Act & Assert
        assertNotEquals(kardex1, kardex2);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void testEquals_NullObject_ReturnsFalse() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        kardex.setIdKardex(1L);

        // Act & Assert
        assertNotEquals(kardex, null);
    }

    @Test
    @DisplayName("Should not be equal to different class")
    void testEquals_DifferentClass_ReturnsFalse() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        kardex.setIdKardex(1L);
        String differentObject = "Not a Kardex";

        // Act & Assert
        assertNotEquals(kardex, differentObject);
    }

    @Test
    @DisplayName("Should be equal when both IDs are null")
    void testEquals_BothIdsNull_ReturnsTrue() {
        // Arrange
        Kardex kardex1 = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        Kardex kardex2 = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        // idKardex is null by default

        // Act & Assert
        assertEquals(kardex1, kardex2);
    }

    @Test
    @DisplayName("Should return same hashCode for equal objects")
    void testHashCode_EqualObjects_ReturnsSameHash() {
        // Arrange
        Kardex kardex1 = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        kardex1.setIdKardex(1L);
        
        Kardex kardex2 = Kardex.createSale("2001", "Sale", 50, validPrice, product);
        kardex2.setIdKardex(1L);

        // Act
        int hash1 = kardex1.hashCode();
        int hash2 = kardex2.hashCode();

        // Assert
        assertEquals(hash1, hash2, "Equal objects must have equal hashCodes");
    }

    @Test
    @DisplayName("Should return different hashCode for different IDs")
    void testHashCode_DifferentIds_ReturnsDifferentHash() {
        // Arrange
        Kardex kardex1 = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        kardex1.setIdKardex(1L);
        
        Kardex kardex2 = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        kardex2.setIdKardex(2L);

        // Act
        int hash1 = kardex1.hashCode();
        int hash2 = kardex2.hashCode();

        // Assert
        assertNotEquals(hash1, hash2, "Different objects should have different hashCodes");
    }

    @Test
    @DisplayName("Should return consistent hashCode")
    void testHashCode_MultipleInvocations_ReturnsConsistentValue() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        kardex.setIdKardex(1L);

        // Act
        int hash1 = kardex.hashCode();
        int hash2 = kardex.hashCode();
        int hash3 = kardex.hashCode();

        // Assert
        assertAll("HashCode consistency",
            () -> assertEquals(hash1, hash2),
            () -> assertEquals(hash2, hash3),
            () -> assertEquals(hash1, hash3)
        );
    }

    @Test
    @DisplayName("Should handle hashCode with null ID")
    void testHashCode_NullId_ReturnsConsistentValue() {
        // Arrange
        Kardex kardex1 = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        Kardex kardex2 = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        // Both have null idKardex

        // Act
        int hash1 = kardex1.hashCode();
        int hash2 = kardex2.hashCode();

        // Assert
        assertEquals(hash1, hash2, "Objects with null ID should have same hashCode");
    }
    @Test
    @DisplayName("Should return formatted string with all fields")
    void testToString_ValidKardex_ReturnsFormattedString() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        kardex.setIdKardex(5L);

        // Act
        String result = kardex.toString();

        // Assert
        assertAll("ToString validation",
            () -> assertTrue(result.contains("id=5")),
            () -> assertTrue(result.contains("type=PURCHASE")),
            () -> assertTrue(result.contains("quantity=100")),
            () -> assertTrue(result.contains("available=100")),
            () -> assertTrue(result.startsWith("Kardex{")),
            () -> assertTrue(result.endsWith("}"))
        );
    }

    @Test
    @DisplayName("Should return correct string for sale movement")
    void testToString_SaleMovement_ReturnsCorrectFormat() {
        // Arrange
        Kardex kardex = Kardex.createSale("2001", "Sale", 50, validPrice, product);
        kardex.setIdKardex(10L);

        // Act
        String result = kardex.toString();

        // Assert
        assertTrue(result.contains("id=10"));
        assertTrue(result.contains("type=SALE"));
        assertTrue(result.contains("quantity=50"));
        assertTrue(result.contains("available=0"));
    }

    @Test
    @DisplayName("Should return correct string with null ID")
    void testToString_NullId_ReturnsStringWithNull() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        // idKardex is null by default

        // Act
        String result = kardex.toString();

        // Assert
        assertTrue(result.contains("id=null"));
    }

    @Test
    @DisplayName("Should return string after quantity reduction")
    void testToString_AfterReduction_ReflectsChanges() {
        // Arrange
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, validPrice, product);
        kardex.setIdKardex(3L);
        kardex.reduceAvailableQuantity(30);

        // Act
        String result = kardex.toString();

        // Assert
        assertTrue(result.contains("available=70"), "Should reflect reduced available quantity");
        assertTrue(result.contains("quantity=100"), "Original quantity should remain unchanged");
    }











    
}
