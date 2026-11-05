package co.unicauca.edu.co.contables.inventory.kardex.unit.infrastructure.adapters.output.jpa.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.MessageProcessingErrorEntity;

/**
 * @brief Unit tests for MessageProcessingErrorEntity
 * 
 * Tests the entity behavior, lifecycle callbacks, constructors, and business logic.
 */
@DisplayName("MessageProcessingErrorEntity Tests")
class MessageProcessingErrorEntityUnitTest {

    private MessageProcessingErrorEntity errorEntity;

    @BeforeEach
    void setUp() {
        errorEntity = new MessageProcessingErrorEntity();
    }

    @Test
    @DisplayName("Should create entity with no-args constructor")
    void testNoArgsConstructor() {
        // Act
        MessageProcessingErrorEntity entity = new MessageProcessingErrorEntity();

        // Assert
        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getEventType());
        assertNull(entity.getErrorDescription());
        assertNull(entity.getMessageData());
        assertNull(entity.getErrorTimestamp());
        assertNull(entity.getEntityType());
        assertNull(entity.getTenantId());
    }

    @Test
    @DisplayName("Should create entity with all-args constructor")
    void testAllArgsConstructor() {
        // Arrange
        Long id = 1L;
        String eventType = "PRODUCT_CREATED";
        String errorDescription = "Database connection failed";
        String messageData = "{\"id\":123}";
        Instant errorTimestamp = Instant.now();
        String entityType = "Product";
        String tenantId = "TENANT-001";

        // Act
        MessageProcessingErrorEntity entity = new MessageProcessingErrorEntity(
            id, eventType, errorDescription, messageData, errorTimestamp, entityType, tenantId
        );

        // Assert
        assertEquals(id, entity.getId());
        assertEquals(eventType, entity.getEventType());
        assertEquals(errorDescription, entity.getErrorDescription());
        assertEquals(messageData, entity.getMessageData());
        assertEquals(errorTimestamp, entity.getErrorTimestamp());
        assertEquals(entityType, entity.getEntityType());
        assertEquals(tenantId, entity.getTenantId());
    }

    @Test
    @DisplayName("Should set and get id correctly")
    void testSetAndGetId() {
        // Arrange
        Long expectedId = 42L;

        // Act
        errorEntity.setId(expectedId);

        // Assert
        assertEquals(expectedId, errorEntity.getId());
    }

    @Test
    @DisplayName("Should set and get eventType correctly")
    void testSetAndGetEventType() {
        // Arrange
        String expectedEventType = "INVENTORY_UPDATED";

        // Act
        errorEntity.setEventType(expectedEventType);

        // Assert
        assertEquals(expectedEventType, errorEntity.getEventType());
    }

    @Test
    @DisplayName("Should set and get errorDescription correctly")
    void testSetAndGetErrorDescription() {
        // Arrange
        String expectedDescription = "Failed to process message due to invalid format";

        // Act
        errorEntity.setErrorDescription(expectedDescription);

        // Assert
        assertEquals(expectedDescription, errorEntity.getErrorDescription());
    }

    @Test
    @DisplayName("Should set and get messageData correctly")
    void testSetAndGetMessageData() {
        // Arrange
        String expectedMessageData = "{\"product_id\":\"P123\",\"quantity\":50}";

        // Act
        errorEntity.setMessageData(expectedMessageData);

        // Assert
        assertEquals(expectedMessageData, errorEntity.getMessageData());
    }

    @Test
    @DisplayName("Should set and get errorTimestamp correctly")
    void testSetAndGetErrorTimestamp() {
        // Arrange
        Instant expectedTimestamp = Instant.now();

        // Act
        errorEntity.setErrorTimestamp(expectedTimestamp);

        // Assert
        assertEquals(expectedTimestamp, errorEntity.getErrorTimestamp());
    }

    @Test
    @DisplayName("Should set and get entityType correctly")
    void testSetAndGetEntityType() {
        // Arrange
        String expectedEntityType = "Kardex";

        // Act
        errorEntity.setEntityType(expectedEntityType);

        // Assert
        assertEquals(expectedEntityType, errorEntity.getEntityType());
    }

    @Test
    @DisplayName("Should set and get tenantId correctly")
    void testSetAndGetTenantId() {
        // Arrange
        String expectedTenantId = "TENANT-999";

        // Act
        errorEntity.setTenantId(expectedTenantId);

        // Assert
        assertEquals(expectedTenantId, errorEntity.getTenantId());
    }

    @Test
    @DisplayName("Should set errorTimestamp on onCreate when null")
    void testOnCreate_SetsTimestampWhenNull() throws Exception {
        // Arrange
        Instant beforeCreate = Instant.now();
        Method onCreateMethod = MessageProcessingErrorEntity.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);
        
        // Act
        onCreateMethod.invoke(errorEntity);
        Instant afterCreate = Instant.now();

        // Assert
        assertNotNull(errorEntity.getErrorTimestamp());
        assertTrue(errorEntity.getErrorTimestamp().isAfter(beforeCreate.minusSeconds(1)));
        assertTrue(errorEntity.getErrorTimestamp().isBefore(afterCreate.plusSeconds(1)));
    }

    @Test
    @DisplayName("Should not override errorTimestamp on onCreate when already set")
    void testOnCreate_DoesNotOverrideExistingTimestamp() throws Exception {
        // Arrange
        Instant existingTimestamp = Instant.parse("2025-01-01T10:00:00Z");
        errorEntity.setErrorTimestamp(existingTimestamp);
        
        Method onCreateMethod = MessageProcessingErrorEntity.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);

        // Act
        onCreateMethod.invoke(errorEntity);

        // Assert
        assertEquals(existingTimestamp, errorEntity.getErrorTimestamp());
    }

    @Test
    @DisplayName("Should handle null values for optional fields")
    void testNullValues_ForOptionalFields() {
        // Act
        errorEntity.setId(null);
        errorEntity.setErrorDescription(null);
        errorEntity.setMessageData(null);
        errorEntity.setTenantId(null);

        // Assert
        assertNull(errorEntity.getId());
        assertNull(errorEntity.getErrorDescription());
        assertNull(errorEntity.getMessageData());
        assertNull(errorEntity.getTenantId());
    }

    @Test
    @DisplayName("Should handle empty strings correctly")
    void testSetStrings_EmptyValues() {
        // Act
        errorEntity.setEventType("");
        errorEntity.setErrorDescription("");
        errorEntity.setMessageData("");
        errorEntity.setEntityType("");
        errorEntity.setTenantId("");

        // Assert
        assertEquals("", errorEntity.getEventType());
        assertEquals("", errorEntity.getErrorDescription());
        assertEquals("", errorEntity.getMessageData());
        assertEquals("", errorEntity.getEntityType());
        assertEquals("", errorEntity.getTenantId());
    }

    @Test
    @DisplayName("Should handle very long text values")
    void testSetText_VeryLongValues() {
        // Arrange
        String longText = "A".repeat(5000);

        // Act
        errorEntity.setErrorDescription(longText);
        errorEntity.setMessageData(longText);

        // Assert
        assertEquals(longText, errorEntity.getErrorDescription());
        assertEquals(longText, errorEntity.getMessageData());
    }

    @Test
    @DisplayName("Should handle edge case with minimum Long value")
    void testSetId_MinimumLongValue() {
        // Arrange
        Long minLongValue = Long.MIN_VALUE;

        // Act
        errorEntity.setId(minLongValue);

        // Assert
        assertEquals(minLongValue, errorEntity.getId());
    }

    @Test
    @DisplayName("Should handle edge case with maximum Long value")
    void testSetId_MaximumLongValue() {
        // Arrange
        Long maxLongValue = Long.MAX_VALUE;

        // Act
        errorEntity.setId(maxLongValue);

        // Assert
        assertEquals(maxLongValue, errorEntity.getId());
    }

    @Test
    @DisplayName("Should handle past Instant values")
    void testSetErrorTimestamp_PastDate() {
        // Arrange
        Instant pastDate = Instant.parse("2020-01-01T00:00:00Z");

        // Act
        errorEntity.setErrorTimestamp(pastDate);

        // Assert
        assertEquals(pastDate, errorEntity.getErrorTimestamp());
        assertTrue(errorEntity.getErrorTimestamp().isBefore(Instant.now()));
    }

    @Test
    @DisplayName("Should handle future Instant values")
    void testSetErrorTimestamp_FutureDate() {
        // Arrange
        Instant futureDate = Instant.parse("2030-12-31T23:59:59Z");

        // Act
        errorEntity.setErrorTimestamp(futureDate);

        // Assert
        assertEquals(futureDate, errorEntity.getErrorTimestamp());
        assertTrue(errorEntity.getErrorTimestamp().isAfter(Instant.now()));
    }

    @Test
    @DisplayName("Should maintain entity state across property changes")
    void testEntityState_PersistsAcrossChanges() throws Exception {
        // Arrange
        Method onCreateMethod = MessageProcessingErrorEntity.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);

        // Act
        errorEntity.setId(100L);
        errorEntity.setEventType("ORDER_FAILED");
        errorEntity.setErrorDescription("Network timeout");
        errorEntity.setMessageData("{\"order_id\":\"ORD-001\"}");
        errorEntity.setEntityType("Order");
        errorEntity.setTenantId("TENANT-100");
        onCreateMethod.invoke(errorEntity);

        // Assert
        assertEquals(100L, errorEntity.getId());
        assertEquals("ORDER_FAILED", errorEntity.getEventType());
        assertEquals("Network timeout", errorEntity.getErrorDescription());
        assertEquals("{\"order_id\":\"ORD-001\"}", errorEntity.getMessageData());
        assertEquals("Order", errorEntity.getEntityType());
        assertEquals("TENANT-100", errorEntity.getTenantId());
        assertNotNull(errorEntity.getErrorTimestamp());
    }

    @Test
    @DisplayName("Should handle JSON-like messageData")
    void testSetMessageData_JsonFormat() {
        // Arrange
        String jsonData = "{\"event\":\"PRODUCT_CREATED\",\"productId\":123,\"name\":\"Test Product\",\"price\":99.99}";

        // Act
        errorEntity.setMessageData(jsonData);

        // Assert
        assertEquals(jsonData, errorEntity.getMessageData());
    }

    @Test
    @DisplayName("Should handle special characters in text fields")
    void testSetText_SpecialCharacters() {
        // Arrange
        String specialCharsDescription = "Error: \"Connection failed\" at line #42 with 100% certainty!";
        String specialCharsData = "{\"key\":\"value with 'quotes' and \\\"escapes\\\"\"}";

        // Act
        errorEntity.setErrorDescription(specialCharsDescription);
        errorEntity.setMessageData(specialCharsData);

        // Assert
        assertEquals(specialCharsDescription, errorEntity.getErrorDescription());
        assertEquals(specialCharsData, errorEntity.getMessageData());
    }

    @Test
    @DisplayName("Should handle unicode characters")
    void testSetText_UnicodeCharacters() {
        // Arrange
        String unicodeText = "Error en español: ñáéíóú 中文 日本語 emoji: 🚀💻";

        // Act
        errorEntity.setErrorDescription(unicodeText);
        errorEntity.setEventType(unicodeText);

        // Assert
        assertEquals(unicodeText, errorEntity.getErrorDescription());
        assertEquals(unicodeText, errorEntity.getEventType());
    }

    @Test
    @DisplayName("Should handle multiple onCreate calls")
    void testOnCreate_MultipleCalls() throws Exception {
        // Arrange
        Method onCreateMethod = MessageProcessingErrorEntity.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);

        // Act
        onCreateMethod.invoke(errorEntity);
        Instant firstTimestamp = errorEntity.getErrorTimestamp();
        
        Thread.sleep(10);
        
        errorEntity.setErrorTimestamp(null); // Reset to test onCreate again
        onCreateMethod.invoke(errorEntity);
        Instant secondTimestamp = errorEntity.getErrorTimestamp();

        // Assert
        assertNotNull(firstTimestamp);
        assertNotNull(secondTimestamp);
        assertTrue(secondTimestamp.isAfter(firstTimestamp));
    }

    @Test
    @DisplayName("Should create valid entity with all required fields")
    void testCreateCompleteEntity() {
        // Act
        errorEntity.setEventType("SYNC_ERROR");
        errorEntity.setEntityType("Inventory");
        errorEntity.setErrorTimestamp(Instant.now());
        errorEntity.setErrorDescription("Synchronization failed");
        errorEntity.setMessageData("{\"sync_id\":123}");
        errorEntity.setTenantId("TENANT-SYNC");

        // Assert
        assertNotNull(errorEntity.getEventType());
        assertNotNull(errorEntity.getEntityType());
        assertNotNull(errorEntity.getErrorTimestamp());
        assertNotNull(errorEntity.getErrorDescription());
        assertNotNull(errorEntity.getMessageData());
        assertNotNull(errorEntity.getTenantId());
    }

    @Test
    @DisplayName("Should handle all-args constructor with null timestamp")
    void testAllArgsConstructor_WithNullTimestamp() throws Exception {
        // Arrange
        Method onCreateMethod = MessageProcessingErrorEntity.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);

        // Act
        MessageProcessingErrorEntity entity = new MessageProcessingErrorEntity(
            null, "EVENT_TYPE", "Description", "Data", null, "EntityType", "TENANT-001"
        );
        onCreateMethod.invoke(entity);

        // Assert
        assertNotNull(entity.getErrorTimestamp());
    }
}
