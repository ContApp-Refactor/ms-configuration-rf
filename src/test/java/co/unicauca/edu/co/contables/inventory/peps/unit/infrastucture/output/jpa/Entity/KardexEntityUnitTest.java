package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.jpa.Entity;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.inventory.peps.domain.enums.MovementType;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.DetailOutputEntity;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.KardexEntity;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.ProductEntity;

/**
 * @brief Unit tests for KardexEntity
 * 
 * Tests JPA entity for Kardex records including field validation,
 * getters/setters, relationships, and business logic.
 */
@DisplayName("KardexEntity Unit Tests")
class KardexEntityUnitTest {
    
    private KardexEntity kardexEntity;
    private ProductEntity productEntity;
    private ZonedDateTime testDate;

    @BeforeEach
    void setUp() {
        // Initialize test date
        testDate = ZonedDateTime.now(ZoneId.of("America/Bogota"));
        
        // Initialize product entity
        productEntity = new ProductEntity();
        productEntity.setProductId(1L);
        productEntity.setName("Test Product");
        productEntity.setReference("REF-001");
        
        // Initialize kardex entity
        kardexEntity = new KardexEntity();
    }

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Should create entity with no-args constructor")
    void testNoArgsConstructor() {
        // Act
        KardexEntity entity = new KardexEntity();

        // Assert
        assertNotNull(entity);
        assertNull(entity.getIdKardex());
        assertNull(entity.getFactCode());
        assertNull(entity.getDate());
        assertNull(entity.getDetails());
        assertEquals(0, entity.getQuantity());
        assertNull(entity.getUnitPrice());
        assertNull(entity.getType());
        assertEquals(0, entity.getAvailableQuantity());
        assertNull(entity.getProduct());
        assertNull(entity.getDetailsOutput());
        assertNull(entity.getDetailsOrigin());
    }

    @Test
    @DisplayName("Should create entity with all-args constructor")
    void testAllArgsConstructor() {
        // Arrange
        Long id = 1L;
        String factCode = "12345";
        String details = "Purchase";
        int quantity = 100;
        BigDecimal unitPrice = new BigDecimal("50.00");
        MovementType type = MovementType.PURCHASE;
        int availableQuantity = 100;
        List<DetailOutputEntity> detailsOutput = new ArrayList<>();
        List<DetailOutputEntity> detailsOrigin = new ArrayList<>();

        // Act
        KardexEntity entity = new KardexEntity(
            id, factCode, testDate, details, quantity, unitPrice,
            type, availableQuantity, productEntity, detailsOutput, detailsOrigin
        );

        // Assert
        assertEquals(id, entity.getIdKardex());
        assertEquals(factCode, entity.getFactCode());
        assertEquals(testDate, entity.getDate());
        assertEquals(details, entity.getDetails());
        assertEquals(quantity, entity.getQuantity());
        assertEquals(unitPrice, entity.getUnitPrice());
        assertEquals(type, entity.getType());
        assertEquals(availableQuantity, entity.getAvailableQuantity());
        assertEquals(productEntity, entity.getProduct());
        assertEquals(detailsOutput, entity.getDetailsOutput());
        assertEquals(detailsOrigin, entity.getDetailsOrigin());
    }

    // ==================== ID Tests ====================

    @Test
    @DisplayName("Should set and get idKardex correctly")
    void testIdKardexGetterSetter() {
        // Arrange
        Long expectedId = 999L;

        // Act
        kardexEntity.setIdKardex(expectedId);

        // Assert
        assertEquals(expectedId, kardexEntity.getIdKardex());
    }

    @Test
    @DisplayName("Should handle null idKardex")
    void testNullIdKardex() {
        // Act
        kardexEntity.setIdKardex(null);

        // Assert
        assertNull(kardexEntity.getIdKardex());
    }

    // ==================== FactCode Tests ====================

    @Test
    @DisplayName("Should set and get factCode correctly")
    void testFactCodeGetterSetter() {
        // Arrange
        String expectedFactCode = "54321";

        // Act
        kardexEntity.setFactCode(expectedFactCode);

        // Assert
        assertEquals(expectedFactCode, kardexEntity.getFactCode());
    }

