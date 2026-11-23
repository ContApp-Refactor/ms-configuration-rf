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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.stock.domain.model.SyncState;

public class ProductUniTest {
    private Long validProductId;
    private String validName;
    private String validReference;
    private String validPresentation;
    private String validEnterpriseId;

    @BeforeEach
    void setUp() {
        validProductId = 1L;
        validName = "Avocado";
        validReference = "REF-001";
        validPresentation = "Box";
        validEnterpriseId = "ENT-001";
    }

    @Test
    @DisplayName("create product successfully")
    void testCreate_ValidParameters() {
        // Act
        Product product = Product.create(
            validProductId,
            validName,
            validReference,
            validPresentation,
            validEnterpriseId
        );
        
        // Assert
        assertAll("Product creation validation",
            () -> assertEquals(validProductId, product.getProductId()),
            () -> assertEquals(validName, product.getName()),
            () -> assertEquals(validReference, product.getReference()),
            () -> assertEquals(validPresentation, product.getPresentation()),
            () -> assertEquals(validEnterpriseId, product.getEnterpriseId()),
            () -> assertTrue(product.isActive()),
            () -> assertNotNull(product.getRecordsKardex()),
            () -> assertTrue(product.getRecordsKardex().isEmpty())
        );
    }
    
    @Test
    @DisplayName("trim name when creating product")
    void testCreate_NameWithSpaces_TrimsName() {
        // Arrange
        String nameWithSpaces = "  Test Product  ";
        
        // Act
        Product product = Product.create(
            validProductId,
            nameWithSpaces,
            validReference,
            validPresentation,
            validEnterpriseId
        );
        
        // Assert
        assertEquals("Test Product", product.getName());
    }
    
    @Test
    @DisplayName("throw exception when product ID is null")
    void testCreate_NullProductId() {
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Product.create(null, validName, validReference, validPresentation, validEnterpriseId)
        );
        
