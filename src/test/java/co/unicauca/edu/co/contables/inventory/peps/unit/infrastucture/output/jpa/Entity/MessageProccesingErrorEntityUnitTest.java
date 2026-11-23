package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.jpa.Entity;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.MessageProcessingErrorEntity;

/**
 * @brief Unit tests for MessageProcessingErrorEntity
 * 
 * Tests JPA entity for tracking RabbitMQ message processing errors
 * including field validation, getters/setters, and @PrePersist lifecycle.
 */
@DisplayName("MessageProcessingErrorEntity Unit Tests")
class MessageProccesingErrorEntityUnitTest {
    
    private MessageProcessingErrorEntity errorEntity;
    private Instant testTimestamp;

    @BeforeEach
    void setUp() {
        errorEntity = new MessageProcessingErrorEntity();
        testTimestamp = Instant.now();
    }

    /**
     * Helper method to invoke protected onCreate() method using reflection
     */
    private void invokeOnCreate(MessageProcessingErrorEntity entity) throws Exception {
        Method onCreateMethod = MessageProcessingErrorEntity.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);
        onCreateMethod.invoke(entity);
    }

    // ==================== Constructor Tests ====================

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
    }

    @Test
    @DisplayName("Should create entity with all-args constructor")
    void testAllArgsConstructor() {
        // Arrange
        Long id = 1L;
        String eventType = "CREATED";
        String errorDescription = "Validation error";
        String messageData = "{\"id\":1}";
        Instant timestamp = Instant.now();
        String entityType = "Product";
        String tenantId = "tenant-001";

        // Act
        MessageProcessingErrorEntity entity = new MessageProcessingErrorEntity(
            id, eventType, errorDescription, messageData, timestamp, entityType, tenantId
        );

        // Assert
        assertEquals(id, entity.getId());
        assertEquals(eventType, entity.getEventType());
        assertEquals(errorDescription, entity.getErrorDescription());
        assertEquals(messageData, entity.getMessageData());
        assertEquals(timestamp, entity.getErrorTimestamp());
        assertEquals(entityType, entity.getEntityType());
        assertEquals(tenantId, entity.getTenantId());
    }

    // ==================== ID Tests ====================

    @Test
    @DisplayName("Should set and get id correctly")
    void testIdGetterSetter() {
        // Arrange
        Long expectedId = 123L;

        // Act
        errorEntity.setId(expectedId);

        // Assert
        assertEquals(expectedId, errorEntity.getId());
    }

    @Test
    @DisplayName("Should handle null id")
    void testNullId() {
        // Act
        errorEntity.setId(null);

        // Assert
        assertNull(errorEntity.getId());
    }

    @Test
    @DisplayName("Should handle large id values")
    void testLargeId() {
        // Arrange
        Long largeId = Long.MAX_VALUE;

        // Act
        errorEntity.setId(largeId);

        // Assert
        assertEquals(largeId, errorEntity.getId());
    }

    // ==================== Event Type Tests ====================

    @Test
    @DisplayName("Should set and get eventType correctly")
    void testEventTypeGetterSetter() {
        // Arrange
        String expectedEventType = "UPDATED";

        // Act
        errorEntity.setEventType(expectedEventType);

        // Assert
        assertEquals(expectedEventType, errorEntity.getEventType());
    }

    @Test
    @DisplayName("Should handle null eventType")
    void testNullEventType() {
        // Act
        errorEntity.setEventType(null);

        // Assert
        assertNull(errorEntity.getEventType());
    }

    @Test
    @DisplayName("Should handle empty eventType")
    void testEmptyEventType() {
        // Act
        errorEntity.setEventType("");

        // Assert
        assertEquals("", errorEntity.getEventType());
    }

    @Test
    @DisplayName("Should handle different event types")
    void testDifferentEventTypes() {
        // Test CREATED
        errorEntity.setEventType("CREATED");
        assertEquals("CREATED", errorEntity.getEventType());

        // Test DELETED
        errorEntity.setEventType("DELETED");
        assertEquals("DELETED", errorEntity.getEventType());

        // Test UPDATED
        errorEntity.setEventType("UPDATED");
        assertEquals("UPDATED", errorEntity.getEventType());
    }

    // ==================== Error Description Tests ====================

    @Test
    @DisplayName("Should set and get errorDescription correctly")
    void testErrorDescriptionGetterSetter() {
        // Arrange
        String expectedDescription = "Database connection failed";

        // Act
        errorEntity.setErrorDescription(expectedDescription);

        // Assert
        assertEquals(expectedDescription, errorEntity.getErrorDescription());
    }

    @Test
    @DisplayName("Should handle null errorDescription")
    void testNullErrorDescription() {
        // Act
        errorEntity.setErrorDescription(null);

        // Assert
        assertNull(errorEntity.getErrorDescription());
    }

    @Test
    @DisplayName("Should handle empty errorDescription")
    void testEmptyErrorDescription() {
        // Act
        errorEntity.setErrorDescription("");

        // Assert
        assertEquals("", errorEntity.getErrorDescription());
    }

    @Test
    @DisplayName("Should handle long errorDescription text")
    void testLongErrorDescription() {
        // Arrange
        String longDescription = "Error: ".repeat(100);

        // Act
        errorEntity.setErrorDescription(longDescription);

        // Assert
        assertEquals(longDescription, errorEntity.getErrorDescription());
        assertTrue(errorEntity.getErrorDescription().length() > 500);
    }

    @Test
    @DisplayName("Should handle errorDescription with special characters")
    void testErrorDescriptionWithSpecialCharacters() {
        // Arrange
        String specialChars = "Error: \"Invalid JSON\" - can't parse {}[]";

        // Act
        errorEntity.setErrorDescription(specialChars);

        // Assert
        assertEquals(specialChars, errorEntity.getErrorDescription());
    }

    @Test
    @DisplayName("Should handle errorDescription with line breaks")
    void testErrorDescriptionWithLineBreaks() {
        // Arrange
        String multilineDescription = "Error occurred:\nLine 1\nLine 2\nLine 3";

        // Act
        errorEntity.setErrorDescription(multilineDescription);

        // Assert
        assertEquals(multilineDescription, errorEntity.getErrorDescription());
        assertTrue(errorEntity.getErrorDescription().contains("\n"));
    }

    // ==================== Message Data Tests ====================

    @Test
    @DisplayName("Should set and get messageData correctly")
    void testMessageDataGetterSetter() {
        // Arrange
        String expectedMessageData = "{\"productId\":1,\"name\":\"Product A\"}";

        // Act
        errorEntity.setMessageData(expectedMessageData);

        // Assert
        assertEquals(expectedMessageData, errorEntity.getMessageData());
    }

    @Test
    @DisplayName("Should handle null messageData")
    void testNullMessageData() {
        // Act
        errorEntity.setMessageData(null);

        // Assert
        assertNull(errorEntity.getMessageData());
    }

    @Test
    @DisplayName("Should handle empty messageData")
    void testEmptyMessageData() {
        // Act
        errorEntity.setMessageData("");

        // Assert
        assertEquals("", errorEntity.getMessageData());
    }

    @Test
    @DisplayName("Should handle JSON messageData")
    void testJsonMessageData() {
        // Arrange
        String jsonData = "{\"id\":1,\"name\":\"Test\",\"data\":{\"nested\":true}}";

        // Act
        errorEntity.setMessageData(jsonData);

        // Assert
        assertEquals(jsonData, errorEntity.getMessageData());
        assertTrue(errorEntity.getMessageData().startsWith("{"));
        assertTrue(errorEntity.getMessageData().endsWith("}"));
    }

    @Test
    @DisplayName("Should handle large messageData")
    void testLargeMessageData() {
        // Arrange
        String largeData = "{\"data\":\"" + "x".repeat(5000) + "\"}";

        // Act
        errorEntity.setMessageData(largeData);

        // Assert
        assertEquals(largeData, errorEntity.getMessageData());
        assertTrue(errorEntity.getMessageData().length() > 5000);
    }

    @Test
    @DisplayName("Should handle messageData with special characters")
    void testMessageDataWithSpecialCharacters() {
        // Arrange
        String specialData = "{\"text\":\"Contains \\\"quotes\\\", \\n newlines\"}";

        // Act
        errorEntity.setMessageData(specialData);

        // Assert
        assertEquals(specialData, errorEntity.getMessageData());
    }

    // ==================== Error Timestamp Tests ====================

    @Test
    @DisplayName("Should set and get errorTimestamp correctly")
    void testErrorTimestampGetterSetter() {
        // Act
        errorEntity.setErrorTimestamp(testTimestamp);

        // Assert
        assertEquals(testTimestamp, errorEntity.getErrorTimestamp());
    }

    @Test
    @DisplayName("Should handle null errorTimestamp")
    void testNullErrorTimestamp() {
        // Act
        errorEntity.setErrorTimestamp(null);

        // Assert
        assertNull(errorEntity.getErrorTimestamp());
    }

    @Test
    @DisplayName("Should handle past timestamp")
    void testPastTimestamp() {
        // Arrange
        Instant pastTimestamp = Instant.now().minus(30, ChronoUnit.DAYS);

        // Act
        errorEntity.setErrorTimestamp(pastTimestamp);

        // Assert
        assertEquals(pastTimestamp, errorEntity.getErrorTimestamp());
        assertTrue(errorEntity.getErrorTimestamp().isBefore(Instant.now()));
    }

    @Test
    @DisplayName("Should handle very old timestamp")
    void testVeryOldTimestamp() {
        // Arrange
        Instant oldTimestamp = Instant.parse("2020-01-01T00:00:00Z");

        // Act
        errorEntity.setErrorTimestamp(oldTimestamp);

        // Assert
        assertEquals(oldTimestamp, errorEntity.getErrorTimestamp());
    }

    @Test
    @DisplayName("Should handle timestamp with nanosecond precision")
    void testTimestampWithNanoseconds() {
        // Arrange
        Instant preciseTimestamp = Instant.now();

        // Act
        errorEntity.setErrorTimestamp(preciseTimestamp);

        // Assert
        assertEquals(preciseTimestamp, errorEntity.getErrorTimestamp());
        assertEquals(preciseTimestamp.getNano(), errorEntity.getErrorTimestamp().getNano());
    }

    // ==================== Entity Type Tests ====================

    @Test
    @DisplayName("Should set and get entityType correctly")
    void testEntityTypeGetterSetter() {
        // Arrange
        String expectedEntityType = "Product";

        // Act
        errorEntity.setEntityType(expectedEntityType);

        // Assert
        assertEquals(expectedEntityType, errorEntity.getEntityType());
    }

    @Test
    @DisplayName("Should handle null entityType")
    void testNullEntityType() {
        // Act
        errorEntity.setEntityType(null);

        // Assert
        assertNull(errorEntity.getEntityType());
    }

    @Test
    @DisplayName("Should handle different entity types")
    void testDifferentEntityTypes() {
        // Test Product
        errorEntity.setEntityType("Product");
        assertEquals("Product", errorEntity.getEntityType());

        // Test Kardex
        errorEntity.setEntityType("Kardex");
        assertEquals("Kardex", errorEntity.getEntityType());

        // Test DetailOutput
        errorEntity.setEntityType("DetailOutput");
        assertEquals("DetailOutput", errorEntity.getEntityType());
    }

    @Test
    @DisplayName("Should handle empty entityType")
    void testEmptyEntityType() {
        // Act
        errorEntity.setEntityType("");

        // Assert
        assertEquals("", errorEntity.getEntityType());
    }

    // ==================== @PrePersist Tests ====================

    @Test
    @DisplayName("onCreate should set timestamp when null")
    void testOnCreateSetsTimestampWhenNull() throws Exception {
        // Arrange
        errorEntity.setErrorTimestamp(null);

        // Act
        invokeOnCreate(errorEntity);

        // Assert
        assertNotNull(errorEntity.getErrorTimestamp());
        assertTrue(errorEntity.getErrorTimestamp().isBefore(Instant.now().plusSeconds(1)));
        assertTrue(errorEntity.getErrorTimestamp().isAfter(Instant.now().minusSeconds(1)));
    }

    @Test
    @DisplayName("onCreate should not override existing timestamp")
    void testOnCreateDoesNotOverrideExistingTimestamp() throws Exception {
        // Arrange
        Instant existingTimestamp = Instant.parse("2024-01-01T10:00:00Z");
        errorEntity.setErrorTimestamp(existingTimestamp);

        // Act
        invokeOnCreate(errorEntity);

        // Assert
        assertEquals(existingTimestamp, errorEntity.getErrorTimestamp());
    }

    @Test
    @DisplayName("onCreate can be called multiple times safely")
    void testOnCreateMultipleCalls() throws Exception {
        // Arrange
        errorEntity.setErrorTimestamp(null);

        // Act
        invokeOnCreate(errorEntity);
        Instant firstTimestamp = errorEntity.getErrorTimestamp();
        
        invokeOnCreate(errorEntity); // Call again
        Instant secondTimestamp = errorEntity.getErrorTimestamp();

        // Assert
        assertNotNull(firstTimestamp);
        assertEquals(firstTimestamp, secondTimestamp);
    }

    // ==================== Complete Entity Tests ====================

    @Test
    @DisplayName("Should create complete valid error entity")
    void testCompleteValidEntity() {
        // Arrange & Act
        errorEntity.setId(1L);
        errorEntity.setEventType("CREATED");
        errorEntity.setErrorDescription("Processing error: Duplicate key violation");
        errorEntity.setMessageData("{\"productId\":123,\"name\":\"Test Product\"}");
        errorEntity.setErrorTimestamp(testTimestamp);
        errorEntity.setEntityType("Product");

        // Assert
        assertNotNull(errorEntity);
        assertEquals(1L, errorEntity.getId());
        assertEquals("CREATED", errorEntity.getEventType());
        assertEquals("Processing error: Duplicate key violation", errorEntity.getErrorDescription());
        assertEquals("{\"productId\":123,\"name\":\"Test Product\"}", errorEntity.getMessageData());
        assertEquals(testTimestamp, errorEntity.getErrorTimestamp());
        assertEquals("Product", errorEntity.getEntityType());
    }

    @Test
    @DisplayName("Should create entity with minimal required fields")
    void testMinimalRequiredFields() {
        // Act
        errorEntity.setEventType("DELETED");
        errorEntity.setErrorTimestamp(testTimestamp);
        errorEntity.setEntityType("Kardex");

        // Assert
        assertNotNull(errorEntity.getEventType());
        assertNotNull(errorEntity.getErrorTimestamp());
        assertNotNull(errorEntity.getEntityType());
        assertNull(errorEntity.getId());
        assertNull(errorEntity.getErrorDescription());
        assertNull(errorEntity.getMessageData());
    }

    @Test
    @DisplayName("Should update all fields correctly")
    void testUpdateAllFields() {
        // Arrange - Initial values
        errorEntity.setId(1L);
        errorEntity.setEventType("CREATED");
        errorEntity.setErrorDescription("Initial error");
        errorEntity.setMessageData("{\"old\":\"data\"}");
        errorEntity.setErrorTimestamp(testTimestamp);
        errorEntity.setEntityType("Product");

        // Act - Update values
        Instant newTimestamp = Instant.now().plus(1, ChronoUnit.HOURS);
        errorEntity.setId(2L);
        errorEntity.setEventType("UPDATED");
        errorEntity.setErrorDescription("Updated error");
        errorEntity.setMessageData("{\"new\":\"data\"}");
        errorEntity.setErrorTimestamp(newTimestamp);
        errorEntity.setEntityType("Kardex");

        // Assert
        assertEquals(2L, errorEntity.getId());
        assertEquals("UPDATED", errorEntity.getEventType());
        assertEquals("Updated error", errorEntity.getErrorDescription());
        assertEquals("{\"new\":\"data\"}", errorEntity.getMessageData());
        assertEquals(newTimestamp, errorEntity.getErrorTimestamp());
        assertEquals("Kardex", errorEntity.getEntityType());
    }

    @Test
    @DisplayName("Should maintain data integrity after multiple operations")
    void testDataIntegrityAfterMultipleOperations() {
        // Arrange & Act
        errorEntity.setEventType("CREATED");
        String firstEventType = errorEntity.getEventType();
        
        errorEntity.setEntityType("Product");
        errorEntity.setErrorDescription("Error occurred");
        
        String secondEventType = errorEntity.getEventType();

        // Assert - Original values should remain unchanged
        assertEquals(firstEventType, secondEventType);
        assertEquals("CREATED", errorEntity.getEventType());
        assertEquals("Product", errorEntity.getEntityType());
        assertEquals("Error occurred", errorEntity.getErrorDescription());
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle entity representing validation error")
    void testValidationErrorEntity() throws Exception {
        // Arrange & Act
        errorEntity.setEventType("CREATED");
        errorEntity.setErrorDescription("Validation failed: Required fields are missing");
        errorEntity.setMessageData("{\"productId\":null,\"name\":null}");
        errorEntity.setEntityType("Product");
        invokeOnCreate(errorEntity);

        // Assert
        assertEquals("Validation failed: Required fields are missing", errorEntity.getErrorDescription());
        assertTrue(errorEntity.getMessageData().contains("null"));
        assertNotNull(errorEntity.getErrorTimestamp());
    }

    @Test
    @DisplayName("Should handle entity representing processing error")
    void testProcessingErrorEntity() throws Exception {
        // Arrange & Act
        errorEntity.setEventType("UPDATED");
        errorEntity.setErrorDescription("Processing error: Database connection timeout");
        errorEntity.setMessageData("{\"operation\":\"update\",\"retries\":3}");
        errorEntity.setEntityType("Kardex");
        invokeOnCreate(errorEntity);

        // Assert
        assertTrue(errorEntity.getErrorDescription().contains("Processing error"));
        assertTrue(errorEntity.getMessageData().contains("retries"));
        assertNotNull(errorEntity.getErrorTimestamp());
    }

    @Test
    @DisplayName("Should handle entity representing recovery action")
    void testRecoveryActionEntity() throws Exception {
        // Arrange & Act
        errorEntity.setEventType("CREATED");
        errorEntity.setErrorDescription("Processing error: Duplicate product (Recovery action executed)");
        errorEntity.setMessageData("{\"productId\":456,\"recoveryAttempted\":true}");
        errorEntity.setEntityType("Product");
        invokeOnCreate(errorEntity);

        // Assert
        assertTrue(errorEntity.getErrorDescription().contains("Recovery action executed"));
        assertTrue(errorEntity.getMessageData().contains("recoveryAttempted"));
    }

    @Test
    @DisplayName("Should handle consecutive errors with same event type")
    void testConsecutiveErrorsSameEventType() throws Exception {
        // Arrange
        MessageProcessingErrorEntity error1 = new MessageProcessingErrorEntity();
        MessageProcessingErrorEntity error2 = new MessageProcessingErrorEntity();

        // Act
        error1.setEventType("CREATED");
        error1.setEntityType("Product");
        invokeOnCreate(error1);

        error2.setEventType("CREATED");
        error2.setEntityType("Product");
        invokeOnCreate(error2);

        // Assert
        assertEquals(error1.getEventType(), error2.getEventType());
        assertEquals(error1.getEntityType(), error2.getEntityType());
        assertNotNull(error1.getErrorTimestamp());
        assertNotNull(error2.getErrorTimestamp());
    }

    @Test
    @DisplayName("Should handle error with very long message data")
    void testErrorWithVeryLongMessageData() {
        // Arrange
        StringBuilder largeJson = new StringBuilder("{\"data\":\"");
        largeJson.append("x".repeat(10000));
        largeJson.append("\"}");

        // Act
        errorEntity.setMessageData(largeJson.toString());
        errorEntity.setEventType("CREATED");
        errorEntity.setEntityType("Product");

        // Assert
        assertTrue(errorEntity.getMessageData().length() > 10000);
        assertNotNull(errorEntity.getEventType());
    }
}