    @Test
    @DisplayName("Should handle null factCode")
    void testNullFactCode() {
        // Act
        kardexEntity.setFactCode(null);

        // Assert
        assertNull(kardexEntity.getFactCode());
    }

    @Test
    @DisplayName("Should handle large factCode values")
    void testLargeFactCode() {
        // Arrange
        String largeFactCode = "9999999999999";

        // Act
        kardexEntity.setFactCode(largeFactCode);

        // Assert
        assertEquals(largeFactCode, kardexEntity.getFactCode());
    }

    // ==================== Date Tests ====================

    @Test
    @DisplayName("Should set and get date correctly")
    void testDateGetterSetter() {
        // Act
        kardexEntity.setDate(testDate);

        // Assert
        assertEquals(testDate, kardexEntity.getDate());
    }

    @Test
    @DisplayName("Should handle null date")
    void testNullDate() {
        // Act
        kardexEntity.setDate(null);

        // Assert
        assertNull(kardexEntity.getDate());
    }

    @Test
    @DisplayName("Should handle different time zones")
    void testDifferentTimeZones() {
        // Arrange
        ZonedDateTime utcDate = ZonedDateTime.now(ZoneId.of("UTC"));

        // Act
        kardexEntity.setDate(utcDate);

        // Assert
        assertEquals(utcDate, kardexEntity.getDate());
    }

    // ==================== Details Tests ====================

    @Test
    @DisplayName("Should set and get details correctly")
    void testDetailsGetterSetter() {
        // Arrange
        String expectedDetails = "Initial inventory purchase";

        // Act
        kardexEntity.setDetails(expectedDetails);

        // Assert
        assertEquals(expectedDetails, kardexEntity.getDetails());
    }

    @Test
    @DisplayName("Should handle empty details")
    void testEmptyDetails() {
        // Act
        kardexEntity.setDetails("");

        // Assert
        assertEquals("", kardexEntity.getDetails());
    }

    @Test
    @DisplayName("Should handle null details")
    void testNullDetails() {
        // Act
        kardexEntity.setDetails(null);

        // Assert
        assertNull(kardexEntity.getDetails());
    }

    @Test
    @DisplayName("Should handle long details text")
    void testLongDetails() {
        // Arrange
        String longDetails = "A".repeat(500);

        // Act
        kardexEntity.setDetails(longDetails);

        // Assert
        assertEquals(longDetails, kardexEntity.getDetails());
        assertEquals(500, kardexEntity.getDetails().length());
    }

    // ==================== Quantity Tests ====================

    @Test
    @DisplayName("Should set and get quantity correctly")
    void testQuantityGetterSetter() {
        // Arrange
        int expectedQuantity = 250;

        // Act
        kardexEntity.setQuantity(expectedQuantity);

        // Assert
        assertEquals(expectedQuantity, kardexEntity.getQuantity());
    }

    @Test
    @DisplayName("Should handle zero quantity")
    void testZeroQuantity() {
        // Act
        kardexEntity.setQuantity(0);

        // Assert
        assertEquals(0, kardexEntity.getQuantity());
    }

    @Test
    @DisplayName("Should handle negative quantity")
    void testNegativeQuantity() {
        // Act
        kardexEntity.setQuantity(-50);

        // Assert
        assertEquals(-50, kardexEntity.getQuantity());
    }

    @Test
    @DisplayName("Should handle large quantity values")
    void testLargeQuantity() {
        // Arrange
        int largeQuantity = Integer.MAX_VALUE;

        // Act
        kardexEntity.setQuantity(largeQuantity);

        // Assert
        assertEquals(largeQuantity, kardexEntity.getQuantity());
    }

    // ==================== Unit Price Tests ====================

