package co.unicauca.edu.co.contables.inventory.kardex.unit.infrastructure.adapters.output.jpa.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.SyncState;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.adapter.SyncStatePortAdapter;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.entity.SyncStateEntity;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.mapper.ISyncStateEntityMapper;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.repository.ISyncStateRepository;

/**
 * @brief Unit tests for SyncStatePortAdapter
 * 
 * Tests the synchronization state management operations including
 * finding sync states, retrieving last sync timestamps, and saving.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SyncStatePortAdapter Tests")
class SyncStatePortAdapterUnitTest {

    @Mock
    private ISyncStateRepository syncStateRepository;

    @Mock
    private ISyncStateEntityMapper syncStateEntityMapper;

    @InjectMocks
    private SyncStatePortAdapter syncStatePortAdapter;

    private SyncState syncStateDomain;
    private SyncStateEntity syncStateEntity;
    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.now();

        // Create test sync state domain object
        syncStateDomain = new SyncState();
        syncStateDomain.setId(1L);
        syncStateDomain.setSyncType("PRODUCT_SYNC");
        syncStateDomain.setEnterpriseId("ENT-001");
        syncStateDomain.setLastSyncDate(now);
        syncStateDomain.setCreatedAt(now.minusSeconds(3600));
        syncStateDomain.setUpdatedAt(now);

        // Create test sync state entity
        syncStateEntity = new SyncStateEntity();
        syncStateEntity.setId(1L);
        syncStateEntity.setSyncType("PRODUCT_SYNC");
        syncStateEntity.setEnterpriseId("ENT-001");
        syncStateEntity.setLastSyncDate(now);
        syncStateEntity.setCreatedAt(now.minusSeconds(3600));
        syncStateEntity.setUpdatedAt(now);
    }

    @Test
    @DisplayName("Should find sync state by sync type and enterprise ID successfully")
    void testFindBySyncTypeAndEnterpriseId_Success() {
        // Arrange
        when(syncStateRepository.findBySyncTypeAndEnterpriseId("PRODUCT_SYNC", "ENT-001"))
            .thenReturn(Optional.of(syncStateEntity));
        when(syncStateEntityMapper.toDomain(syncStateEntity)).thenReturn(syncStateDomain);

        // Act
        Optional<SyncState> result = syncStatePortAdapter.findBySyncTypeAndEnterpriseId("PRODUCT_SYNC", "ENT-001");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("PRODUCT_SYNC", result.get().getSyncType());
        assertEquals("ENT-001", result.get().getEnterpriseId());
        assertEquals(now, result.get().getLastSyncDate());

        verify(syncStateRepository, times(1)).findBySyncTypeAndEnterpriseId("PRODUCT_SYNC", "ENT-001");
        verify(syncStateEntityMapper, times(1)).toDomain(syncStateEntity);
    }

    @Test
    @DisplayName("Should return empty optional when sync state not found")
    void testFindBySyncTypeAndEnterpriseId_NotFound() {
        // Arrange
        when(syncStateRepository.findBySyncTypeAndEnterpriseId("UNKNOWN_SYNC", "ENT-999"))
            .thenReturn(Optional.empty());

        // Act
        Optional<SyncState> result = syncStatePortAdapter.findBySyncTypeAndEnterpriseId("UNKNOWN_SYNC", "ENT-999");

        // Assert
        assertFalse(result.isPresent());

        verify(syncStateRepository, times(1)).findBySyncTypeAndEnterpriseId("UNKNOWN_SYNC", "ENT-999");
        verify(syncStateEntityMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should find last sync timestamp successfully")
    void testFindLastSyncFor_Success() {
        // Arrange
        Instant lastSync = Instant.now().minusSeconds(1800);
        when(syncStateRepository.findLastSyncFor("PRODUCT_SYNC", "ENT-001"))
            .thenReturn(Optional.of(lastSync));

        // Act
        Optional<Instant> result = syncStatePortAdapter.findLastSyncFor("PRODUCT_SYNC", "ENT-001");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(lastSync, result.get());

        verify(syncStateRepository, times(1)).findLastSyncFor("PRODUCT_SYNC", "ENT-001");
    }

    @Test
    @DisplayName("Should return empty optional when last sync not found")
    void testFindLastSyncFor_NotFound() {
        // Arrange
        when(syncStateRepository.findLastSyncFor("INVOICE_SYNC", "ENT-999"))
            .thenReturn(Optional.empty());

        // Act
        Optional<Instant> result = syncStatePortAdapter.findLastSyncFor("INVOICE_SYNC", "ENT-999");

        // Assert
        assertFalse(result.isPresent());

        verify(syncStateRepository, times(1)).findLastSyncFor("INVOICE_SYNC", "ENT-999");
    }

    @Test
    @DisplayName("Should save sync state successfully")
    void testSave_Success() {
        // Arrange
        when(syncStateEntityMapper.toEntity(syncStateDomain)).thenReturn(syncStateEntity);
        when(syncStateRepository.save(syncStateEntity)).thenReturn(syncStateEntity);

        // Act
        syncStatePortAdapter.save(syncStateDomain);

        // Assert
        verify(syncStateEntityMapper, times(1)).toEntity(syncStateDomain);
        verify(syncStateRepository, times(1)).save(syncStateEntity);
    }

    @Test
    @DisplayName("Should save sync state with correct attributes")
    void testSave_CorrectAttributes() {
        // Arrange
        when(syncStateEntityMapper.toEntity(syncStateDomain)).thenReturn(syncStateEntity);
        when(syncStateRepository.save(any(SyncStateEntity.class))).thenReturn(syncStateEntity);

        // Act
        syncStatePortAdapter.save(syncStateDomain);

        // Assert
        verify(syncStateRepository).save(argThat(entity ->
            entity.getSyncType().equals("PRODUCT_SYNC") &&
            entity.getEnterpriseId().equals("ENT-001") &&
            entity.getLastSyncDate().equals(now)
        ));
    }

    @Test
    @DisplayName("Should handle different sync types")
    void testFindBySyncTypeAndEnterpriseId_DifferentSyncTypes() {
        // Arrange
        String[] syncTypes = {"PRODUCT_SYNC", "INVOICE_SYNC", "KARDEX_SYNC", "STOCK_SYNC"};

        for (String syncType : syncTypes) {
            SyncStateEntity entity = new SyncStateEntity();
            entity.setSyncType(syncType);
            entity.setEnterpriseId("ENT-001");

            SyncState domain = new SyncState();
            domain.setSyncType(syncType);
            domain.setEnterpriseId("ENT-001");

            when(syncStateRepository.findBySyncTypeAndEnterpriseId(syncType, "ENT-001"))
                .thenReturn(Optional.of(entity));
            when(syncStateEntityMapper.toDomain(entity)).thenReturn(domain);
        }

        // Act & Assert
        for (String syncType : syncTypes) {
            Optional<SyncState> result = syncStatePortAdapter.findBySyncTypeAndEnterpriseId(syncType, "ENT-001");
            assertTrue(result.isPresent());
            assertEquals(syncType, result.get().getSyncType());
        }

        verify(syncStateRepository, times(syncTypes.length)).findBySyncTypeAndEnterpriseId(anyString(), eq("ENT-001"));
    }

    @Test
    @DisplayName("Should handle different enterprise IDs")
    void testFindBySyncTypeAndEnterpriseId_DifferentEnterprises() {
        // Arrange
        String[] enterpriseIds = {"ENT-001", "ENT-002", "ENT-003"};

        for (String enterpriseId : enterpriseIds) {
            SyncStateEntity entity = new SyncStateEntity();
            entity.setSyncType("PRODUCT_SYNC");
            entity.setEnterpriseId(enterpriseId);

            SyncState domain = new SyncState();
            domain.setSyncType("PRODUCT_SYNC");
            domain.setEnterpriseId(enterpriseId);

            when(syncStateRepository.findBySyncTypeAndEnterpriseId("PRODUCT_SYNC", enterpriseId))
                .thenReturn(Optional.of(entity));
            when(syncStateEntityMapper.toDomain(entity)).thenReturn(domain);
        }

        // Act & Assert
        for (String enterpriseId : enterpriseIds) {
            Optional<SyncState> result = syncStatePortAdapter.findBySyncTypeAndEnterpriseId("PRODUCT_SYNC", enterpriseId);
            assertTrue(result.isPresent());
            assertEquals(enterpriseId, result.get().getEnterpriseId());
        }

        verify(syncStateRepository, times(enterpriseIds.length)).findBySyncTypeAndEnterpriseId(eq("PRODUCT_SYNC"), anyString());
    }

    @Test
    @DisplayName("Should handle recent sync timestamps")
    void testFindLastSyncFor_RecentSync() {
        // Arrange
        Instant recentSync = Instant.now().minusSeconds(60); // 1 minute ago
        when(syncStateRepository.findLastSyncFor("PRODUCT_SYNC", "ENT-001"))
            .thenReturn(Optional.of(recentSync));

        // Act
        Optional<Instant> result = syncStatePortAdapter.findLastSyncFor("PRODUCT_SYNC", "ENT-001");

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().isAfter(Instant.now().minusSeconds(120)));
        assertTrue(result.get().isBefore(Instant.now()));
    }

    @Test
    @DisplayName("Should handle old sync timestamps")
    void testFindLastSyncFor_OldSync() {
        // Arrange
        Instant oldSync = Instant.now().minusSeconds(86400); // 1 day ago
        when(syncStateRepository.findLastSyncFor("PRODUCT_SYNC", "ENT-001"))
            .thenReturn(Optional.of(oldSync));

        // Act
        Optional<Instant> result = syncStatePortAdapter.findLastSyncFor("PRODUCT_SYNC", "ENT-001");

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().isBefore(Instant.now().minusSeconds(3600)));
    }

    @Test
    @DisplayName("Should save new sync state")
    void testSave_NewSyncState() {
        // Arrange
        SyncState newSyncState = new SyncState();
        newSyncState.setSyncType("NEW_SYNC");
        newSyncState.setEnterpriseId("ENT-NEW");
        newSyncState.setLastSyncDate(Instant.now());

        SyncStateEntity newEntity = new SyncStateEntity();
        newEntity.setSyncType("NEW_SYNC");
        newEntity.setEnterpriseId("ENT-NEW");

        when(syncStateEntityMapper.toEntity(newSyncState)).thenReturn(newEntity);
        when(syncStateRepository.save(newEntity)).thenReturn(newEntity);

        // Act
        syncStatePortAdapter.save(newSyncState);

        // Assert
        verify(syncStateEntityMapper).toEntity(newSyncState);
        verify(syncStateRepository).save(newEntity);
    }

    @Test
    @DisplayName("Should update existing sync state")
    void testSave_UpdateExisting() {
        // Arrange
        syncStateDomain.setLastSyncDate(Instant.now());
        syncStateEntity.setLastSyncDate(Instant.now());

        when(syncStateEntityMapper.toEntity(syncStateDomain)).thenReturn(syncStateEntity);
        when(syncStateRepository.save(syncStateEntity)).thenReturn(syncStateEntity);

        // Act
        syncStatePortAdapter.save(syncStateDomain);

        // Assert
        verify(syncStateEntityMapper).toEntity(syncStateDomain);
        verify(syncStateRepository).save(syncStateEntity);
    }

    @Test
    @DisplayName("Should map entity to domain correctly")
    void testFindBySyncTypeAndEnterpriseId_MappingCorrectness() {
        // Arrange
        when(syncStateRepository.findBySyncTypeAndEnterpriseId("PRODUCT_SYNC", "ENT-001"))
            .thenReturn(Optional.of(syncStateEntity));
        when(syncStateEntityMapper.toDomain(syncStateEntity)).thenReturn(syncStateDomain);

        // Act
        Optional<SyncState> result = syncStatePortAdapter.findBySyncTypeAndEnterpriseId("PRODUCT_SYNC", "ENT-001");

        // Assert
        assertTrue(result.isPresent());
        SyncState syncState = result.get();
        assertEquals(syncStateEntity.getId(), syncState.getId());
        assertEquals(syncStateEntity.getSyncType(), syncState.getSyncType());
        assertEquals(syncStateEntity.getEnterpriseId(), syncState.getEnterpriseId());
        assertEquals(syncStateEntity.getLastSyncDate(), syncState.getLastSyncDate());
        assertEquals(syncStateEntity.getCreatedAt(), syncState.getCreatedAt());
        assertEquals(syncStateEntity.getUpdatedAt(), syncState.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle multiple save operations")
    void testSave_MultipleSaves() {
        // Arrange
        when(syncStateEntityMapper.toEntity(any(SyncState.class))).thenReturn(syncStateEntity);
        when(syncStateRepository.save(any(SyncStateEntity.class))).thenReturn(syncStateEntity);

        // Act
        syncStatePortAdapter.save(syncStateDomain);
        syncStatePortAdapter.save(syncStateDomain);
        syncStatePortAdapter.save(syncStateDomain);

        // Assert
        verify(syncStateEntityMapper, times(3)).toEntity(any(SyncState.class));
        verify(syncStateRepository, times(3)).save(any(SyncStateEntity.class));
    }

    @Test
    @DisplayName("Should verify repository interaction in findBySyncTypeAndEnterpriseId")
    void testFindBySyncTypeAndEnterpriseId_RepositoryInteraction() {
        // Arrange
        when(syncStateRepository.findBySyncTypeAndEnterpriseId("PRODUCT_SYNC", "ENT-001"))
            .thenReturn(Optional.of(syncStateEntity));
        when(syncStateEntityMapper.toDomain(syncStateEntity)).thenReturn(syncStateDomain);

        // Act
        syncStatePortAdapter.findBySyncTypeAndEnterpriseId("PRODUCT_SYNC", "ENT-001");

        // Assert
        verify(syncStateRepository, times(1)).findBySyncTypeAndEnterpriseId("PRODUCT_SYNC", "ENT-001");
        verifyNoMoreInteractions(syncStateRepository);
    }

    @Test
    @DisplayName("Should verify repository interaction in findLastSyncFor")
    void testFindLastSyncFor_RepositoryInteraction() {
        // Arrange
        when(syncStateRepository.findLastSyncFor("PRODUCT_SYNC", "ENT-001"))
            .thenReturn(Optional.of(now));

        // Act
        syncStatePortAdapter.findLastSyncFor("PRODUCT_SYNC", "ENT-001");

        // Assert
        verify(syncStateRepository, times(1)).findLastSyncFor("PRODUCT_SYNC", "ENT-001");
        verifyNoMoreInteractions(syncStateRepository);
    }

    @Test
    @DisplayName("Should handle null sync type gracefully")
    void testFindBySyncTypeAndEnterpriseId_NullSyncType() {
        // Arrange
        when(syncStateRepository.findBySyncTypeAndEnterpriseId(null, "ENT-001"))
            .thenReturn(Optional.empty());

        // Act
        Optional<SyncState> result = syncStatePortAdapter.findBySyncTypeAndEnterpriseId(null, "ENT-001");

        // Assert
        assertFalse(result.isPresent());
        verify(syncStateRepository, times(1)).findBySyncTypeAndEnterpriseId(null, "ENT-001");
    }

    @Test
    @DisplayName("Should handle null enterprise ID gracefully")
    void testFindBySyncTypeAndEnterpriseId_NullEnterpriseId() {
        // Arrange
        when(syncStateRepository.findBySyncTypeAndEnterpriseId("PRODUCT_SYNC", null))
            .thenReturn(Optional.empty());

        // Act
        Optional<SyncState> result = syncStatePortAdapter.findBySyncTypeAndEnterpriseId("PRODUCT_SYNC", null);

        // Assert
        assertFalse(result.isPresent());
        verify(syncStateRepository, times(1)).findBySyncTypeAndEnterpriseId("PRODUCT_SYNC", null);
    }

    @Test
    @DisplayName("Should preserve timestamps when saving")
    void testSave_PreserveTimestamps() {
        // Arrange
        Instant createdAt = Instant.now().minusSeconds(7200);
        Instant updatedAt = Instant.now().minusSeconds(3600);
        Instant lastSync = Instant.now();

        syncStateDomain.setCreatedAt(createdAt);
        syncStateDomain.setUpdatedAt(updatedAt);
        syncStateDomain.setLastSyncDate(lastSync);

        syncStateEntity.setCreatedAt(createdAt);
        syncStateEntity.setUpdatedAt(updatedAt);
        syncStateEntity.setLastSyncDate(lastSync);

        when(syncStateEntityMapper.toEntity(syncStateDomain)).thenReturn(syncStateEntity);
        when(syncStateRepository.save(syncStateEntity)).thenReturn(syncStateEntity);

        // Act
        syncStatePortAdapter.save(syncStateDomain);

        // Assert
        verify(syncStateRepository).save(argThat(entity ->
            entity.getCreatedAt().equals(createdAt) &&
            entity.getUpdatedAt().equals(updatedAt) &&
            entity.getLastSyncDate().equals(lastSync)
        ));
    }

    @Test
    @DisplayName("Should find sync state with complex sync type names")
    void testFindBySyncTypeAndEnterpriseId_ComplexSyncTypes() {
        // Arrange
        String complexSyncType = "PRODUCT_INVENTORY_BATCH_SYNC_V2";
        SyncStateEntity entity = new SyncStateEntity();
        entity.setSyncType(complexSyncType);
        entity.setEnterpriseId("ENT-001");

        SyncState domain = new SyncState();
        domain.setSyncType(complexSyncType);
        domain.setEnterpriseId("ENT-001");

        when(syncStateRepository.findBySyncTypeAndEnterpriseId(complexSyncType, "ENT-001"))
            .thenReturn(Optional.of(entity));
        when(syncStateEntityMapper.toDomain(entity)).thenReturn(domain);

        // Act
        Optional<SyncState> result = syncStatePortAdapter.findBySyncTypeAndEnterpriseId(complexSyncType, "ENT-001");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(complexSyncType, result.get().getSyncType());
    }
}
