package co.unicauca.edu.co.contables.inventory.kardex.unit.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;

public class ProductUnitTest {
    
    private Product product;
    
    @BeforeEach
    void setUp() {
        product = Product.builder()
                .productId(1L)
                .reference("REF001")
                .name("Test Product")
                .presentation("Box")
                .enterpriseId("ENT001")
                .state(true)
                .build();
    }
    
    // ========== validateRequiredFields Tests ==========
    
    @Test
    @DisplayName("Should validate successfully when all required fields are present")
    void testValidateRequiredFieldsSuccess() {
        // Act & Assert
        assertDoesNotThrow(() -> product.validateRequiredFields());
    }
    
    @Test
    @DisplayName("Should throw exception when productId is null")
    void testValidateRequiredFieldsWithNullProductId() {
        // Arrange
        product.setProductId(null);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> product.validateRequiredFields()
        );
        assertEquals("Product ID is required", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when name is null")
    void testValidateRequiredFieldsWithNullName() {
        // Arrange
        product.setName(null);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> product.validateRequiredFields()
        );
        assertEquals("Product name is required and cannot be empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when name is empty")
    void testValidateRequiredFieldsWithEmptyName() {
        // Arrange
        product.setName("");
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> product.validateRequiredFields()
        );
        assertEquals("Product name is required and cannot be empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when name contains only whitespace")
    void testValidateRequiredFieldsWithWhitespaceName() {
        // Arrange
        product.setName("   ");
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> product.validateRequiredFields()
        );
        assertEquals("Product name is required and cannot be empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when reference is null")
    void testValidateRequiredFieldsWithNullReference() {
        // Arrange
        product.setReference(null);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> product.validateRequiredFields()
        );
        assertEquals("Product reference is required and cannot be empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when reference is empty")
    void testValidateRequiredFieldsWithEmptyReference() {
        // Arrange
        product.setReference("");
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> product.validateRequiredFields()
        );
        assertEquals("Product reference is required and cannot be empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when reference contains only whitespace")
    void testValidateRequiredFieldsWithWhitespaceReference() {
        // Arrange
        product.setReference("   ");
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> product.validateRequiredFields()
        );
        assertEquals("Product reference is required and cannot be empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when enterpriseId is null")
    void testValidateRequiredFieldsWithNullEnterpriseId() {
        // Arrange
        product.setEnterpriseId(null);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> product.validateRequiredFields()
        );
        assertEquals("Enterprise ID is required and cannot be empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when enterpriseId is empty")
    void testValidateRequiredFieldsWithEmptyEnterpriseId() {
        // Arrange
        product.setEnterpriseId("");
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> product.validateRequiredFields()
        );
        assertEquals("Enterprise ID is required and cannot be empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when enterpriseId contains only whitespace")
    void testValidateRequiredFieldsWithWhitespaceEnterpriseId() {
        // Arrange
        product.setEnterpriseId("   ");
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> product.validateRequiredFields()
        );
        assertEquals("Enterprise ID is required and cannot be empty", exception.getMessage());
    }
    
    // ========== isValid Tests ==========
    
    @Test
    @DisplayName("Should return true when all required fields are valid")
    void testIsValidWithAllFields() {
        // Act & Assert
        assertTrue(product.isValid());
    }
    
    @Test
    @DisplayName("Should return false when productId is null")
    void testIsValidWithNullProductId() {
        // Arrange
        product.setProductId(null);
        
        // Act & Assert
        assertFalse(product.isValid());
    }
    
    @Test
    @DisplayName("Should return false when name is null")
    void testIsValidWithNullName() {
        // Arrange
        product.setName(null);
        
        // Act & Assert
        assertFalse(product.isValid());
    }
    
    @Test
    @DisplayName("Should return false when name is empty")
    void testIsValidWithEmptyName() {
        // Arrange
        product.setName("");
        
        // Act & Assert
        assertFalse(product.isValid());
    }
    
    @Test
    @DisplayName("Should return false when reference is null")
    void testIsValidWithNullReference() {
        // Arrange
        product.setReference(null);
        
        // Act & Assert
        assertFalse(product.isValid());
    }
    
    @Test
    @DisplayName("Should return false when reference is empty")
    void testIsValidWithEmptyReference() {
        // Arrange
        product.setReference("");
        
        // Act & Assert
        assertFalse(product.isValid());
    }
    
    @Test
    @DisplayName("Should return false when enterpriseId is null")
    void testIsValidWithNullEnterpriseId() {
        // Arrange
        product.setEnterpriseId(null);
        
        // Act & Assert
        assertFalse(product.isValid());
    }
    
    @Test
    @DisplayName("Should return false when enterpriseId is empty")
    void testIsValidWithEmptyEnterpriseId() {
        // Arrange
        product.setEnterpriseId("");
        
        // Act & Assert
        assertFalse(product.isValid());
    }
    
    // ========== State Management Tests ==========
    
    @Test
    @DisplayName("Should activate product by setting state to true")
    void testActivate() {
        // Arrange
        product.setState(false);
        
        // Act
        product.activate();
        
        // Assert
        assertTrue(product.isState());
        assertTrue(product.isActive());
    }
    
    @Test
    @DisplayName("Should deactivate product by setting state to false")
    void testDeactivate() {
        // Arrange
        product.setState(true);
        
        // Act
        product.deactivate();
        
        // Assert
        assertFalse(product.isState());
        assertFalse(product.isActive());
    }
    