        assertEquals("Product ID must be positive", exception.getMessage());
    }
    
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -100})
    @DisplayName("throw exception when product ID is not positive")
    void testCreate_NonPositiveProductId(Long invalidId) {
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Product.create(invalidId, validName, validReference, validPresentation, validEnterpriseId)
        );
        
        assertEquals("Product ID must be positive", exception.getMessage());
    }
    
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("throw exception when name is null or empty")
    void testCreate_InvalidName(String invalidName) {
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Product.create(validProductId, invalidName, validReference, validPresentation, validEnterpriseId)
        );
        
        assertEquals("Product name cannot be empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when name exceeds max length")
    void testCreate_NameTooLong() {
        // Arrange
        String longName = "A".repeat(256);
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Product.create(validProductId, longName, validReference, validPresentation, validEnterpriseId)
        );
        
        assertEquals("Product name is too long (max 255 characters)", exception.getMessage());
    }
    
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t"})
    @DisplayName("throw exception when enterprise ID is null or empty")
    void testCreate_InvalidEnterpriseId(String invalidEnterpriseId) {
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Product.create(validProductId, validName, validReference, validPresentation, invalidEnterpriseId)
        );
        
        assertEquals("Enterprise ID cannot be empty", exception.getMessage());
    }
    

    @Test
    @DisplayName("activate inactive product")
    void testActivate_InactiveProduct_ActivatesSuccessfully() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        product.setState(false); 
        
        // Act
        product.activate();
        
        // Assert
        assertTrue(product.isActive());
    }
    
    @Test
    @DisplayName("throw exception when activating already active product")
    void testActivate_AlreadyActive() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        // Product is active by default
        
        // Act y Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> product.activate()
        );
        
        assertEquals("Product is already active", exception.getMessage());
    }

    @Test
    @DisplayName("deactivate product without stock")
    void testDeactivate_NoStock_DeactivatesSuccessfully() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        
        // Act
        product.deactivate();
        
        // Assert
        assertFalse(product.isActive());
    }
    
    @Test
    @DisplayName("throw exception when deactivating already inactive product")
    void testDeactivate_AlreadyInactive() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        product.setState(false);
        
        // Act y Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> product.deactivate()
        );
        
        assertEquals("Product is already inactive", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when deactivating product with available stock")
    void testDeactivate_WithAvailableStock() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        
        Kardex kardex = Kardex.createPurchase(
            "1001",
            "Purchase",
            100,
            new BigDecimal("10.00"),
            product
        );
        product.addKardexRecord(kardex);
        
        // Act y Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> product.deactivate()
        );
        
        assertEquals("Cannot deactivate product with available stock", exception.getMessage());
    }
    
    @Test
    @DisplayName("deactivate product when stock is fully consumed")
    void testDeactivate_StockFullyConsumed_DeactivatesSuccessfully() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        
        Kardex kardex = Kardex.createPurchase(
            "1001",
            "Purchase",
            100,
            new BigDecimal("10.00"),
            product
        );
        kardex.reduceAvailableQuantity(100); 
        product.addKardexRecord(kardex);
        
        // Act
        product.deactivate();
        
        // Assert
        assertFalse(product.isActive());
    }
    

    @Test
    @DisplayName("return true when product is active")
    void testIsActive_ActiveProduct() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        
        // Act & Assert
        assertTrue(product.isActive());
    }
    
    @Test
    @DisplayName("return false when product is inactive")
    void testIsActive_InactiveProduct() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        product.setState(false);
        
        // Act y Assert
        assertFalse(product.isActive());
    }
    

    @Test
    @DisplayName("return true when has available stock")
    void testHasAvailableStock_WithStock() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        
        Kardex kardex = Kardex.createPurchase(
            "1001",
            "Purchase",
            100,
            new BigDecimal("10.00"),
            product
        );
        product.addKardexRecord(kardex);
        
        // Act y Assert
        assertTrue(product.hasAvailableStock());
    }
    
    @Test
    @DisplayName("return false when no available stock")
    void testHasAvailableStock_NoStock() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        
        Kardex kardex = Kardex.createPurchase(
            "1001",
            "Purchase",
            100,
            new BigDecimal("10.00"),
            product
        );
        kardex.reduceAvailableQuantity(100);
        product.addKardexRecord(kardex);
        
        // Act y Assert
        assertFalse(product.hasAvailableStock());
    }
    
    @Test
    @DisplayName("return false when kardex records is null")
    void testHasAvailableStock_NullRecords() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        product.setRecordsKardex(null);
        
        // Act y Assert
        assertFalse(product.hasAvailableStock());
    }
    

    @Test
    @DisplayName("calculate total available stock correctly")
    void testGetTotalAvailableStock_MultipleRecords_ReturnsCorrectSum() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        
        Kardex kardex1 = Kardex.createPurchase("1001", "Purchase 1", 100, new BigDecimal("10.00"), product);
        Kardex kardex2 = Kardex.createPurchase("1002", "Purchase 2", 50, new BigDecimal("12.00"), product);
        
        product.addKardexRecord(kardex1);
        product.addKardexRecord(kardex2);
        
        // Act
        int totalStock = product.getTotalAvailableStock();
        
        // Assert
        assertEquals(150, totalStock);
    }
    
    @Test
    @DisplayName("return zero when no records")
    void testGetTotalAvailableStock_NoRecords() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        
        // Act & Assert
        assertEquals(0, product.getTotalAvailableStock());
    }
    
    @Test
    @DisplayName("return zero when records is null")
    void testGetTotalAvailableStock_NullRecords() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        product.setRecordsKardex(null);
        
        // Act & Assert
        assertEquals(0, product.getTotalAvailableStock());
    }
    
    @Test
    @DisplayName("calculate correctly with partially consumed stock")
    void testGetTotalAvailableStock_PartiallyConsumed_ReturnsCorrectSum() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        
        Kardex kardex1 = Kardex.createPurchase("1001", "Purchase 1", 100, new BigDecimal("10.00"), product);
        kardex1.reduceAvailableQuantity(30); 
        
        Kardex kardex2 = Kardex.createPurchase("1002", "Purchase 2", 50, new BigDecimal("12.00"), product);
        kardex2.reduceAvailableQuantity(20); 
        product.addKardexRecord(kardex1);
        product.addKardexRecord(kardex2);
        
        // Act
        int totalStock = product.getTotalAvailableStock();
        
        // Assert
        assertEquals(100, totalStock); // 70 + 30
    }
    

    @Test
    @DisplayName("add kardex record successfully")
    void testAddKardexRecord_ValidKardex_AddsToList() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, new BigDecimal("10.00"), product);
        
        // Act
        product.addKardexRecord(kardex);
        
        // Assert
        assertEquals(1, product.getRecordsKardex().size());
        assertTrue(product.getRecordsKardex().contains(kardex));
    }
    
    @Test
    @DisplayName("throw exception when kardex is null")
    void testAddKardexRecord_NullKardex() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        
        // Act y Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> product.addKardexRecord(null)
        );
        
        assertEquals("Kardex record cannot be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when kardex belongs to different product")
    void testAddKardexRecord_DifferentProduct() {
        // Arrange
        Product product1 = Product.create(1L, "Product 1", "REF-001", "Box", "ENT-001");
        Product product2 = Product.create(2L, "Product 2", "REF-002", "Box", "ENT-001");
        
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, new BigDecimal("10.00"), product2);
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> product1.addKardexRecord(kardex)
        );
        
        assertEquals("Kardex record does not belong to this product", exception.getMessage());
    }
    
    @Test
    @DisplayName("initialize list when null before adding")
    void testAddKardexRecord_NullList_InitializesAndAdds() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        product.setRecordsKardex(null);
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, new BigDecimal("10.00"), product);
        
        // Act
        product.addKardexRecord(kardex);
        
        // Assert
        assertNotNull(product.getRecordsKardex());
        assertEquals(1, product.getRecordsKardex().size());
    }
    
    @Test
    @DisplayName("validate successfully for active product")
    void testValidateForMovementCreation_ActiveProduct() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        
        // Act y Assert
        assertDoesNotThrow(() -> product.validateForMovementCreation());
    }
    
    @Test
    @DisplayName("throw exception for inactive product")
    void testValidateForMovementCreation_InactiveProduct() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        product.setState(false);
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> product.validateForMovementCreation()
        );
        
        assertEquals("Cannot create movement for inactive product", exception.getMessage());
    }
    
   
    @Test
    @DisplayName("validate required fields successfully")
    void testValidateRequiredFields_ValidProduct() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        
        // Act y Assert
        assertDoesNotThrow(() -> product.validateRequiredFields());
    }
    
    @Test
    @DisplayName("throw exception when product ID is invalid")
    void testValidateRequiredFields_InvalidProductId() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        product.setProductId(null);
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> product.validateRequiredFields()
        );
        
        assertEquals("Product ID must be positive", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when name is invalid")
    void testValidateRequiredFields_InvalidName() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        product.setName("");
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> product.validateRequiredFields()
        );
        
        assertEquals("Product name cannot be empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when enterprise ID is invalid")
    void testValidateRequiredFields_InvalidEnterpriseId() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        product.setEnterpriseId("  ");
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> product.validateRequiredFields()
        );
        
        assertEquals("Enterprise ID cannot be empty", exception.getMessage());
    }
    

    @Test
    @DisplayName("return unmodifiable list of kardex records")
    void testGetRecordsKardexReadOnly_WithRecords_ReturnsUnmodifiableList() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        Kardex kardex = Kardex.createPurchase("1001", "Purchase", 100, new BigDecimal("10.00"), product);
        product.addKardexRecord(kardex);
        
        // Act
        List<Kardex> readOnly = product.getRecordsKardexReadOnly();
        
        // Assert
        assertEquals(1, readOnly.size());
        assertThrows(UnsupportedOperationException.class,
            () -> readOnly.add(Kardex.createPurchase("1002", "Purchase 2", 50, new BigDecimal("10.00"), product)));
    }
    
    @Test
    @DisplayName("return empty list when records is null")
    void testGetRecordsKardexReadOnly_NullRecords() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        product.setRecordsKardex(null);
        
        // Act
        List<Kardex> readOnly = product.getRecordsKardexReadOnly();
        
        // Assert
        assertNotNull(readOnly);
        assertTrue(readOnly.isEmpty());
    }
    

    @Test
    @DisplayName("be equal when same instance")
    void testEquals_SameInstance() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        
        // Act y Assert
        assertEquals(product, product);
    }
    
    @Test
    @DisplayName("be equal when same product ID")
    void testEquals_SameProductId() {
        // Arrange
        Product product1 = Product.create(1L, "Product 1", "REF-001", "Box", "ENT-001");
        Product product2 = Product.create(1L, "Product 2", "REF-002", "Bottle", "ENT-002");
        
        // Act y Assert
        assertEquals(product1, product2);
    }
    
    @Test
    @DisplayName("not be equal when different product IDs -returns false")
    void testEquals_DifferentProductId() {
        // Arrange
        Product product1 = Product.create(1L, "Product 1", "REF-001", "Box", "ENT-001");
        Product product2 = Product.create(2L, "Product 1", "REF-001", "Box", "ENT-001");
        
        // Act y Assert
        assertNotEquals(product1, product2);
    }
    
    @Test
    @DisplayName("not be equal to null")
    void testEquals_NullObject() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        
        // Act y Assert
        assertNotEquals(product, null);
    }
    
    @Test
    @DisplayName("not be equal to different class - return false")
    void testEquals_DifferentClass() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        String differentObject = "Not a Product";
        
        // Act y Assert
        assertNotEquals(product, differentObject);
    }
    
   
    @Test
    @DisplayName("return same hashCode for equal products")
    void testHashCode_EqualProducts_ReturnsSameHash() {
        // Arrange
        Product product1 = Product.create(1L, "Product 1", "REF-001", "Box", "ENT-001");
        Product product2 = Product.create(1L, "Product 2", "REF-002", "Bottle", "ENT-002");
        
        // Act y Assert
        assertEquals(product1.hashCode(), product2.hashCode());
    }
    
    @Test
    @DisplayName("return consistent hashCode")
    void testHashCode_MultipleInvocations_ReturnsConsistent() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        
        // Act
        int hash1 = product.hashCode();
        int hash2 = product.hashCode();
        int hash3 = product.hashCode();
        
        // Assert
        assertAll("HashCode consistency",
            () -> assertEquals(hash1, hash2),
            () -> assertEquals(hash2, hash3)
        );
    }
  
    @Test
    @DisplayName("validate date consistency successfully")
    void testValidateDateConsistency_ValidDates_NoException() {
        // Arrange 
        Instant baseTime = Instant.now().minus(2, ChronoUnit.HOURS);
        
        SyncState syncState = SyncState.builder()
            .syncType("products")
            .enterpriseId(validEnterpriseId)
            .createdAt(baseTime)
            .updatedAt(baseTime.plus(30, ChronoUnit.MINUTES))
            .lastSyncDate(baseTime.plus(1, ChronoUnit.HOURS))
            .build();
        
        // Act & Assert
        assertDoesNotThrow(() -> syncState.validateDateConsistency());
    }

    
    @Test
    @DisplayName("show correct state in toString")
    void testToString_InactiveProduct_ShowsInactiveState() {
        // Arrange
        Product product = Product.create(validProductId, validName, validReference, validPresentation, validEnterpriseId);
        product.setState(false);
        
        // Act
        String result = product.toString();
        
        // Assert
        assertTrue(result.contains("state=false"));
    }
    
    @Test
    @DisplayName("create product using builder")
    void testBuilder_ValidValues_CreatesProduct() {
        // Arrange & Act
        Product product = Product.builder()
            .productId(1L)
            .name("Builder Product")
            .reference("REF-BUILDER")
            .presentation("Bottle")
            .enterpriseId("ENT-BUILDER")
            .state(true)
            .build();
        
        // Assert
        assertAll("Builder validation",
            () -> assertEquals(1L, product.getProductId()),
            () -> assertEquals("Builder Product", product.getName()),
            () -> assertEquals("REF-BUILDER", product.getReference()),
            () -> assertTrue(product.isActive())
        );
    }
    


}
