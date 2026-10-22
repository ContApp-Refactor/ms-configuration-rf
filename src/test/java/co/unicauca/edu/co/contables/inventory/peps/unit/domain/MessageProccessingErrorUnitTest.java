package co.unicauca.edu.co.contables.inventory.peps.unit.domain;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.MessageProcessingError;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MessageProccessingErrorUnitTest {

    // ========== Constructor Tests ==========

    @Test
    void testNoArgsConstructor() {
        MessageProcessingError error = new MessageProcessingError();
        assertNotNull(error);
        assertNull(error.getId());
        assertNull(error.getEventType());
        assertNull(error.getErrorDescription());
        assertNull(error.getMessageData());
        assertNull(error.getErrorTimestamp());
        assertNull(error.getEntityType());
    }

    @Test
    void testAllArgsConstructor() {
        Long id = 1L;
        String eventType = "PRODUCT_CREATED";
        String errorDescription = "Failed to process product creation";
        String messageData = "{\"productId\":123}";
        Instant timestamp = Instant.now();
        String entityType = "Product";

        MessageProcessingError error = new MessageProcessingError(
                id, eventType, errorDescription, messageData, timestamp, entityType
        );

        assertEquals(id, error.getId());
        assertEquals(eventType, error.getEventType());
        assertEquals(errorDescription, error.getErrorDescription());
        assertEquals(messageData, error.getMessageData());
        assertEquals(timestamp, error.getErrorTimestamp());
        assertEquals(entityType, error.getEntityType());
    }

    // ========== Getter/Setter Tests ==========

    @Test
    void testSetAndGetId() {
        MessageProcessingError error = new MessageProcessingError();
        Long id = 100L;
        error.setId(id);
        assertEquals(id, error.getId());
    }

    @Test
    void testSetAndGetEventType() {
        MessageProcessingError error = new MessageProcessingError();
        String eventType = "INVENTORY_UPDATED";
        error.setEventType(eventType);
        assertEquals(eventType, error.getEventType());
    }

    @Test
    void testSetAndGetErrorDescription() {
        MessageProcessingError error = new MessageProcessingError();
        String description = "Database connection timeout";
        error.setErrorDescription(description);
        assertEquals(description, error.getErrorDescription());
    }

    @Test
    void testSetAndGetMessageData() {
        MessageProcessingError error = new MessageProcessingError();
        String data = "{\"key\":\"value\"}";
        error.setMessageData(data);
        assertEquals(data, error.getMessageData());
    }

    @Test
    void testSetAndGetErrorTimestamp() {
        MessageProcessingError error = new MessageProcessingError();
        Instant timestamp = Instant.parse("2024-01-15T10:30:00Z");
        error.setErrorTimestamp(timestamp);
        assertEquals(timestamp, error.getErrorTimestamp());
    }

    @Test
    void testSetAndGetEntityType() {
        MessageProcessingError error = new MessageProcessingError();
        String entityType = "Kardex";
        error.setEntityType(entityType);
        assertEquals(entityType, error.getEntityType());
    }

    // ========== requireValid() Tests ==========

    @Test
    void testRequireValid_WithAllRequiredFields_Success() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("PRODUCT_DELETED");
        error.setErrorDescription("Product not found");
        error.setEntityType("Product");

        MessageProcessingError result = error.requireValid();

        assertNotNull(result);
        assertSame(error, result); // Fluent API returns same instance
        assertNotNull(error.getErrorTimestamp()); // Should be auto-set
    }

    @Test
    void testRequireValid_WithNullEventType_ThrowsException() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType(null);
        error.setErrorDescription("Some error");
        error.setEntityType("Product");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> error.requireValid()
        );
        assertEquals("eventType is required", exception.getMessage());
    }

    @Test
    void testRequireValid_WithBlankEventType_ThrowsException() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("   ");
        error.setErrorDescription("Some error");
        error.setEntityType("Product");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> error.requireValid()
        );
        assertEquals("eventType is required", exception.getMessage());
    }

    @Test
    void testRequireValid_WithEmptyEventType_ThrowsException() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("");
        error.setErrorDescription("Some error");
        error.setEntityType("Product");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> error.requireValid()
        );
        assertEquals("eventType is required", exception.getMessage());
    }

    @Test
    void testRequireValid_WithNullErrorDescription_ThrowsException() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("INVENTORY_SYNC");
        error.setErrorDescription(null);
        error.setEntityType("Inventory");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> error.requireValid()
        );
        assertEquals("errorDescription is required", exception.getMessage());
    }

    @Test
    void testRequireValid_WithBlankErrorDescription_ThrowsException() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("INVENTORY_SYNC");
        error.setErrorDescription("  \t  ");
        error.setEntityType("Inventory");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> error.requireValid()
        );
        assertEquals("errorDescription is required", exception.getMessage());
    }

    @Test
    void testRequireValid_WithNullEntityType_ThrowsException() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("DATA_IMPORT");
        error.setErrorDescription("Import failed");
        error.setEntityType(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> error.requireValid()
        );
        assertEquals("entityType is required", exception.getMessage());
    }

    @Test
    void testRequireValid_WithBlankEntityType_ThrowsException() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("DATA_IMPORT");
        error.setErrorDescription("Import failed");
        error.setEntityType("");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> error.requireValid()
        );
        assertEquals("entityType is required", exception.getMessage());
    }

    @Test
    void testRequireValid_SetsTimestampWhenNull() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("SYNC_ERROR");
        error.setErrorDescription("Sync timeout");
        error.setEntityType("SyncState");
        error.setErrorTimestamp(null);

        Instant before = Instant.now();
        error.requireValid();
        Instant after = Instant.now();

        assertNotNull(error.getErrorTimestamp());
        assertTrue(error.getErrorTimestamp().isAfter(before.minusSeconds(1)));
        assertTrue(error.getErrorTimestamp().isBefore(after.plusSeconds(1)));
    }

    @Test
    void testRequireValid_PreservesExistingTimestamp() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("VALIDATION_ERROR");
        error.setErrorDescription("Invalid data format");
        error.setEntityType("Message");
        Instant existingTimestamp = Instant.parse("2024-06-15T14:30:00Z");
        error.setErrorTimestamp(existingTimestamp);

        error.requireValid();

        assertEquals(existingTimestamp, error.getErrorTimestamp());
    }

    // ========== summary() Tests ==========

    @Test
    void testSummary_WithAllFields() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("PRODUCT_CREATED");
        error.setEntityType("Product");
        error.setErrorDescription("Failed to create product in database");
        error.setMessageData("{\"productId\":123,\"name\":\"Test Product\"}");
        error.setErrorTimestamp(Instant.parse("2024-01-20T10:30:00Z"));

        String summary = error.summary(50);

        assertTrue(summary.contains("event=PRODUCT_CREATED"));
        assertTrue(summary.contains("entity=Product"));
        assertTrue(summary.contains("at=2024-01-20T10:30:00Z"));
        assertTrue(summary.contains("detail="));
        assertTrue(summary.contains("Failed to create product in database"));
    }

    @Test
    void testSummary_WithNullErrorDescription_UsesMessageData() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("DATA_SYNC");
        error.setEntityType("SyncState");
        error.setErrorDescription(null);
        error.setMessageData("Sync failed for tenant ABC");
        error.setErrorTimestamp(Instant.parse("2024-02-10T08:15:00Z"));

        String summary = error.summary(40);

        assertTrue(summary.contains("event=DATA_SYNC"));
        assertTrue(summary.contains("entity=SyncState"));
        assertTrue(summary.contains("detail=Sync failed for tenant ABC"));
    }

    @Test
    void testSummary_TruncatesLongDetails() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("ERROR");
        error.setEntityType("Test");
        error.setErrorDescription("This is a very long error description that should be truncated to the maximum length specified");
        error.setErrorTimestamp(Instant.parse("2024-03-01T12:00:00Z"));

        String summary = error.summary(20);

        assertTrue(summary.contains("detail="));
        // The detail should be truncated (note: Math.max(32, 20) = 32)
        String[] parts = summary.split("detail=");
        assertTrue(parts.length > 1);
        String detailPart = parts[1];
        assertTrue(detailPart.length() <= 32);
    }

    @Test
    void testSummary_WithMaxDetailLength32() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("TEST");
        error.setEntityType("Entity");
        error.setErrorDescription("Short error");
        error.setErrorTimestamp(Instant.parse("2024-04-01T09:00:00Z"));

        String summary = error.summary(15); // Should use Math.max(32, 15) = 32

        assertTrue(summary.contains("detail=Short error"));
    }

    @Test
    void testSummary_WithNullTimestamp() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("NULL_TIMESTAMP_TEST");
        error.setEntityType("Test");
        error.setErrorDescription("Testing null timestamp");
        error.setErrorTimestamp(null);

        String summary = error.summary(50);

        assertTrue(summary.contains("at=null"));
    }

    @Test
    void testSummary_WithBlankEventType() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("");
        error.setEntityType("Product");
        error.setErrorDescription("Error occurred");
        error.setErrorTimestamp(Instant.now());

        String summary = error.summary(50);

        assertFalse(summary.contains("event="));
        assertTrue(summary.contains("entity=Product"));
    }

    @Test
    void testSummary_WithBlankEntityType() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("PROCESS_ERROR");
        error.setEntityType("   ");
        error.setErrorDescription("Processing failed");
        error.setErrorTimestamp(Instant.now());

        String summary = error.summary(50);

        assertTrue(summary.contains("event=PROCESS_ERROR"));
        assertFalse(summary.contains("entity="));
    }

    @Test
    void testSummary_WithNullMessageDataAndErrorDescription() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("NULL_TEST");
        error.setEntityType("Test");
        error.setErrorDescription(null);
        error.setMessageData(null);
        error.setErrorTimestamp(Instant.now());

        String summary = error.summary(50);

        assertTrue(summary.contains("detail=null"));
    }

    @Test
    void testSummary_WithZeroMaxLength() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("ZERO_TEST");
        error.setEntityType("Test");
        error.setErrorDescription("Some error");
        error.setErrorTimestamp(Instant.now());

        String summary = error.summary(0); // Math.max(32, 0) = 32

        assertTrue(summary.contains("detail=Some error"));
    }

    @Test
    void testSummary_WithNegativeMaxLength() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("NEGATIVE_TEST");
        error.setEntityType("Test");
        error.setErrorDescription("Error message");
        error.setErrorTimestamp(Instant.now());

        String summary = error.summary(-10); // Math.max(32, -10) = 32

        assertTrue(summary.contains("detail=Error message"));
    }

    // ========== Complete Entity Scenarios ==========

    @Test
    void testCompleteErrorScenario_ProductCreationFailure() {
        MessageProcessingError error = new MessageProcessingError();
        error.setId(1L);
        error.setEventType("PRODUCT_CREATED");
        error.setErrorDescription("Duplicate product code detected");
        error.setMessageData("{\"productId\":456,\"code\":\"PROD-001\"}");
        error.setEntityType("Product");

        error.requireValid();

        assertEquals(1L, error.getId());
        assertEquals("PRODUCT_CREATED", error.getEventType());
        assertEquals("Duplicate product code detected", error.getErrorDescription());
        assertEquals("Product", error.getEntityType());
        assertNotNull(error.getErrorTimestamp());

        String summary = error.summary(100);
        assertTrue(summary.contains("PRODUCT_CREATED"));
        assertTrue(summary.contains("Product"));
    }

    @Test
    void testCompleteErrorScenario_InventorySyncFailure() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("INVENTORY_SYNC");
        error.setErrorDescription("Network timeout during sync operation");
        error.setMessageData("{\"kardexId\":789,\"quantity\":100}");
        error.setEntityType("Kardex");
        error.setErrorTimestamp(Instant.parse("2024-05-20T16:45:30Z"));

        MessageProcessingError validated = error.requireValid();

        assertSame(error, validated);
        assertEquals("INVENTORY_SYNC", validated.getEventType());
        assertEquals("Kardex", validated.getEntityType());
        assertEquals(Instant.parse("2024-05-20T16:45:30Z"), validated.getErrorTimestamp());
    }

    @Test
    void testCompleteErrorScenario_ValidationError() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("MESSAGE_VALIDATION");
        error.setErrorDescription("Required field 'tenantId' is missing");
        error.setEntityType("Message");

        error.requireValid();

        String summary = error.summary(60);
        assertTrue(summary.contains("MESSAGE_VALIDATION"));
        assertTrue(summary.contains("Message"));
        assertTrue(summary.contains("Required field"));
    }

    // ========== Edge Cases ==========

    @Test
    void testEdgeCase_VeryLongErrorDescription() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("LONG_ERROR");
        error.setEntityType("Test");
        StringBuilder longDescription = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longDescription.append("Error ");
        }
        error.setErrorDescription(longDescription.toString());

        error.requireValid();

        String summary = error.summary(50);
        assertNotNull(summary);
        assertTrue(summary.length() < longDescription.length());
    }

    @Test
    void testEdgeCase_SpecialCharactersInFields() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("ERROR|WITH|PIPES");
        error.setErrorDescription("Error with special chars: @#$%^&*()");
        error.setEntityType("Special|Entity");

        error.requireValid();

        String summary = error.summary(100);
        assertTrue(summary.contains("ERROR|WITH|PIPES"));
        assertTrue(summary.contains("Special|Entity"));
    }

    @Test
    void testEdgeCase_UnicodeCharacters() {
        MessageProcessingError error = new MessageProcessingError();
        error.setEventType("测试事件");
        error.setErrorDescription("错误描述 with émojis 🚀");
        error.setEntityType("产品");

        error.requireValid();

        assertEquals("测试事件", error.getEventType());
        assertEquals("错误描述 with émojis 🚀", error.getErrorDescription());
        assertEquals("产品", error.getEntityType());
    }
}
