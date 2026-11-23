package co.unicauca.edu.co.contables.inventory.peps.unit.infrastucture.output.jpa.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.inventory.stock.domain.model.SyncState;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.adapter.SyncStatePortAdapter;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.entity.SyncStateEntity;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.mapper.ISyncStateEntityMapper;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.repository.ISyncStateRepository;

@ExtendWith(MockitoExtension.class)
public class SyncStatePortAdapterUnitTest {
    

    @Mock
    private ISyncStateRepository syncStateRepository;
    
    @Mock
    private ISyncStateEntityMapper syncStateEntityMapper;

    @InjectMocks
    private SyncStatePortAdapter syncStatePortAdapter;

    private String syncType;
    private String enterpriseId;
    private SyncState mockSyncState;
    private SyncStateEntity mockSyncStateEntity;
    private Instant mockInstant;

    @BeforeEach
    void setUp() {
        // Arrange - Common setup
        syncType = "products";
        enterpriseId = "ENT-001";
        mockInstant = Instant.now();
        
        mockSyncState = SyncState.builder()
            .id(1L)
            .syncType(syncType)
            .enterpriseId(enterpriseId)
            .lastSyncDate(mockInstant)
            .createdAt(mockInstant)
            .updatedAt(mockInstant)
            .build();

        mockSyncStateEntity = new SyncStateEntity();
        mockSyncStateEntity.setId(1L);
        mockSyncStateEntity.setSyncType(syncType);
        mockSyncStateEntity.setEnterpriseId(enterpriseId);
        mockSyncStateEntity.setLastSyncDate(mockInstant);
    }

