package co.unicauca.edu.co.contables.inventory.peps.unit.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import co.unicauca.edu.co.contables.inventory.stock.domain.model.SyncState;

public class SyncStateUnitTest {

    

    private String validEnterpriseId;
    private Instant validSyncDate;
    
    @BeforeEach
    void setUp() {
        validEnterpriseId = "ENT-001";
        validSyncDate = Instant.now().minus(1, ChronoUnit.HOURS);
    }
   
    @Test
    @DisplayName("create product sync state successfully")
    void testCreateForProductSync_ValidParameters() {
        // Act
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, validSyncDate);
        
        // Assert
        assertAll("Product sync state creation validation",
            () -> assertEquals("products", syncState.getSyncType()),
            () -> assertEquals(validEnterpriseId, syncState.getEnterpriseId()),
            () -> assertEquals(validSyncDate, syncState.getLastSyncDate()),
            () -> assertNotNull(syncState.getCreatedAt()),
            () -> assertNotNull(syncState.getUpdatedAt()),
            () -> assertTrue(syncState.isProductSync())
        );
    }
    
    @Test
    @DisplayName("set timestamps when creating sync state")
    void testCreateForProductSync_SetsTimestamps() {
        // Arrange
        Instant beforeCreation = Instant.now();
        
        // Act
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, validSyncDate);
        
        // Assert
        Instant afterCreation = Instant.now().plusSeconds(1);
        assertTrue(syncState.getCreatedAt().isAfter(beforeCreation.minusSeconds(1)));
        assertTrue(syncState.getCreatedAt().isBefore(afterCreation));
        assertTrue(syncState.getUpdatedAt().isAfter(beforeCreation.minusSeconds(1)));
        assertTrue(syncState.getUpdatedAt().isBefore(afterCreation));
    }
    
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("throw exception when enterprise ID is invalid")
    void testCreateForProductSync_InvalidEnterpriseId(String invalidEnterpriseId) {
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SyncState.createForProductSync(invalidEnterpriseId, validSyncDate)
        );
        
        assertEquals("Enterprise ID cannot be empty", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when enterprise ID is too long")
    void testCreateForProductSync_EnterpriseIdTooLong() {
        // Arrange
        String longEnterpriseId = "A".repeat(51);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SyncState.createForProductSync(longEnterpriseId, validSyncDate)
        );
        
        assertEquals("Enterprise ID is too long", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when sync date is null")
    void testCreateForProductSync_NullSyncDate() {
        // Act y Assert
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> SyncState.createForProductSync(validEnterpriseId, null)
        );
        
        assertEquals("Sync date cannot be null", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when sync date is in the future")
    void testCreateForProductSync_FutureSyncDate() {
        // Arrange
        Instant futureDate = Instant.now().plus(2, ChronoUnit.HOURS);
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> SyncState.createForProductSync(validEnterpriseId, futureDate)
        );
        
        assertEquals("Sync date cannot be in the future", exception.getMessage());
    }
    

    @Test
    @DisplayName("create initial product sync with default date")
    void testCreateInitialProductSync_ValidEnterpriseId() {
        // Act
        SyncState syncState = SyncState.createInitialProductSync(validEnterpriseId);
        
        // Assert
        assertAll("Initial sync state validation",
            () -> assertEquals("products", syncState.getSyncType()),
            () -> assertEquals(validEnterpriseId, syncState.getEnterpriseId()),
            () -> assertEquals(Instant.parse("2000-01-01T00:00:00Z"), syncState.getLastSyncDate()),
            () -> assertTrue(syncState.isFirstSync())
        );
    }
    

    @Test
    @DisplayName("update sync date successfully")
    void testUpdateSyncDate_ValidDate() {
        // Arrange
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, validSyncDate);
        Instant oldUpdatedAt = syncState.getUpdatedAt();
        Instant newSyncDate = Instant.now();
        
        // Act
        syncState.updateSyncDate(newSyncDate);
        
        // Assert
        assertEquals(newSyncDate, syncState.getLastSyncDate());
        assertTrue(syncState.getUpdatedAt().isAfter(oldUpdatedAt) || 
                   syncState.getUpdatedAt().equals(oldUpdatedAt));
    }
    
    @Test
    @DisplayName("throw exception when new sync date is before last sync date")
    void testUpdateSyncDate_DateBeforeLastSync() {
        // Arrange
        Instant recentDate = Instant.now().minus(1, ChronoUnit.HOURS);
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, recentDate);
        Instant olderDate = recentDate.minus(2, ChronoUnit.HOURS);
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> syncState.updateSyncDate(olderDate)
        );
        
        assertEquals("New sync date cannot be before last sync date", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when updating with null date")
    void testUpdateSyncDate_NullDate() {
        // Arrange
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, validSyncDate);
        
        // Act y Assert
        assertThrows(NullPointerException.class, () -> syncState.updateSyncDate(null));
    }
    
    @Test
    @DisplayName("throw exception when updating with future date")
    void testUpdateSyncDate_FutureDate() {
        // Arrange
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, validSyncDate);
        Instant futureDate = Instant.now().plus(2, ChronoUnit.HOURS);
        
        // Act y Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> syncState.updateSyncDate(futureDate)
        );
        
        assertEquals("Sync date cannot be in the future", exception.getMessage());
    }
    

    @Test
    @DisplayName("mark sync as started")
    void testMarkSyncStarted_UpdatesTimestamp() {
        // Arrange
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, validSyncDate);
        Instant oldUpdatedAt = syncState.getUpdatedAt();
        
        // Act
        syncState.markSyncStarted();
        
        // Assert
        assertTrue(syncState.getUpdatedAt().isAfter(oldUpdatedAt) || 
                   syncState.getUpdatedAt().equals(oldUpdatedAt));
    }
    
    
    @Test
    @DisplayName("return true for initial sync")
    void testIsFirstSync_InitialSync() {
        // Arrange
        SyncState syncState = SyncState.createInitialProductSync(validEnterpriseId);
        
        // Act y Assert
        assertTrue(syncState.isFirstSync());
    }
    
    @Test
    @DisplayName("return false when sync date is not default")
    void testIsFirstSync_AfterSync() {
        // Arrange
        Instant recentDate = Instant.now().minus(1, ChronoUnit.DAYS);
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, recentDate);
        
        // Act y Assert
        assertFalse(syncState.isFirstSync());
    }
    
    @Test
    @DisplayName("return true when last sync date is null")
    void testIsFirstSync_NullLastSyncDate() {
        // Arrange
        SyncState syncState = SyncState.builder()
            .syncType("products")
            .enterpriseId(validEnterpriseId)
            .lastSyncDate(null)
            .build();
        
        // Act y Assert
        assertTrue(syncState.isFirstSync());
    }
    
    @Test
    @DisplayName("return true when sync is outdated")
    void testIsOutdated_OldSyncDate() {
        // Arrange
        Instant oldDate = Instant.now().minus(10, ChronoUnit.DAYS);
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, oldDate);
        Instant threshold = Instant.now().minus(5, ChronoUnit.DAYS);
        
        // Act y Assert
        assertTrue(syncState.isOutdated(threshold));
    }
    
    @Test
    @DisplayName("return false when sync is recent")
    void testIsOutdated_RecentSyncDate() {
        // Arrange
        Instant recentDate = Instant.now().minus(1, ChronoUnit.HOURS);
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, recentDate);
        Instant threshold = Instant.now().minus(5, ChronoUnit.DAYS);
        
        // Act y Assert
        assertFalse(syncState.isOutdated(threshold));
    }
    
    @Test
    @DisplayName("return true when last sync date is null")
    void testIsOutdated_NullLastSyncDate() {
        // Arrange
        SyncState syncState = SyncState.builder()
            .syncType("products")
            .enterpriseId(validEnterpriseId)
            .lastSyncDate(null)
            .build();
        Instant threshold = Instant.now();
        
        // Act y Assert
        assertTrue(syncState.isOutdated(threshold));
    }
    

    @Test
    @DisplayName("return true when sync is needed after specified minutes")
    void testNeedsSyncAfterMinutes_OldSync() {
        // Arrange
        Instant oldDate = Instant.now().minus(65, ChronoUnit.MINUTES);
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, oldDate);
        
        // Act & Assert
        assertTrue(syncState.needsSyncAfterMinutes(60));
    }
    
    @Test
    @DisplayName("Should return false when sync is recent")
    void testNeedsSyncAfterMinutes_RecentSync() {
        // Arrange
        Instant recentDate = Instant.now().minus(30, ChronoUnit.MINUTES);
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, recentDate);
        
        // Act y Assert
        assertFalse(syncState.needsSyncAfterMinutes(60));
    }
    
    @Test
    @DisplayName("return true when last sync date is null")
    void testNeedsSyncAfterMinutes_NullLastSyncDate() {
        // Arrange
        SyncState syncState = SyncState.builder()
            .syncType("products")
            .enterpriseId(validEnterpriseId)
            .lastSyncDate(null)
            .build();
        
        // Act y Assert
        assertTrue(syncState.needsSyncAfterMinutes(60));
    }
    

    @Test
    @DisplayName("calculate minutes since last sync correctly")
    void testGetMinutesSinceLastSync_WithLastSync() {
        // Arrange
        Instant syncDate = Instant.now().minus(120, ChronoUnit.MINUTES);
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, syncDate);
        
        // Act
        long minutes = syncState.getMinutesSinceLastSync();
        
        // Assert
        assertTrue(minutes >= 119 && minutes <= 121); 
    }
    
    @Test
    @DisplayName("return max value when last sync date is null")
    void testGetMinutesSinceLastSync_NullLastSyncDate_ReturnsMaxValue() {
        // Arrange
        SyncState syncState = SyncState.builder()
            .syncType("products")
            .enterpriseId(validEnterpriseId)
            .lastSyncDate(null)
            .build();
        
        // Act y Assert
        assertEquals(Long.MAX_VALUE, syncState.getMinutesSinceLastSync());
    }
    
 
    @Test
    @DisplayName("calculate minutes since creation correctly")
    void testGetMinutesSinceCreation_WithCreatedAt_ReturnsPositiveValue() {
        // Arrange
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, validSyncDate);
        
        // Act
        long minutes = syncState.getMinutesSinceCreation();
        
        // Assert
        assertTrue(minutes >= 0);
    }
    
    @Test
    @DisplayName("return zero when created at is null")
    void testGetMinutesSinceCreation_NullCreatedAt() {
        // Arrange
        SyncState syncState = SyncState.builder()
            .syncType("products")
            .enterpriseId(validEnterpriseId)
            .createdAt(null)
            .build();
        
        // Act y Assert
        assertEquals(0, syncState.getMinutesSinceCreation());
    }
    

    @Test
    @DisplayName("return true for product sync type")
    void testIsProductSync_ProductType() {
        // Arrange
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, validSyncDate);
        
        // Act & Assert
        assertTrue(syncState.isProductSync());
    }
    
    @Test
    @DisplayName("return false for non-product sync type")
    void testIsProductSync_DifferentType() {
        // Arrange
        SyncState syncState = SyncState.builder()
            .syncType("orders")
            .enterpriseId(validEnterpriseId)
            .build();
        
        // Act y Assert
        assertFalse(syncState.isProductSync());
    }
    

    @Test
    @DisplayName("return true when updated recently")
    void testWasUpdatedInLastMinutes_RecentUpdate() {
        // Arrange
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, validSyncDate);
        
        // Act & Assert
        assertTrue(syncState.wasUpdatedInLastMinutes(5));
    }
    
    @Test
    @DisplayName("return false when updated long ago")
    void testWasUpdatedInLastMinutes_OldUpdate_ReturnsFalse() {
        // Arrange
        SyncState syncState = SyncState.builder()
            .syncType("products")
            .enterpriseId(validEnterpriseId)
            .updatedAt(Instant.now().minus(120, ChronoUnit.MINUTES))
            .build();
        
        // Act y Assert
        assertFalse(syncState.wasUpdatedInLastMinutes(60));
    }
    
    @Test
    @DisplayName("return false when updated at is null")
    void testWasUpdatedInLastMinutes_NullUpdatedAt() {
        // Arrange
        SyncState syncState = SyncState.builder()
            .syncType("products")
            .enterpriseId(validEnterpriseId)
            .updatedAt(null)
            .build();
        
        // Act y Assert
        assertFalse(syncState.wasUpdatedInLastMinutes(60));
    }
    
 
    @Test
    @DisplayName("validate successfully for update")
    void testValidateForUpdate_ValidState() {
        // Arrange
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, validSyncDate);
        
        // Act & Assert
        assertDoesNotThrow(() -> syncState.validateForUpdate());
    }
    
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t"})
    @DisplayName("throw exception when enterprise ID is invalid for update")
    void testValidateForUpdate_InvalidEnterpriseId(String invalidEnterpriseId) {
        // Arrange
        SyncState syncState = SyncState.builder()
            .syncType("products")
            .enterpriseId(invalidEnterpriseId)
            .build();
        
        // Act y Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> syncState.validateForUpdate()
        );
        
        assertEquals("Cannot update sync state without enterprise ID", exception.getMessage());
    }
    
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t"})
    @DisplayName("throw exception when sync type is invalid for update")
    void testValidateForUpdate_InvalidSyncType(String invalidSyncType) {
        // Arrange
        SyncState syncState = SyncState.builder()
            .syncType(invalidSyncType)
            .enterpriseId(validEnterpriseId)
            .build();
        
        // Act y Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> syncState.validateForUpdate()
        );
        
        assertEquals("Cannot update sync state without sync type", exception.getMessage());
    }
    
    
    @Test
    @DisplayName("throw exception when updated date is before created date")
    void testValidateDateConsistency_UpdatedBeforeCreated() {
        // Arrange
        Instant createdAt = Instant.now();
        Instant updatedAt = createdAt.minus(1, ChronoUnit.HOURS);
        
        SyncState syncState = SyncState.builder()
            .syncType("products")
            .enterpriseId(validEnterpriseId)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
        
        // Act y Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> syncState.validateDateConsistency()
        );
        
        assertEquals("Updated date cannot be before created date", exception.getMessage());
    }
    
    @Test
    @DisplayName("throw exception when last sync date is before created date")
    void testValidateDateConsistency_LastSyncBeforeCreated() {
        // Arrange
        Instant createdAt = Instant.now();
        Instant lastSyncDate = createdAt.minus(1, ChronoUnit.HOURS);
        
        SyncState syncState = SyncState.builder()
            .syncType("products")
            .enterpriseId(validEnterpriseId)
            .createdAt(createdAt)
            .lastSyncDate(lastSyncDate)
            .build();
        
        // Act y Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> syncState.validateDateConsistency()
        );
        
        assertEquals("Last sync date cannot be before created date", exception.getMessage());
    }
    
 
    @Test
    @DisplayName("reset to initial state")
    void testResetToInitialState_ResetsDateToDefault() {
        // Arrange
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, validSyncDate);
        Instant beforeReset = Instant.now();
        
        // Act
        syncState.resetToInitialState();
        
        // Assert
        assertAll("Reset validation",
            () -> assertEquals(Instant.parse("2000-01-01T00:00:00Z"), syncState.getLastSyncDate()),
            () -> assertTrue(syncState.getUpdatedAt().isAfter(beforeReset.minusSeconds(1))),
            () -> assertTrue(syncState.isFirstSync())
        );
    }
    

    @Test
    @DisplayName("be equal when same instance")
    void testEquals_SameInstance_ReturnsTrue() {
        // Arrange
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, validSyncDate);
        
        // Act y Assert
        assertEquals(syncState, syncState);
    }
    
    @Test
    @DisplayName("be equal when same ID")
    void testEquals_SameId_ReturnsTrue() {
        // Arrange
        SyncState syncState1 = SyncState.builder()
            .id(1L)
            .syncType("products")
            .enterpriseId("ENT-001")
            .build();
        
        SyncState syncState2 = SyncState.builder()
            .id(1L)
            .syncType("orders")
            .enterpriseId("ENT-002")
            .build();
        
        // Act y Assert
        assertEquals(syncState1, syncState2);
    }
    
    @Test
    @DisplayName("not be equal when different IDs")
    void testEquals_DifferentIds_ReturnsFalse() {
        // Arrange
        SyncState syncState1 = SyncState.builder().id(1L).build();
        SyncState syncState2 = SyncState.builder().id(2L).build();
        
        // Act y Assert
        assertNotEquals(syncState1, syncState2);
    }
    
    @Test
    @DisplayName("not be equal to null")
    void testEquals_NullObject_ReturnsFalse() {
        // Arrange
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, validSyncDate);
        
        // Act & Assert
        assertNotEquals(syncState, null);
    }

    @Test
    @DisplayName("return same hashCode for equal objects")
    void testHashCode_EqualObjects_ReturnsSameHash() {
        // Arrange
        SyncState syncState1 = SyncState.builder().id(1L).build();
        SyncState syncState2 = SyncState.builder().id(1L).build();
        
        // Act y Assert
        assertEquals(syncState1.hashCode(), syncState2.hashCode());
    }
    
    @Test
    @DisplayName("return consistent hashCode")
    void testHashCode_MultipleInvocations_ReturnsConsistent() {
        // Arrange
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, validSyncDate);
        syncState.setId(1L);
        
        // Act
        int hash1 = syncState.hashCode();
        int hash2 = syncState.hashCode();
        int hash3 = syncState.hashCode();
        
        // Assert
        assertAll("HashCode consistency",
            () -> assertEquals(hash1, hash2),
            () -> assertEquals(hash2, hash3)
        );
    }
    
    @Test
    @DisplayName("return formatted string with all details")
    void testToString_ValidState_ReturnsFormattedString() {
        // Arrange
        SyncState syncState = SyncState.createForProductSync(validEnterpriseId, validSyncDate);
        syncState.setId(10L);
        
        // Act
        String result = syncState.toString();
        
        // Assert
        assertAll("ToString validation",
            () -> assertTrue(result.contains("id=10")),
            () -> assertTrue(result.contains("type='products'")),
            () -> assertTrue(result.contains("enterpriseId='ENT-001'")),
            () -> assertTrue(result.contains("lastSync=")),
            () -> assertTrue(result.contains("minutesSinceLastSync=")),
            () -> assertTrue(result.startsWith("SyncState{"))
        );
    }
    

    @Test
    @DisplayName("Screate sync state using builder")
    void testBuilder_ValidValues_CreatesSyncState() {
        // Arrange y Act
        SyncState syncState = SyncState.builder()
            .id(1L)
            .syncType("products")
            .enterpriseId("ENT-BUILDER")
            .lastSyncDate(validSyncDate)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        
        // Assert
        assertAll("Builder validation",
            () -> assertEquals(1L, syncState.getId()),
            () -> assertEquals("products", syncState.getSyncType()),
            () -> assertEquals("ENT-BUILDER", syncState.getEnterpriseId()),
            () -> assertEquals(validSyncDate, syncState.getLastSyncDate())
        );
    }
    
}
