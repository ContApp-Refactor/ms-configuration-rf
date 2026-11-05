package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.jpa.Entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.KardexEntity;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.ProductEntity;

/**
 * @brief Unit tests for ProductEntity
 * 
 * Tests JPA entity for Product including field validation,
 * getters/setters, and relationships with Kardex entities.
 */
@DisplayName("ProductEntity Unit Tests")
class ProductEntityUnitTest {
    
    private ProductEntity productEntity;

    @BeforeEach
    void setUp() {
        productEntity = new ProductEntity();
    }

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Should create entity with no-args constructor")
    void testNoArgsConstructor() {
        // Act
        ProductEntity entity = new ProductEntity();

        // Assert
        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getProductId());
        assertNull(entity.getName());
        assertNull(entity.getReference());
        assertNull(entity.getPresentation());
        assertNull(entity.getEnterpriseId());
        assertFalse(entity.isState());
        assertNull(entity.getRecordsKardex());
    }

    @Test
    @DisplayName("Should create entity with all-args constructor")
    void testAllArgsConstructor() {
        // Arrange
        Long id = 1L;
        Long productId = 100L;
        String name = "Test Product";
        String reference = "REF-001";
        String presentation = "Box of 10";
        String enterpriseId = "ENT-123";
        boolean state = true;
        List<KardexEntity> kardexList = new ArrayList<>();

        // Act
        ProductEntity entity = new ProductEntity(
            id, productId, name, reference, presentation, 
            enterpriseId, state, kardexList
        );

        // Assert
        assertEquals(id, entity.getId());
        assertEquals(productId, entity.getProductId());
        assertEquals(name, entity.getName());
        assertEquals(reference, entity.getReference());
        assertEquals(presentation, entity.getPresentation());
        assertEquals(enterpriseId, entity.getEnterpriseId());
        assertTrue(entity.isState());
        assertEquals(kardexList, entity.getRecordsKardex());
    }

    // ==================== ID Tests ====================

    @Test
    @DisplayName("Should set and get id correctly")
    void testIdGetterSetter() {
        // Arrange
        Long expectedId = 999L;

        // Act
        productEntity.setId(expectedId);

        // Assert
        assertEquals(expectedId, productEntity.getId());
    }

    @Test
    @DisplayName("Should handle null id")
    void testNullId() {
        // Act
        productEntity.setId(null);

        // Assert
        assertNull(productEntity.getId());
    }

    // ==================== Product ID Tests ====================

    @Test
    @DisplayName("Should set and get productId correctly")
    void testProductIdGetterSetter() {
        // Arrange
        Long expectedProductId = 12345L;

        // Act
        productEntity.setProductId(expectedProductId);

        // Assert
        assertEquals(expectedProductId, productEntity.getProductId());
    }

    @Test
    @DisplayName("Should handle null productId")
    void testNullProductId() {
        // Act
        productEntity.setProductId(null);

        // Assert
        assertNull(productEntity.getProductId());
    }

    @Test
    @DisplayName("Should handle large productId values")
    void testLargeProductId() {
        // Arrange
        Long largeProductId = Long.MAX_VALUE;

        // Act
        productEntity.setProductId(largeProductId);

        // Assert
        assertEquals(largeProductId, productEntity.getProductId());
    }

    // ==================== Name Tests ====================

    @Test
    @DisplayName("Should set and get name correctly")
    void testNameGetterSetter() {
        // Arrange
        String expectedName = "Premium Widget";

        // Act
        productEntity.setName(expectedName);

        // Assert
        assertEquals(expectedName, productEntity.getName());
    }

    @Test
    @DisplayName("Should handle null name")
    void testNullName() {
        // Act
        productEntity.setName(null);

        // Assert
        assertNull(productEntity.getName());
    }

    @Test
    @DisplayName("Should handle empty name")
    void testEmptyName() {
        // Act
        productEntity.setName("");

        // Assert
        assertEquals("", productEntity.getName());
    }

    @Test
    @DisplayName("Should handle name at max length (50 chars)")
    void testNameMaxLength() {
        // Arrange
        String maxLengthName = "A".repeat(50);

        // Act
        productEntity.setName(maxLengthName);

        // Assert
        assertEquals(maxLengthName, productEntity.getName());
        assertEquals(50, productEntity.getName().length());
    }

    @Test
    @DisplayName("Should handle name with special characters")
    void testNameWithSpecialCharacters() {
        // Arrange
        String nameWithSpecialChars = "Product & Co. (2024)";

        // Act
        productEntity.setName(nameWithSpecialChars);

        // Assert
        assertEquals(nameWithSpecialChars, productEntity.getName());
    }

    @Test
    @DisplayName("Should handle name with numbers")
    void testNameWithNumbers() {
        // Arrange
        String nameWithNumbers = "Product 123 Version 2.0";

        // Act
        productEntity.setName(nameWithNumbers);

        // Assert
        assertEquals(nameWithNumbers, productEntity.getName());
    }

    // ==================== Reference Tests ====================

    @Test
    @DisplayName("Should set and get reference correctly")
    void testReferenceGetterSetter() {
        // Arrange
        String expectedReference = "REF-2024-001";

        // Act
        productEntity.setReference(expectedReference);

        // Assert
        assertEquals(expectedReference, productEntity.getReference());
    }

    @Test
    @DisplayName("Should handle null reference")
    void testNullReference() {
        // Act
        productEntity.setReference(null);

        // Assert
        assertNull(productEntity.getReference());
    }

    @Test
    @DisplayName("Should handle empty reference")
    void testEmptyReference() {
        // Act
        productEntity.setReference("");

        // Assert
        assertEquals("", productEntity.getReference());
    }

    @Test
    @DisplayName("Should handle reference at max length (50 chars)")
    void testReferenceMaxLength() {
        // Arrange
        String maxLengthRef = "REF-" + "X".repeat(46);

        // Act
        productEntity.setReference(maxLengthRef);

        // Assert
        assertEquals(maxLengthRef, productEntity.getReference());
        assertEquals(50, productEntity.getReference().length());
    }

    @Test
    @DisplayName("Should handle reference with alphanumeric pattern")
    void testReferenceAlphanumeric() {
        // Arrange
        String alphanumericRef = "ABC123-XYZ789";

        // Act
        productEntity.setReference(alphanumericRef);

        // Assert
        assertEquals(alphanumericRef, productEntity.getReference());
    }

    // ==================== Presentation Tests ====================

    @Test
    @DisplayName("Should set and get presentation correctly")
    void testPresentationGetterSetter() {
        // Arrange
        String expectedPresentation = "Box of 24 units";

        // Act
        productEntity.setPresentation(expectedPresentation);

        // Assert
        assertEquals(expectedPresentation, productEntity.getPresentation());
    }

    @Test
    @DisplayName("Should handle null presentation")
    void testNullPresentation() {
        // Act
        productEntity.setPresentation(null);

        // Assert
        assertNull(productEntity.getPresentation());
    }

    @Test
    @DisplayName("Should handle empty presentation")
    void testEmptyPresentation() {
        // Act
        productEntity.setPresentation("");

        // Assert
        assertEquals("", productEntity.getPresentation());
    }

    @Test
    @DisplayName("Should handle presentation at max length (50 chars)")
    void testPresentationMaxLength() {
        // Arrange
        String maxLengthPresentation = "P".repeat(50);

        // Act
        productEntity.setPresentation(maxLengthPresentation);

        // Assert
        assertEquals(maxLengthPresentation, productEntity.getPresentation());
        assertEquals(50, productEntity.getPresentation().length());
    }

    @Test
    @DisplayName("Should handle different presentation formats")
    void testDifferentPresentationFormats() {
        // Test bottle
        productEntity.setPresentation("Bottle 500ml");
        assertEquals("Bottle 500ml", productEntity.getPresentation());

        // Test box
        productEntity.setPresentation("Box of 12");
        assertEquals("Box of 12", productEntity.getPresentation());

        // Test unit
        productEntity.setPresentation("Individual unit");
        assertEquals("Individual unit", productEntity.getPresentation());
    }

    // ==================== Enterprise ID Tests ====================

    @Test
    @DisplayName("Should set and get enterpriseId correctly")
    void testEnterpriseIdGetterSetter() {
        // Arrange
        String expectedEnterpriseId = "ENT-456-789";

        // Act
        productEntity.setEnterpriseId(expectedEnterpriseId);

        // Assert
        assertEquals(expectedEnterpriseId, productEntity.getEnterpriseId());
    }

    @Test
    @DisplayName("Should handle null enterpriseId")
    void testNullEnterpriseId() {
        // Act
        productEntity.setEnterpriseId(null);

        // Assert
        assertNull(productEntity.getEnterpriseId());
    }

    @Test
    @DisplayName("Should handle empty enterpriseId")
    void testEmptyEnterpriseId() {
        // Act
        productEntity.setEnterpriseId("");

        // Assert
        assertEquals("", productEntity.getEnterpriseId());
    }

    @Test
    @DisplayName("Should handle UUID format enterpriseId")
    void testUuidEnterpriseId() {
        // Arrange
        String uuidEnterpriseId = "550e8400-e29b-41d4-a716-446655440000";

        // Act
        productEntity.setEnterpriseId(uuidEnterpriseId);

        // Assert
        assertEquals(uuidEnterpriseId, productEntity.getEnterpriseId());
    }

    @Test
    @DisplayName("Should handle long enterpriseId")
    void testLongEnterpriseId() {
        // Arrange
        String longEnterpriseId = "ENTERPRISE-" + "X".repeat(100);

        // Act
        productEntity.setEnterpriseId(longEnterpriseId);

        // Assert
        assertEquals(longEnterpriseId, productEntity.getEnterpriseId());
    }

    // ==================== State Tests ====================

    @Test
    @DisplayName("Should set and get state as true")
    void testStateTrueGetterSetter() {
        // Act
        productEntity.setState(true);

        // Assert
        assertTrue(productEntity.isState());
    }

    @Test
    @DisplayName("Should set and get state as false")
    void testStateFalseGetterSetter() {
        // Act
        productEntity.setState(false);

        // Assert
        assertFalse(productEntity.isState());
    }

    @Test
    @DisplayName("Should handle state toggle")
    void testStateToggle() {
        // Act
        productEntity.setState(true);
        boolean firstState = productEntity.isState();
        
        productEntity.setState(false);
        boolean secondState = productEntity.isState();

        // Assert
        assertTrue(firstState);
        assertFalse(secondState);
    }

    @Test
    @DisplayName("Should default state to false")
    void testDefaultState() {
        // Act
        ProductEntity entity = new ProductEntity();

        // Assert
        assertFalse(entity.isState());
    }

    // ==================== Records Kardex Tests ====================

    @Test
    @DisplayName("Should set and get recordsKardex list correctly")
    void testRecordsKardexGetterSetter() {
        // Arrange
        List<KardexEntity> kardexList = new ArrayList<>();
        KardexEntity kardex1 = new KardexEntity();
        kardex1.setIdKardex(1L);
        kardexList.add(kardex1);

        // Act
        productEntity.setRecordsKardex(kardexList);

        // Assert
        assertEquals(kardexList, productEntity.getRecordsKardex());
        assertEquals(1, productEntity.getRecordsKardex().size());
    }

    @Test
    @DisplayName("Should handle null recordsKardex list")
    void testNullRecordsKardex() {
        // Act
        productEntity.setRecordsKardex(null);

        // Assert
        assertNull(productEntity.getRecordsKardex());
    }

    @Test
    @DisplayName("Should handle empty recordsKardex list")
    void testEmptyRecordsKardex() {
        // Arrange
        List<KardexEntity> emptyList = new ArrayList<>();

        // Act
        productEntity.setRecordsKardex(emptyList);

        // Assert
        assertNotNull(productEntity.getRecordsKardex());
        assertTrue(productEntity.getRecordsKardex().isEmpty());
    }

    @Test
    @DisplayName("Should handle multiple kardex records")
    void testMultipleKardexRecords() {
        // Arrange
        List<KardexEntity> kardexList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            KardexEntity kardex = new KardexEntity();
            kardex.setIdKardex((long) i);
            kardexList.add(kardex);
        }

        // Act
        productEntity.setRecordsKardex(kardexList);

        // Assert
        assertEquals(5, productEntity.getRecordsKardex().size());
    }

    @Test
    @DisplayName("Should maintain bidirectional relationship with kardex")
    void testBidirectionalRelationshipWithKardex() {
        // Arrange
        List<KardexEntity> kardexList = new ArrayList<>();
        KardexEntity kardex = new KardexEntity();
        kardex.setIdKardex(1L);
        kardex.setProduct(productEntity);
        kardexList.add(kardex);

        // Act
        productEntity.setRecordsKardex(kardexList);

        // Assert
        assertEquals(1, productEntity.getRecordsKardex().size());
        assertEquals(productEntity, productEntity.getRecordsKardex().get(0).getProduct());
    }

    // ==================== Complete Entity Tests ====================

    @Test
    @DisplayName("Should create complete valid product entity")
    void testCompleteValidEntity() {
        // Arrange & Act
        productEntity.setId(1L);
        productEntity.setProductId(12345L);
        productEntity.setName("Premium Widget");
        productEntity.setReference("REF-2024-001");
        productEntity.setPresentation("Box of 24 units");
        productEntity.setEnterpriseId("ENT-789");
        productEntity.setState(true);
        productEntity.setRecordsKardex(new ArrayList<>());

        // Assert
        assertNotNull(productEntity);
        assertEquals(1L, productEntity.getId());
        assertEquals(12345L, productEntity.getProductId());
        assertEquals("Premium Widget", productEntity.getName());
        assertEquals("REF-2024-001", productEntity.getReference());
        assertEquals("Box of 24 units", productEntity.getPresentation());
        assertEquals("ENT-789", productEntity.getEnterpriseId());
        assertTrue(productEntity.isState());
        assertNotNull(productEntity.getRecordsKardex());
    }

    @Test
    @DisplayName("Should create entity with minimal required fields")
    void testMinimalRequiredFields() {
        // Act
        productEntity.setProductId(100L);
        productEntity.setName("Minimal Product");
        productEntity.setReference("MIN-001");
        productEntity.setPresentation("Unit");
        productEntity.setEnterpriseId("ENT-001");
        productEntity.setState(true);

        // Assert
        assertNotNull(productEntity.getProductId());
        assertNotNull(productEntity.getName());
        assertNotNull(productEntity.getReference());
        assertNotNull(productEntity.getPresentation());
        assertNotNull(productEntity.getEnterpriseId());
        assertTrue(productEntity.isState());
    }

    @Test
    @DisplayName("Should update all fields correctly")
    void testUpdateAllFields() {
        // Arrange - Initial values
        productEntity.setId(1L);
        productEntity.setProductId(100L);
        productEntity.setName("Old Product");
        productEntity.setReference("OLD-REF");
        productEntity.setState(false);

        // Act - Update values
        productEntity.setId(2L);
        productEntity.setProductId(200L);
        productEntity.setName("New Product");
        productEntity.setReference("NEW-REF");
        productEntity.setPresentation("Updated presentation");
        productEntity.setEnterpriseId("NEW-ENT");
        productEntity.setState(true);

        // Assert
        assertEquals(2L, productEntity.getId());
        assertEquals(200L, productEntity.getProductId());
        assertEquals("New Product", productEntity.getName());
        assertEquals("NEW-REF", productEntity.getReference());
        assertEquals("Updated presentation", productEntity.getPresentation());
        assertEquals("NEW-ENT", productEntity.getEnterpriseId());
        assertTrue(productEntity.isState());
    }

    @Test
    @DisplayName("Should maintain data integrity after multiple operations")
    void testDataIntegrityAfterMultipleOperations() {
        // Arrange & Act
        productEntity.setProductId(999L);
        Long firstProductId = productEntity.getProductId();
        
        productEntity.setName("Test Product");
        productEntity.setReference("TEST-001");
        
        Long secondProductId = productEntity.getProductId();

        // Assert - ProductId should remain unchanged
        assertEquals(firstProductId, secondProductId);
        assertEquals(999L, productEntity.getProductId());
        assertEquals("Test Product", productEntity.getName());
        assertEquals("TEST-001", productEntity.getReference());
    }

    // ==================== Business Logic Tests ====================

    @Test
    @DisplayName("Should represent active product")
    void testActiveProduct() {
        // Arrange & Act
        productEntity.setProductId(1L);
        productEntity.setName("Active Product");
        productEntity.setReference("ACT-001");
        productEntity.setPresentation("Box");
        productEntity.setEnterpriseId("ENT-001");
        productEntity.setState(true);

        // Assert
        assertTrue(productEntity.isState());
        assertNotNull(productEntity.getProductId());
        assertNotNull(productEntity.getName());
    }

    @Test
    @DisplayName("Should represent inactive product")
    void testInactiveProduct() {
        // Arrange & Act
        productEntity.setProductId(2L);
        productEntity.setName("Inactive Product");
        productEntity.setReference("INA-001");
        productEntity.setPresentation("Unit");
        productEntity.setEnterpriseId("ENT-002");
        productEntity.setState(false);

        // Assert
        assertFalse(productEntity.isState());
        assertNotNull(productEntity.getProductId());
    }

    @Test
    @DisplayName("Should handle product from same enterprise")
    void testProductsFromSameEnterprise() {
        // Arrange
        ProductEntity product1 = new ProductEntity();
        ProductEntity product2 = new ProductEntity();
        String sharedEnterpriseId = "ENT-SHARED";

        // Act
        product1.setEnterpriseId(sharedEnterpriseId);
        product1.setProductId(1L);
        
        product2.setEnterpriseId(sharedEnterpriseId);
        product2.setProductId(2L);

        // Assert
        assertEquals(product1.getEnterpriseId(), product2.getEnterpriseId());
        assertNotEquals(product1.getProductId(), product2.getProductId());
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle product with same reference different enterprises")
    void testSameReferenceDifferentEnterprises() {
        // Arrange
        ProductEntity product1 = new ProductEntity();
        ProductEntity product2 = new ProductEntity();
        String sharedReference = "REF-001";

        // Act
        product1.setReference(sharedReference);
        product1.setEnterpriseId("ENT-001");
        
        product2.setReference(sharedReference);
        product2.setEnterpriseId("ENT-002");

        // Assert
        assertEquals(product1.getReference(), product2.getReference());
        assertNotEquals(product1.getEnterpriseId(), product2.getEnterpriseId());
    }

    @Test
    @DisplayName("Should handle whitespace in text fields")
    void testWhitespaceInFields() {
        // Act
        productEntity.setName("  Product With Spaces  ");
        productEntity.setReference("  REF-001  ");
        productEntity.setPresentation("  Box  ");

        // Assert
        assertEquals("  Product With Spaces  ", productEntity.getName());
        assertEquals("  REF-001  ", productEntity.getReference());
        assertEquals("  Box  ", productEntity.getPresentation());
    }

    @Test
    @DisplayName("Should handle product state transitions")
    void testProductStateTransitions() {
        // Arrange
        productEntity.setProductId(1L);
        productEntity.setState(true);

        // Act - Deactivate
        assertTrue(productEntity.isState());
        productEntity.setState(false);
        assertFalse(productEntity.isState());

        // Act - Reactivate
        productEntity.setState(true);
        assertTrue(productEntity.isState());
    }

    @Test
    @DisplayName("Should handle adding and removing kardex records")
    void testAddingAndRemovingKardexRecords() {
        // Arrange
        List<KardexEntity> kardexList = new ArrayList<>();
        KardexEntity kardex1 = new KardexEntity();
        kardex1.setIdKardex(1L);
        kardexList.add(kardex1);

        // Act - Add records
        productEntity.setRecordsKardex(kardexList);
        assertEquals(1, productEntity.getRecordsKardex().size());

        // Act - Add more records
        KardexEntity kardex2 = new KardexEntity();
        kardex2.setIdKardex(2L);
        productEntity.getRecordsKardex().add(kardex2);
        assertEquals(2, productEntity.getRecordsKardex().size());

        // Act - Remove record
        productEntity.getRecordsKardex().remove(0);
        assertEquals(1, productEntity.getRecordsKardex().size());
    }
}