    // ==================== findBySyncTypeAndEnterpriseId() ====================
    @Test
    @DisplayName("Should find sync state by type and enterprise ID when exists")
    void testFindBySyncTypeAndEnterpriseId_StateExists_ReturnsOptionalWithState() {
        // Arrange
        when(syncStateRepository.findBySyncTypeAndEnterpriseId(syncType, enterpriseId))
            .thenReturn(Optional.of(mockSyncStateEntity));
        when(syncStateEntityMapper.toDomain(mockSyncStateEntity))
            .thenReturn(mockSyncState);

        // Act
        Optional<SyncState> result = syncStatePortAdapter.findBySyncTypeAndEnterpriseId(syncType, enterpriseId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(syncType, result.get().getSyncType());
        assertEquals(enterpriseId, result.get().getEnterpriseId());
        assertEquals(mockInstant, result.get().getLastSyncDate());
        verify(syncStateRepository).findBySyncTypeAndEnterpriseId(syncType, enterpriseId);
        verify(syncStateEntityMapper).toDomain(mockSyncStateEntity);
    }

    @Test
    @DisplayName("Should return empty optional when sync state not found")
    void testFindBySyncTypeAndEnterpriseId_StateNotFound_ReturnsEmpty() {
        // Arrange
        when(syncStateRepository.findBySyncTypeAndEnterpriseId(syncType, enterpriseId))
            .thenReturn(Optional.empty());

        // Act
        Optional<SyncState> result = syncStatePortAdapter.findBySyncTypeAndEnterpriseId(syncType, enterpriseId);

        // Assert
        assertFalse(result.isPresent());
        verify(syncStateRepository).findBySyncTypeAndEnterpriseId(syncType, enterpriseId);
        verify(syncStateEntityMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should handle null sync type")
    void testFindBySyncTypeAndEnterpriseId_NullSyncType_CallsRepository() {
        // Arrange
        when(syncStateRepository.findBySyncTypeAndEnterpriseId(null, enterpriseId))
            .thenReturn(Optional.empty());

        // Act
        Optional<SyncState> result = syncStatePortAdapter.findBySyncTypeAndEnterpriseId(null, enterpriseId);

        // Assert
        assertFalse(result.isPresent());
        verify(syncStateRepository).findBySyncTypeAndEnterpriseId(null, enterpriseId);
    }

    @Test
    @DisplayName("Should handle null enterprise ID")
    void testFindBySyncTypeAndEnterpriseId_NullEnterpriseId_CallsRepository() {
        // Arrange
        when(syncStateRepository.findBySyncTypeAndEnterpriseId(syncType, null))
            .thenReturn(Optional.empty());

        // Act
        Optional<SyncState> result = syncStatePortAdapter.findBySyncTypeAndEnterpriseId(syncType, null);

        // Assert
        assertFalse(result.isPresent());
        verify(syncStateRepository).findBySyncTypeAndEnterpriseId(syncType, null);
    }

    @Test
    @DisplayName("Should handle different sync types")
    void testFindBySyncTypeAndEnterpriseId_DifferentSyncTypes_FindsCorrectly() {
        // Arrange
        String kardexSyncType = "kardex";
        SyncStateEntity kardexEntity = new SyncStateEntity();
        kardexEntity.setSyncType(kardexSyncType);
        kardexEntity.setEnterpriseId(enterpriseId);
        
        SyncState kardexState = SyncState.builder()
            .syncType(kardexSyncType)
            .enterpriseId(enterpriseId)
            .build();
        
        when(syncStateRepository.findBySyncTypeAndEnterpriseId(kardexSyncType, enterpriseId))
            .thenReturn(Optional.of(kardexEntity));
        when(syncStateEntityMapper.toDomain(kardexEntity))
            .thenReturn(kardexState);

        // Act
        Optional<SyncState> result = syncStatePortAdapter.findBySyncTypeAndEnterpriseId(kardexSyncType, enterpriseId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(kardexSyncType, result.get().getSyncType());
    }

    // ==================== findLastSyncFor() ====================
    @Test
    @DisplayName("Should find last sync date when exists")
    void testFindLastSyncFor_DateExists_ReturnsOptionalWithInstant() {
        // Arrange
        when(syncStateRepository.findLastSyncFor(syncType, enterpriseId))
            .thenReturn(Optional.of(mockInstant));

        // Act
        Optional<Instant> result = syncStatePortAdapter.findLastSyncFor(syncType, enterpriseId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockInstant, result.get());
        verify(syncStateRepository).findLastSyncFor(syncType, enterpriseId);
    }

    @Test
    @DisplayName("Should return empty optional when no last sync date found")
    void testFindLastSyncFor_NoDate_ReturnsEmpty() {
        // Arrange
        when(syncStateRepository.findLastSyncFor(syncType, enterpriseId))
            .thenReturn(Optional.empty());

        // Act
        Optional<Instant> result = syncStatePortAdapter.findLastSyncFor(syncType, enterpriseId);

        // Assert
        assertFalse(result.isPresent());
        verify(syncStateRepository).findLastSyncFor(syncType, enterpriseId);
    }

    @Test
    @DisplayName("Should handle null parameters in findLastSyncFor")
    void testFindLastSyncFor_NullParameters_CallsRepository() {
        // Arrange
        when(syncStateRepository.findLastSyncFor(null, null))
            .thenReturn(Optional.empty());

        // Act
        Optional<Instant> result = syncStatePortAdapter.findLastSyncFor(null, null);

        // Assert
        assertFalse(result.isPresent());
        verify(syncStateRepository).findLastSyncFor(null, null);
    }

    @Test
    @DisplayName("Should return correct instant for different enterprises")
    void testFindLastSyncFor_DifferentEnterprises_ReturnsCorrectInstant() {
        // Arrange
        String enterprise2 = "ENT-002";
        Instant differentInstant = Instant.now().minusSeconds(3600);
        
        when(syncStateRepository.findLastSyncFor(syncType, enterprise2))
            .thenReturn(Optional.of(differentInstant));

        // Act
        Optional<Instant> result = syncStatePortAdapter.findLastSyncFor(syncType, enterprise2);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(differentInstant, result.get());
        assertNotEquals(mockInstant, result.get());
    }

    // ==================== save() ====================
    @Test
    @DisplayName("Should save sync state successfully")
    void testSave_ValidSyncState_SavesSuccessfully() {
        // Arrange
        when(syncStateEntityMapper.toEntity(mockSyncState))
            .thenReturn(mockSyncStateEntity);
        when(syncStateRepository.save(mockSyncStateEntity))
            .thenReturn(mockSyncStateEntity);

        // Act
        syncStatePortAdapter.save(mockSyncState);

        // Assert
        verify(syncStateEntityMapper).toEntity(mockSyncState);
        verify(syncStateRepository).save(mockSyncStateEntity);
    }

    @Test
    @DisplayName("Should convert domain to entity before saving")
    void testSave_ValidState_ConvertsToEntity() {
        // Arrange
        when(syncStateEntityMapper.toEntity(mockSyncState))
            .thenReturn(mockSyncStateEntity);
        when(syncStateRepository.save(mockSyncStateEntity))
            .thenReturn(mockSyncStateEntity);

        // Act
        syncStatePortAdapter.save(mockSyncState);

        // Assert
        ArgumentCaptor<SyncState> domainCaptor = ArgumentCaptor.forClass(SyncState.class);
        verify(syncStateEntityMapper).toEntity(domainCaptor.capture());
        
        SyncState capturedDomain = domainCaptor.getValue();
        assertEquals(mockSyncState.getSyncType(), capturedDomain.getSyncType());
        assertEquals(mockSyncState.getEnterpriseId(), capturedDomain.getEnterpriseId());
    }

    @Test
    @DisplayName("Should save entity with all fields")
    void testSave_CompleteState_SavesAllFields() {
        // Arrange
        when(syncStateEntityMapper.toEntity(mockSyncState))
            .thenReturn(mockSyncStateEntity);
        when(syncStateRepository.save(any(SyncStateEntity.class)))
            .thenReturn(mockSyncStateEntity);

        // Act
        syncStatePortAdapter.save(mockSyncState);

        // Assert
        ArgumentCaptor<SyncStateEntity> entityCaptor = ArgumentCaptor.forClass(SyncStateEntity.class);
        verify(syncStateRepository).save(entityCaptor.capture());
        
        SyncStateEntity capturedEntity = entityCaptor.getValue();
        assertNotNull(capturedEntity);
        assertEquals(mockSyncStateEntity.getSyncType(), capturedEntity.getSyncType());
        assertEquals(mockSyncStateEntity.getEnterpriseId(), capturedEntity.getEnterpriseId());
    }

    @Test
    @DisplayName("Should save new sync state without ID")
    void testSave_NewState_SavesWithoutId() {
        // Arrange
        SyncState newState = SyncState.builder()
            .syncType("new_type")
            .enterpriseId("ENT-003")
            .lastSyncDate(Instant.now())
            .build();
        
        SyncStateEntity newEntity = new SyncStateEntity();
        newEntity.setSyncType("new_type");
        newEntity.setEnterpriseId("ENT-003");
        
        when(syncStateEntityMapper.toEntity(newState))
            .thenReturn(newEntity);
        when(syncStateRepository.save(newEntity))
            .thenReturn(newEntity);

        // Act
        syncStatePortAdapter.save(newState);

        // Assert
        verify(syncStateRepository).save(newEntity);
    }

    @Test
    @DisplayName("Should update existing sync state")
    void testSave_ExistingState_Updates() {
        // Arrange
        SyncState existingState = SyncState.builder()
            .id(5L)
            .syncType(syncType)
            .enterpriseId(enterpriseId)
            .lastSyncDate(Instant.now())
            .build();
        
        SyncStateEntity existingEntity = new SyncStateEntity();
        existingEntity.setId(5L);
        existingEntity.setSyncType(syncType);
        
        when(syncStateEntityMapper.toEntity(existingState))
            .thenReturn(existingEntity);
        when(syncStateRepository.save(existingEntity))
            .thenReturn(existingEntity);

        // Act
        syncStatePortAdapter.save(existingState);

        // Assert
        ArgumentCaptor<SyncStateEntity> captor = ArgumentCaptor.forClass(SyncStateEntity.class);
        verify(syncStateRepository).save(captor.capture());
        assertEquals(5L, captor.getValue().getId());
    }

    // ==================== Integration Scenarios ====================
    @Test
    @DisplayName("Should handle complete workflow: find, update, save")
    void testCompleteWorkflow_FindUpdateSave_WorksCorrectly() {
        // Arrange
        when(syncStateRepository.findBySyncTypeAndEnterpriseId(syncType, enterpriseId))
            .thenReturn(Optional.of(mockSyncStateEntity));
        when(syncStateEntityMapper.toDomain(mockSyncStateEntity))
            .thenReturn(mockSyncState);
        when(syncStateEntityMapper.toEntity(mockSyncState))
            .thenReturn(mockSyncStateEntity);
        when(syncStateRepository.save(mockSyncStateEntity))
            .thenReturn(mockSyncStateEntity);

        // Act
        Optional<SyncState> found = syncStatePortAdapter.findBySyncTypeAndEnterpriseId(syncType, enterpriseId);
        assertTrue(found.isPresent());
        
        SyncState state = found.get();
        state.updateSyncDate(Instant.now());
        
        syncStatePortAdapter.save(state);

        // Assert
        verify(syncStateRepository).findBySyncTypeAndEnterpriseId(syncType, enterpriseId);
        verify(syncStateRepository).save(any(SyncStateEntity.class));
    }

    @Test
    @DisplayName("Should handle multiple saves")
    void testSave_MultipleSaves_AllSucceed() {
        // Arrange
        when(syncStateEntityMapper.toEntity(any(SyncState.class)))
            .thenReturn(mockSyncStateEntity);
        when(syncStateRepository.save(any(SyncStateEntity.class)))
            .thenReturn(mockSyncStateEntity);

        // Act
        syncStatePortAdapter.save(mockSyncState);
        syncStatePortAdapter.save(mockSyncState);
        syncStatePortAdapter.save(mockSyncState);

        // Assert
        verify(syncStateRepository, times(3)).save(any(SyncStateEntity.class));
    }

    // ==================== Edge Cases ====================
    @Test
    @DisplayName("Should handle empty string parameters")
    void testFindBySyncTypeAndEnterpriseId_EmptyStrings_CallsRepository() {
        // Arrange
        when(syncStateRepository.findBySyncTypeAndEnterpriseId("", ""))
            .thenReturn(Optional.empty());

        // Act
        Optional<SyncState> result = syncStatePortAdapter.findBySyncTypeAndEnterpriseId("", "");

        // Assert
        assertFalse(result.isPresent());
        verify(syncStateRepository).findBySyncTypeAndEnterpriseId("", "");
    }
}
