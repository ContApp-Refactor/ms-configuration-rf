package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.jpa.Entity;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.entity.SyncStateEntity;

/**
 * @brief Unit tests for SyncStateEntity
 * 
 * Tests JPA entity for synchronization state tracking including
 * field validation, getters/setters, and lifecycle callbacks.
 */
@DisplayName("SyncStateEntity Unit Tests")
public class SyncStateEntityUnitTest {
    
    private SyncStateEntity syncStateEntity;
    private Instant testInstant;

    @BeforeEach
    void setUp() {
        syncStateEntity = new SyncStateEntity();
        testInstant = Instant.now();
    }

    /**
     * Helper method to invoke protected onCreate() method using reflection
     */
    private void invokeOnCreate(SyncStateEntity entity) throws Exception {
        Method onCreateMethod = SyncStateEntity.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);
        onCreateMethod.invoke(entity);
    }

    /**
     * Helper method to invoke protected onUpdate() method using reflection
     */
    private void invokeOnUpdate(SyncStateEntity entity) throws Exception {
        Method onUpdateMethod = SyncStateEntity.class.getDeclaredMethod("onUpdate");
        onUpdateMethod.setAccessible(true);
        onUpdateMethod.invoke(entity);
    }

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Should create entity with default constructor")
    void testDefaultConstructor() {
        // Act
        SyncStateEntity entity = new SyncStateEntity();

        // Assert
        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getSyncType());
        assertNull(entity.getEnterpriseId());
        assertNull(entity.getLastSyncDate());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    // ==================== ID Tests ====================

    @Test
    @DisplayName("Should set and get id correctly")
    void testIdGetterSetter() {
        // Arrange
        Long expectedId = 123L;

        // Act
        syncStateEntity.setId(expectedId);

        // Assert
        assertEquals(expectedId, syncStateEntity.getId());
    }

    @Test
    @DisplayName("Should handle null id")
    void testNullId() {
        // Act
        syncStateEntity.setId(null);

        // Assert
        assertNull(syncStateEntity.getId());
    }

    @Test
    @DisplayName("Should handle large id values")
    void testLargeId() {
        // Arrange
        Long largeId = Long.MAX_VALUE;

        // Act
        syncStateEntity.setId(largeId);

        // Assert
        assertEquals(largeId, syncStateEntity.getId());
    }

    // ==================== Sync Type Tests ====================

    @Test
    @DisplayName("Should set and get syncType correctly")
    void testSyncTypeGetterSetter() {
        // Arrange
        String expectedSyncType = "PRODUCT_SYNC";

        // Act
        syncStateEntity.setSyncType(expectedSyncType);

        // Assert
        assertEquals(expectedSyncType, syncStateEntity.getSyncType());
    }

    @Test
    @DisplayName("Should handle null syncType")
    void testNullSyncType() {
        // Act
        syncStateEntity.setSyncType(null);

        // Assert
        assertNull(syncStateEntity.getSyncType());
    }

    @Test
    @DisplayName("Should handle empty syncType")
    void testEmptySyncType() {
        // Act
        syncStateEntity.setSyncType("");

        // Assert
        assertEquals("", syncStateEntity.getSyncType());
    }

    @Test
    @DisplayName("Should handle different sync types")
    void testDifferentSyncTypes() {
        // Test PRODUCT_SYNC
        syncStateEntity.setSyncType("PRODUCT_SYNC");
        assertEquals("PRODUCT_SYNC", syncStateEntity.getSyncType());

        // Test KARDEX_SYNC
        syncStateEntity.setSyncType("KARDEX_SYNC");
        assertEquals("KARDEX_SYNC", syncStateEntity.getSyncType());

        // Test INVENTORY_SYNC
        syncStateEntity.setSyncType("INVENTORY_SYNC");
        assertEquals("INVENTORY_SYNC", syncStateEntity.getSyncType());
    }

    @Test
    @DisplayName("Should handle syncType with special characters")
    void testSyncTypeWithSpecialCharacters() {
        // Arrange
        String specialSyncType = "SYNC_TYPE-2024_V1";

        // Act
        syncStateEntity.setSyncType(specialSyncType);

        // Assert
        assertEquals(specialSyncType, syncStateEntity.getSyncType());
    }

    // ==================== Enterprise ID Tests ====================

    @Test
    @DisplayName("Should set and get enterpriseId correctly")
    void testEnterpriseIdGetterSetter() {
        // Arrange
        String expectedEnterpriseId = "ENT-12345";

        // Act
        syncStateEntity.setEnterpriseId(expectedEnterpriseId);

        // Assert
        assertEquals(expectedEnterpriseId, syncStateEntity.getEnterpriseId());
    }

    @Test
    @DisplayName("Should handle null enterpriseId")
    void testNullEnterpriseId() {
        // Act
        syncStateEntity.setEnterpriseId(null);

        // Assert
        assertNull(syncStateEntity.getEnterpriseId());
    }

    @Test
    @DisplayName("Should handle empty enterpriseId")
    void testEmptyEnterpriseId() {
        // Act
        syncStateEntity.setEnterpriseId("");

        // Assert
        assertEquals("", syncStateEntity.getEnterpriseId());
    }

    @Test
    @DisplayName("Should handle UUID format enterpriseId")
    void testUuidEnterpriseId() {
        // Arrange
        String uuidEnterpriseId = "550e8400-e29b-41d4-a716-446655440000";

        // Act
        syncStateEntity.setEnterpriseId(uuidEnterpriseId);

        // Assert
        assertEquals(uuidEnterpriseId, syncStateEntity.getEnterpriseId());
    }

    @Test
    @DisplayName("Should handle long enterpriseId")
    void testLongEnterpriseId() {
        // Arrange
        String longEnterpriseId = "ENTERPRISE-" + "X".repeat(100);

        // Act
        syncStateEntity.setEnterpriseId(longEnterpriseId);

        // Assert
        assertEquals(longEnterpriseId, syncStateEntity.getEnterpriseId());
    }

    // ==================== Last Sync Date Tests ====================

    @Test
    @DisplayName("Should set and get lastSyncDate correctly")
    void testLastSyncDateGetterSetter() {
        // Act
        syncStateEntity.setLastSyncDate(testInstant);

        // Assert
        assertEquals(testInstant, syncStateEntity.getLastSyncDate());
    }

    @Test
    @DisplayName("Should handle null lastSyncDate")
    void testNullLastSyncDate() {
        // Act
        syncStateEntity.setLastSyncDate(null);

        // Assert
        assertNull(syncStateEntity.getLastSyncDate());
    }

    @Test
    @DisplayName("Should handle past lastSyncDate")
    void testPastLastSyncDate() {
        // Arrange
        Instant pastDate = Instant.now().minus(7, ChronoUnit.DAYS);

        // Act
        syncStateEntity.setLastSyncDate(pastDate);

        // Assert
        assertEquals(pastDate, syncStateEntity.getLastSyncDate());
        assertTrue(syncStateEntity.getLastSyncDate().isBefore(Instant.now()));
    }

    @Test
    @DisplayName("Should handle very old lastSyncDate")
    void testVeryOldLastSyncDate() {
        // Arrange
        Instant oldDate = Instant.parse("2020-01-01T00:00:00Z");

        // Act
        syncStateEntity.setLastSyncDate(oldDate);

        // Assert
        assertEquals(oldDate, syncStateEntity.getLastSyncDate());
    }

    @Test
    @DisplayName("Should handle lastSyncDate with nanosecond precision")
    void testLastSyncDateWithNanoseconds() {
        // Arrange
        Instant preciseDate = Instant.now();

        // Act
        syncStateEntity.setLastSyncDate(preciseDate);

        // Assert
        assertEquals(preciseDate, syncStateEntity.getLastSyncDate());
        assertEquals(preciseDate.getNano(), syncStateEntity.getLastSyncDate().getNano());
    }

    // ==================== Created At Tests ====================

    @Test
    @DisplayName("Should set and get createdAt correctly")
    void testCreatedAtGetterSetter() {
        // Act
        syncStateEntity.setCreatedAt(testInstant);

        // Assert
        assertEquals(testInstant, syncStateEntity.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle null createdAt")
    void testNullCreatedAt() {
        // Act
        syncStateEntity.setCreatedAt(null);

        // Assert
        assertNull(syncStateEntity.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle createdAt in the past")
    void testCreatedAtInPast() {
        // Arrange
        Instant pastDate = Instant.now().minus(30, ChronoUnit.DAYS);

        // Act
        syncStateEntity.setCreatedAt(pastDate);

        // Assert
        assertEquals(pastDate, syncStateEntity.getCreatedAt());
        assertTrue(syncStateEntity.getCreatedAt().isBefore(Instant.now()));
    }

    // ==================== Updated At Tests ====================

    @Test
    @DisplayName("Should set and get updatedAt correctly")
    void testUpdatedAtGetterSetter() {
        // Act
        syncStateEntity.setUpdatedAt(testInstant);

        // Assert
        assertEquals(testInstant, syncStateEntity.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle null updatedAt")
    void testNullUpdatedAt() {
        // Act
        syncStateEntity.setUpdatedAt(null);

        // Assert
        assertNull(syncStateEntity.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle updatedAt after createdAt")
    void testUpdatedAtAfterCreatedAt() {
        // Arrange
        Instant created = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant updated = Instant.now();

        // Act
        syncStateEntity.setCreatedAt(created);
        syncStateEntity.setUpdatedAt(updated);

        // Assert
        assertTrue(syncStateEntity.getUpdatedAt().isAfter(syncStateEntity.getCreatedAt()));
    }

   

    @Test
    @DisplayName("onCreate should set timestamps to current time")
    void testOnCreateSetsCurrentTime() throws Exception {
        // Arrange
        Instant before = Instant.now();

        // Act
        invokeOnCreate(syncStateEntity);

        // Arrange
        Instant after = Instant.now();

        // Assert
        assertNotNull(syncStateEntity.getCreatedAt());
        assertFalse(syncStateEntity.getCreatedAt().isBefore(before));
        assertFalse(syncStateEntity.getCreatedAt().isAfter(after));
    }

    @Test
    @DisplayName("onCreate can be called multiple times")
    void testOnCreateMultipleCalls() throws Exception {
        // Act
        invokeOnCreate(syncStateEntity);
        Instant firstCreated = syncStateEntity.getCreatedAt();
        
        // Small delay to ensure different timestamps
        Thread.sleep(10);
        
        invokeOnCreate(syncStateEntity);
        Instant secondCreated = syncStateEntity.getCreatedAt();

        // Assert
        assertNotNull(firstCreated);
        assertNotNull(secondCreated);
        // Second call should update the timestamps
        assertFalse(firstCreated.equals(secondCreated));
    }

    // ==================== @PreUpdate Tests ====================

    @Test
    @DisplayName("onUpdate should set updatedAt")
    void testOnUpdateSetsUpdatedAt() throws Exception {
        // Arrange
        invokeOnCreate(syncStateEntity);
        Instant originalUpdatedAt = syncStateEntity.getUpdatedAt();
        
        // Small delay to ensure different timestamp
        Thread.sleep(10);

        // Act
        invokeOnUpdate(syncStateEntity);

        // Assert
        assertNotNull(syncStateEntity.getUpdatedAt());
        assertTrue(syncStateEntity.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    @DisplayName("onUpdate should not modify createdAt")
    void testOnUpdateDoesNotModifyCreatedAt() throws Exception {
        // Arrange
        invokeOnCreate(syncStateEntity);
        Instant originalCreatedAt = syncStateEntity.getCreatedAt();
        
        // Small delay
        Thread.sleep(10);

        // Act
        invokeOnUpdate(syncStateEntity);

        // Assert
        assertEquals(originalCreatedAt, syncStateEntity.getCreatedAt());
    }

    @Test
    @DisplayName("onUpdate can be called multiple times")
    void testOnUpdateMultipleCalls() throws Exception {
        // Arrange
        invokeOnCreate(syncStateEntity);
        
        // Act
        Thread.sleep(10);
        invokeOnUpdate(syncStateEntity);
        Instant firstUpdate = syncStateEntity.getUpdatedAt();
        
        Thread.sleep(10);
        invokeOnUpdate(syncStateEntity);
        Instant secondUpdate = syncStateEntity.getUpdatedAt();

        // Assert
        assertNotNull(firstUpdate);
        assertNotNull(secondUpdate);
        assertTrue(secondUpdate.isAfter(firstUpdate));
    }

    @Test
    @DisplayName("onUpdate should work even if onCreate was not called")
    void testOnUpdateWithoutOnCreate() throws Exception {
        // Act
        invokeOnUpdate(syncStateEntity);

        // Assert
        assertNotNull(syncStateEntity.getUpdatedAt());
        assertNull(syncStateEntity.getCreatedAt());
    }

    // ==================== Complete Entity Tests ====================

    @Test
    @DisplayName("Should create complete valid sync state entity")
    void testCompleteValidEntity() throws Exception {
        // Arrange & Act
        syncStateEntity.setId(1L);
        syncStateEntity.setSyncType("PRODUCT_SYNC");
        syncStateEntity.setEnterpriseId("ENT-789");
        syncStateEntity.setLastSyncDate(testInstant);
        invokeOnCreate(syncStateEntity);

        // Assert
        assertNotNull(syncStateEntity);
        assertEquals(1L, syncStateEntity.getId());
        assertEquals("PRODUCT_SYNC", syncStateEntity.getSyncType());
        assertEquals("ENT-789", syncStateEntity.getEnterpriseId());
        assertEquals(testInstant, syncStateEntity.getLastSyncDate());
        assertNotNull(syncStateEntity.getCreatedAt());
        assertNotNull(syncStateEntity.getUpdatedAt());
    }

    @Test
    @DisplayName("Should create entity with minimal required fields")
    void testMinimalRequiredFields() {
        // Act
        syncStateEntity.setSyncType("INVENTORY_SYNC");
        syncStateEntity.setEnterpriseId("ENT-001");
        syncStateEntity.setLastSyncDate(testInstant);

        // Assert
        assertNotNull(syncStateEntity.getSyncType());
        assertNotNull(syncStateEntity.getEnterpriseId());
        assertNotNull(syncStateEntity.getLastSyncDate());
    }

    @Test
    @DisplayName("Should update all fields correctly")
    void testUpdateAllFields() throws Exception {
        // Arrange - Initial values
        syncStateEntity.setId(1L);
        syncStateEntity.setSyncType("PRODUCT_SYNC");
        syncStateEntity.setEnterpriseId("ENT-001");
        syncStateEntity.setLastSyncDate(testInstant);
        invokeOnCreate(syncStateEntity);
        
        Instant originalCreatedAt = syncStateEntity.getCreatedAt();

        // Act - Update values
        Thread.sleep(10);
        Instant newSyncDate = Instant.now();
        syncStateEntity.setId(2L);
        syncStateEntity.setSyncType("KARDEX_SYNC");
        syncStateEntity.setEnterpriseId("ENT-002");
        syncStateEntity.setLastSyncDate(newSyncDate);
        invokeOnUpdate(syncStateEntity);

        // Assert
        assertEquals(2L, syncStateEntity.getId());
        assertEquals("KARDEX_SYNC", syncStateEntity.getSyncType());
        assertEquals("ENT-002", syncStateEntity.getEnterpriseId());
        assertEquals(newSyncDate, syncStateEntity.getLastSyncDate());
        assertEquals(originalCreatedAt, syncStateEntity.getCreatedAt());
        assertTrue(syncStateEntity.getUpdatedAt().isAfter(originalCreatedAt));
    }

    @Test
    @DisplayName("Should maintain data integrity after multiple operations")
    void testDataIntegrityAfterMultipleOperations() throws Exception {
        // Arrange & Act
        syncStateEntity.setSyncType("PRODUCT_SYNC");
        String firstSyncType = syncStateEntity.getSyncType();
        
        syncStateEntity.setEnterpriseId("ENT-123");
        syncStateEntity.setLastSyncDate(testInstant);
        invokeOnCreate(syncStateEntity);
        
        String secondSyncType = syncStateEntity.getSyncType();

        // Assert
        assertEquals(firstSyncType, secondSyncType);
        assertEquals("PRODUCT_SYNC", syncStateEntity.getSyncType());
        assertEquals("ENT-123", syncStateEntity.getEnterpriseId());
        assertNotNull(syncStateEntity.getCreatedAt());
        assertNotNull(syncStateEntity.getUpdatedAt());
    }

    // ==================== Business Logic Tests ====================

    @Test
    @DisplayName("Should track sync for specific enterprise")
    void testTrackSyncForEnterprise() throws Exception {
        // Arrange & Act
        syncStateEntity.setSyncType("PRODUCT_SYNC");
        syncStateEntity.setEnterpriseId("ENT-456");
        syncStateEntity.setLastSyncDate(testInstant);
        invokeOnCreate(syncStateEntity);

        // Assert
        assertEquals("ENT-456", syncStateEntity.getEnterpriseId());
        assertEquals("PRODUCT_SYNC", syncStateEntity.getSyncType());
        assertNotNull(syncStateEntity.getLastSyncDate());
    }

    @Test
    @DisplayName("Should handle multiple sync types for same enterprise")
    void testMultipleSyncTypesSameEnterprise() {
        // Arrange
        SyncStateEntity productSync = new SyncStateEntity();
        SyncStateEntity kardexSync = new SyncStateEntity();
        String sharedEnterpriseId = "ENT-SHARED";

        // Act
        productSync.setSyncType("PRODUCT_SYNC");
        productSync.setEnterpriseId(sharedEnterpriseId);
        productSync.setLastSyncDate(testInstant);

        kardexSync.setSyncType("KARDEX_SYNC");
        kardexSync.setEnterpriseId(sharedEnterpriseId);
        kardexSync.setLastSyncDate(testInstant);

        // Assert
        assertEquals(productSync.getEnterpriseId(), kardexSync.getEnterpriseId());
        assertNotEquals(productSync.getSyncType(), kardexSync.getSyncType());
    }

    @Test
    @DisplayName("Should update lastSyncDate on successful sync")
    void testUpdateLastSyncDate() throws Exception {
        // Arrange
        Instant initialSync = Instant.now().minus(1, ChronoUnit.HOURS);
        syncStateEntity.setLastSyncDate(initialSync);
        invokeOnCreate(syncStateEntity);

        // Act
        Thread.sleep(10);
        Instant newSync = Instant.now();
        syncStateEntity.setLastSyncDate(newSync);
        invokeOnUpdate(syncStateEntity);

        // Assert
        assertEquals(newSync, syncStateEntity.getLastSyncDate());
        assertTrue(syncStateEntity.getLastSyncDate().isAfter(initialSync));
        assertTrue(syncStateEntity.getUpdatedAt().isAfter(syncStateEntity.getCreatedAt()));
    }

    
    @Test
    @DisplayName("Should handle same sync type for different enterprises")
    void testSameSyncTypeDifferentEnterprises() {
        // Arrange
        SyncStateEntity sync1 = new SyncStateEntity();
        SyncStateEntity sync2 = new SyncStateEntity();
        String sharedSyncType = "PRODUCT_SYNC";

        // Act
        sync1.setSyncType(sharedSyncType);
        sync1.setEnterpriseId("ENT-001");
        
        sync2.setSyncType(sharedSyncType);
        sync2.setEnterpriseId("ENT-002");

        // Assert
        assertEquals(sync1.getSyncType(), sync2.getSyncType());
        assertNotEquals(sync1.getEnterpriseId(), sync2.getEnterpriseId());
    }

    @Test
    @DisplayName("Should handle rapid sequential updates")
    void testRapidSequentialUpdates() throws Exception {
        // Arrange
        invokeOnCreate(syncStateEntity);
        Instant created = syncStateEntity.getCreatedAt();

        // Act - Multiple rapid updates
        for (int i = 0; i < 5; i++) {
            Thread.sleep(5);
            invokeOnUpdate(syncStateEntity);
        }

        // Assert
        assertEquals(created, syncStateEntity.getCreatedAt());
        assertTrue(syncStateEntity.getUpdatedAt().isAfter(created));
    }

    @Test
    @DisplayName("Should track time difference between create and update")
    void testTimeDifferenceBetweenCreateAndUpdate() throws Exception {
        // Arrange
        invokeOnCreate(syncStateEntity);
        Instant created = syncStateEntity.getCreatedAt();

        // Act
        Thread.sleep(100); // Significant delay
        invokeOnUpdate(syncStateEntity);
        Instant updated = syncStateEntity.getUpdatedAt();

        // Assert
        long millisDifference = ChronoUnit.MILLIS.between(created, updated);
        assertTrue(millisDifference >= 100);
    }
}