    @Test
    @DisplayName("Should set and get unitPrice correctly")
    void testUnitPriceGetterSetter() {
        // Arrange
        BigDecimal expectedPrice = new BigDecimal("125.75");

        // Act
        kardexEntity.setUnitPrice(expectedPrice);

        // Assert
        assertEquals(0, expectedPrice.compareTo(kardexEntity.getUnitPrice()));
    }

    @Test
    @DisplayName("Should handle null unitPrice")
    void testNullUnitPrice() {
        // Act
        kardexEntity.setUnitPrice(null);

        // Assert
        assertNull(kardexEntity.getUnitPrice());
    }

    @Test
    @DisplayName("Should handle zero unitPrice")
    void testZeroUnitPrice() {
        // Arrange
        BigDecimal zero = BigDecimal.ZERO;

        // Act
        kardexEntity.setUnitPrice(zero);

        // Assert
        assertEquals(0, zero.compareTo(kardexEntity.getUnitPrice()));
    }

    @Test
    @DisplayName("Should handle unitPrice with multiple decimals")
    void testUnitPriceWithMultipleDecimals() {
        // Arrange
        BigDecimal price = new BigDecimal("99.9999");

        // Act
        kardexEntity.setUnitPrice(price);

        // Assert
        assertEquals(0, price.compareTo(kardexEntity.getUnitPrice()));
    }

    @Test
    @DisplayName("Should handle very large unitPrice")
    void testVeryLargeUnitPrice() {
        // Arrange
        BigDecimal largePrice = new BigDecimal("9999999999.99");

        // Act
        kardexEntity.setUnitPrice(largePrice);

        // Assert
        assertEquals(0, largePrice.compareTo(kardexEntity.getUnitPrice()));
    }

    // ==================== Movement Type Tests ====================

    @Test
    @DisplayName("Should set and get PURCHASE type correctly")
    void testPurchaseType() {
        // Act
        kardexEntity.setType(MovementType.PURCHASE);

        // Assert
        assertEquals(MovementType.PURCHASE, kardexEntity.getType());
    }

    @Test
    @DisplayName("Should set and get SALE type correctly")
    void testSaleType() {
        // Act
        kardexEntity.setType(MovementType.SALE);

        // Assert
        assertEquals(MovementType.SALE, kardexEntity.getType());
    }

    @Test
    @DisplayName("Should set and get PURCHASERETURN type correctly")
    void testPurchaseReturnType() {
        // Act
        kardexEntity.setType(MovementType.PURCHASERETURN);

        // Assert
        assertEquals(MovementType.PURCHASERETURN, kardexEntity.getType());
    }

    @Test
    @DisplayName("Should set and get SALESRETURN type correctly")
    void testSalesReturnType() {
        // Act
        kardexEntity.setType(MovementType.SALESRETURN);

        // Assert
        assertEquals(MovementType.SALESRETURN, kardexEntity.getType());
    }

    @Test
    @DisplayName("Should set and get NONCOMMERCIALENTRY type correctly")
    void testNonCommercialEntryType() {
        // Act
        kardexEntity.setType(MovementType.NONCOMMERCIALENTRY);

        // Assert
        assertEquals(MovementType.NONCOMMERCIALENTRY, kardexEntity.getType());
    }

    @Test
    @DisplayName("Should set and get NONCOMMERCIALEXIT type correctly")
    void testNonCommercialExitType() {
        // Act
        kardexEntity.setType(MovementType.NONCOMMERCIALEXIT);

        // Assert
        assertEquals(MovementType.NONCOMMERCIALEXIT, kardexEntity.getType());
    }

    @Test
    @DisplayName("Should handle null type")
    void testNullType() {
        // Act
        kardexEntity.setType(null);

        // Assert
        assertNull(kardexEntity.getType());
    }

    // ==================== Available Quantity Tests ====================

    @Test
    @DisplayName("Should set and get availableQuantity correctly")
    void testAvailableQuantityGetterSetter() {
        // Arrange
        int expectedAvailable = 75;

        // Act
        kardexEntity.setAvailableQuantity(expectedAvailable);

        // Assert
        assertEquals(expectedAvailable, kardexEntity.getAvailableQuantity());
    }