    @Test
    @DisplayName("Should return true when product is active")
    void testIsActive() {
        // Arrange
        product.setState(true);
        
        // Act & Assert
        assertTrue(product.isActive());
    }
    
    @Test
    @DisplayName("Should return false when product is inactive")
    void testIsNotActive() {
        // Arrange
        product.setState(false);
        
        // Act & Assert
        assertFalse(product.isActive());
    }
    
    // ========== normalize Tests ==========
    
    @Test
    @DisplayName("Should trim and uppercase reference during normalization")
    void testNormalizeReference() {
        // Arrange
        product.setReference("  ref123  ");
        
        // Act
        product.normalize();
        
        // Assert
        assertEquals("REF123", product.getReference());
    }
    
    @Test
    @DisplayName("Should trim name during normalization")
    void testNormalizeName() {
        // Arrange
        product.setName("  Product Name  ");
        
        // Act
        product.normalize();
        
        // Assert
        assertEquals("Product Name", product.getName());
    }
    
    @Test
    @DisplayName("Should trim presentation during normalization")
    void testNormalizePresentation() {
        // Arrange
        product.setPresentation("  Box of 12  ");
        
        // Act
        product.normalize();
        
        // Assert
        assertEquals("Box of 12", product.getPresentation());
    }
    
    @Test
    @DisplayName("Should trim enterpriseId during normalization")
    void testNormalizeEnterpriseId() {
        // Arrange
        product.setEnterpriseId("  ENT001  ");
        
        // Act
        product.normalize();
        
        // Assert
        assertEquals("ENT001", product.getEnterpriseId());
    }
    
    @Test
    @DisplayName("Should handle null fields during normalization")
    void testNormalizeWithNullFields() {
        // Arrange
        product.setName(null);
        product.setReference(null);
        product.setPresentation(null);
        product.setEnterpriseId(null);
        
        // Act & Assert
        assertDoesNotThrow(() -> product.normalize());
        assertNull(product.getName());
        assertNull(product.getReference());
        assertNull(product.getPresentation());
        assertNull(product.getEnterpriseId());
    }
    
    @Test
    @DisplayName("Should normalize all fields together")
    void testNormalizeAllFields() {
        // Arrange
        product.setName("  Product  ");
        product.setReference("  ref456  ");
        product.setPresentation("  Unit  ");
        product.setEnterpriseId("  ENT999  ");
        
        // Act
        product.normalize();
        
        // Assert
        assertEquals("Product", product.getName());
        assertEquals("REF456", product.getReference());
        assertEquals("Unit", product.getPresentation());
        assertEquals("ENT999", product.getEnterpriseId());
    }
    
    // ========== belongsToEnterprise Tests ==========
    
    @Test
    @DisplayName("Should return true when product belongs to the enterprise")
    void testBelongsToEnterpriseTrue() {
        // Arrange
        product.setEnterpriseId("ENT001");
        
        // Act & Assert
        assertTrue(product.belongsToEnterprise("ENT001"));
    }
    
    @Test
    @DisplayName("Should return false when product does not belong to the enterprise")
    void testBelongsToEnterpriseFalse() {
        // Arrange
        product.setEnterpriseId("ENT001");
        
        // Act & Assert
        assertFalse(product.belongsToEnterprise("ENT999"));
    }
    
    @Test
    @DisplayName("Should return false when enterpriseId is null")
    void testBelongsToEnterpriseWithNullEnterpriseId() {
        // Arrange
        product.setEnterpriseId(null);
        
        // Act & Assert
        assertFalse(product.belongsToEnterprise("ENT001"));
    }
    
    @Test
    @DisplayName("Should return false when target enterpriseId is null")
    void testBelongsToEnterpriseWithNullTarget() {
        // Arrange
        product.setEnterpriseId("ENT001");
        
        // Act & Assert
        assertFalse(product.belongsToEnterprise(null));
    }
    
    // ========== prepareForPersistence Tests ==========
    
    @Test
    @DisplayName("Should normalize and validate on prepareForPersistence")
    void testPrepareForPersistence() {
        // Arrange
        product.setName("  Product Name  ");
        product.setReference("  ref789  ");
        product.setEnterpriseId("  ENT001  ");
        
        // Act
        product.prepareForPersistence();
        
        // Assert
        assertEquals("Product Name", product.getName());
        assertEquals("REF789", product.getReference());
        assertEquals("ENT001", product.getEnterpriseId());
    }
    
    @Test
    @DisplayName("Should throw exception on prepareForPersistence when validation fails")
    void testPrepareForPersistenceWithInvalidData() {
        // Arrange
        product.setProductId(null);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> product.prepareForPersistence());
    }
    
    @Test
    @DisplayName("Should complete prepareForPersistence with all operations")
    void testPrepareForPersistenceComplete() {
        // Arrange
        Product newProduct = Product.builder()
                .productId(100L)
                .reference("  abc123  ")
                .name("  Test Product  ")
                .presentation("  Pack  ")
                .enterpriseId("  ENT555  ")
                .state(false)
                .build();
        
        // Act
        newProduct.prepareForPersistence();
        
        // Assert - Normalized
        assertEquals("ABC123", newProduct.getReference());
        assertEquals("Test Product", newProduct.getName());
        assertEquals("Pack", newProduct.getPresentation());
        assertEquals("ENT555", newProduct.getEnterpriseId());
        
        // Assert - Still valid after normalization
        assertTrue(newProduct.isValid());
    }
}