    @Test
    @DisplayName("Should handle zero availableQuantity")
    void testZeroAvailableQuantity() {
        // Act
        kardexEntity.setAvailableQuantity(0);

        // Assert
        assertEquals(0, kardexEntity.getAvailableQuantity());
    }

    @Test
    @DisplayName("Should handle negative availableQuantity")
    void testNegativeAvailableQuantity() {
        // Act
        kardexEntity.setAvailableQuantity(-10);

        // Assert
        assertEquals(-10, kardexEntity.getAvailableQuantity());
    }

    @Test
    @DisplayName("AvailableQuantity should be less than or equal to quantity")
    void testAvailableQuantityLogic() {
        // Act
        kardexEntity.setQuantity(100);
        kardexEntity.setAvailableQuantity(80);

        // Assert
        assertTrue(kardexEntity.getAvailableQuantity() <= kardexEntity.getQuantity());
    }

    // ==================== Product Relationship Tests ====================

    @Test
    @DisplayName("Should set and get product correctly")
    void testProductGetterSetter() {
        // Act
        kardexEntity.setProduct(productEntity);

        // Assert
        assertEquals(productEntity, kardexEntity.getProduct());
        assertEquals(1L, kardexEntity.getProduct().getProductId());
        assertEquals("Test Product", kardexEntity.getProduct().getName());
    }

    @Test
    @DisplayName("Should handle null product")
    void testNullProduct() {
        // Act
        kardexEntity.setProduct(null);

        // Assert
        assertNull(kardexEntity.getProduct());
    }

    @Test
    @DisplayName("Should maintain product relationship after updates")
    void testProductRelationshipPersistence() {
        // Act
        kardexEntity.setProduct(productEntity);
        kardexEntity.setQuantity(50);
        kardexEntity.setUnitPrice(new BigDecimal("100.00"));

        // Assert
        assertNotNull(kardexEntity.getProduct());
        assertEquals(productEntity, kardexEntity.getProduct());
    }

    // ==================== Details Output List Tests ====================

    @Test
    @DisplayName("Should set and get detailsOutput list correctly")
    void testDetailsOutputGetterSetter() {
        // Arrange
        List<DetailOutputEntity> detailsList = new ArrayList<>();
        DetailOutputEntity detail1 = new DetailOutputEntity();
        detail1.setIdDetailOutput(1L);
        detailsList.add(detail1);

        // Act
        kardexEntity.setDetailsOutput(detailsList);

        // Assert
        assertEquals(detailsList, kardexEntity.getDetailsOutput());
        assertEquals(1, kardexEntity.getDetailsOutput().size());
    }

    @Test
    @DisplayName("Should handle empty detailsOutput list")
    void testEmptyDetailsOutputList() {
        // Arrange
        List<DetailOutputEntity> emptyList = new ArrayList<>();

        // Act
        kardexEntity.setDetailsOutput(emptyList);

        // Assert
        assertNotNull(kardexEntity.getDetailsOutput());
        assertTrue(kardexEntity.getDetailsOutput().isEmpty());
    }

    @Test
    @DisplayName("Should handle null detailsOutput list")
    void testNullDetailsOutputList() {
        // Act
        kardexEntity.setDetailsOutput(null);

        // Assert
        assertNull(kardexEntity.getDetailsOutput());
    }

    @Test
    @DisplayName("Should handle multiple detailsOutput entries")
    void testMultipleDetailsOutput() {
        // Arrange
        List<DetailOutputEntity> detailsList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            DetailOutputEntity detail = new DetailOutputEntity();
            detail.setIdDetailOutput((long) i);
            detailsList.add(detail);
        }

        // Act
        kardexEntity.setDetailsOutput(detailsList);

        // Assert
        assertEquals(5, kardexEntity.getDetailsOutput().size());
    }

    // ==================== Details Origin List Tests ====================

    @Test
    @DisplayName("Should set and get detailsOrigin list correctly")
    void testDetailsOriginGetterSetter() {
        // Arrange
        List<DetailOutputEntity> detailsList = new ArrayList<>();
        DetailOutputEntity detail1 = new DetailOutputEntity();
        detail1.setIdDetailOutput(1L);
        detailsList.add(detail1);

        // Act
        kardexEntity.setDetailsOrigin(detailsList);

        // Assert
        assertEquals(detailsList, kardexEntity.getDetailsOrigin());
        assertEquals(1, kardexEntity.getDetailsOrigin().size());
    }

    @Test
    @DisplayName("Should handle empty detailsOrigin list")
    void testEmptyDetailsOriginList() {
        // Arrange
        List<DetailOutputEntity> emptyList = new ArrayList<>();

        // Act
        kardexEntity.setDetailsOrigin(emptyList);

        // Assert
        assertNotNull(kardexEntity.getDetailsOrigin());
        assertTrue(kardexEntity.getDetailsOrigin().isEmpty());
    }

    @Test
    @DisplayName("Should handle null detailsOrigin list")
    void testNullDetailsOriginList() {
        // Act
        kardexEntity.setDetailsOrigin(null);

        // Assert
        assertNull(kardexEntity.getDetailsOrigin());
    }

    @Test
    @DisplayName("Should handle multiple detailsOrigin entries")
    void testMultipleDetailsOrigin() {
        // Arrange
        List<DetailOutputEntity> detailsList = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            DetailOutputEntity detail = new DetailOutputEntity();
            detail.setIdDetailOutput((long) i);
            detailsList.add(detail);
        }

        // Act
        kardexEntity.setDetailsOrigin(detailsList);

        // Assert
        assertEquals(3, kardexEntity.getDetailsOrigin().size());
    }

    // ==================== Complete Entity Tests ====================

    @Test
    @DisplayName("Should create complete purchase kardex entity")
    void testCompletePurchaseEntity() {
        // Arrange & Act
        kardexEntity.setIdKardex(1L);
        kardexEntity.setFactCode("12345");
        kardexEntity.setDate(testDate);
        kardexEntity.setDetails("Purchase from supplier XYZ");
        kardexEntity.setQuantity(100);
        kardexEntity.setUnitPrice(new BigDecimal("50.00"));
        kardexEntity.setType(MovementType.PURCHASE);
        kardexEntity.setAvailableQuantity(100);
        kardexEntity.setProduct(productEntity);
        kardexEntity.setDetailsOutput(new ArrayList<>());
        kardexEntity.setDetailsOrigin(new ArrayList<>());

        // Assert
        assertEquals(1L, kardexEntity.getIdKardex());
        assertEquals("12345", kardexEntity.getFactCode());
        assertEquals(testDate, kardexEntity.getDate());
        assertEquals("Purchase from supplier XYZ", kardexEntity.getDetails());
        assertEquals(100, kardexEntity.getQuantity());
        assertEquals(0, new BigDecimal("50.00").compareTo(kardexEntity.getUnitPrice()));
        assertEquals(MovementType.PURCHASE, kardexEntity.getType());
        assertEquals(100, kardexEntity.getAvailableQuantity());
        assertNotNull(kardexEntity.getProduct());
        assertNotNull(kardexEntity.getDetailsOutput());
        assertNotNull(kardexEntity.getDetailsOrigin());
    }

    @Test
    @DisplayName("Should create complete sale kardex entity")
    void testCompleteSaleEntity() {
        // Arrange & Act
        kardexEntity.setIdKardex(2L);
        kardexEntity.setFactCode("67890");
        kardexEntity.setDate(testDate);
        kardexEntity.setDetails("Sale to customer ABC");
        kardexEntity.setQuantity(50);
        kardexEntity.setUnitPrice(new BigDecimal("75.00"));
        kardexEntity.setType(MovementType.SALE);
        kardexEntity.setAvailableQuantity(0);
        kardexEntity.setProduct(productEntity);

        // Assert
        assertEquals(2L, kardexEntity.getIdKardex());
        assertEquals("67890", kardexEntity.getFactCode());
        assertEquals(MovementType.SALE, kardexEntity.getType());
        assertEquals(0, kardexEntity.getAvailableQuantity());
    }

    @Test
    @DisplayName("Should calculate total value correctly")
    void testCalculateTotalValue() {
        // Arrange
        kardexEntity.setQuantity(10);
        kardexEntity.setUnitPrice(new BigDecimal("100.50"));

        // Act
        BigDecimal totalValue = kardexEntity.getUnitPrice()
            .multiply(BigDecimal.valueOf(kardexEntity.getQuantity()));

        // Assert
        assertEquals(new BigDecimal("1005.00"), totalValue);
    }

    @Test
    @DisplayName("Should update all fields correctly")
    void testUpdateAllFields() {
        // Arrange - Initial values
        kardexEntity.setIdKardex(1L);
        kardexEntity.setFactCode("11111");
        kardexEntity.setQuantity(100);
        kardexEntity.setType(MovementType.PURCHASE);

        // Act - Update values
        kardexEntity.setIdKardex(2L);
        kardexEntity.setFactCode("22222");
        kardexEntity.setQuantity(200);
        kardexEntity.setType(MovementType.SALE);
        kardexEntity.setDetails("Updated details");

        // Assert
        assertEquals(2L, kardexEntity.getIdKardex());
        assertEquals("22222", kardexEntity.getFactCode());
        assertEquals(200, kardexEntity.getQuantity());
        assertEquals(MovementType.SALE, kardexEntity.getType());
        assertEquals("Updated details", kardexEntity.getDetails());
    }

    @Test
    @DisplayName("Should maintain data integrity across multiple operations")
    void testDataIntegrityAcrossOperations() {
        // Arrange & Act
        kardexEntity.setProduct(productEntity);
        kardexEntity.setQuantity(100);
        kardexEntity.setAvailableQuantity(100);
        
        // Simulate sale reducing available quantity
        kardexEntity.setAvailableQuantity(kardexEntity.getAvailableQuantity() - 30);
        
        // Assert
        assertEquals(100, kardexEntity.getQuantity()); // Original quantity unchanged
        assertEquals(70, kardexEntity.getAvailableQuantity()); // Available reduced
        assertNotNull(kardexEntity.getProduct()); // Product relationship maintained
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle entity with minimal required fields")
    void testMinimalRequiredFields() {
        // Act
        kardexEntity.setDate(testDate);
        kardexEntity.setDetails("Minimal");
        kardexEntity.setQuantity(1);
        kardexEntity.setProduct(productEntity);

        // Assert
        assertNotNull(kardexEntity.getDate());
        assertNotNull(kardexEntity.getDetails());
        assertEquals(1, kardexEntity.getQuantity());
        assertNotNull(kardexEntity.getProduct());
    }

    @Test
    @DisplayName("Should handle same factCode for different movements")
    void testSameFactCodeDifferentMovements() {
        // Arrange
        KardexEntity kardex1 = new KardexEntity();
        KardexEntity kardex2 = new KardexEntity();
        String sharedFactCode = "99999";

        // Act
        kardex1.setFactCode(sharedFactCode);
        kardex1.setType(MovementType.PURCHASE);
        
        kardex2.setFactCode(sharedFactCode);
        kardex2.setType(MovementType.PURCHASERETURN);

        // Assert
        assertEquals(kardex1.getFactCode(), kardex2.getFactCode());
        assertNotEquals(kardex1.getType(), kardex2.getType());
    }

    @Test
    @DisplayName("Should handle bidirectional relationship with details")
    void testBidirectionalRelationshipWithDetails() {
        // Arrange
        List<DetailOutputEntity> outputDetails = new ArrayList<>();
        DetailOutputEntity detail = new DetailOutputEntity();
        detail.setMovementSale(kardexEntity);
        outputDetails.add(detail);

        // Act
        kardexEntity.setDetailsOutput(outputDetails);

        // Assert
        assertEquals(1, kardexEntity.getDetailsOutput().size());
        assertEquals(kardexEntity, kardexEntity.getDetailsOutput().get(0).getMovementSale());
    }
}
