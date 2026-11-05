package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.adapter;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.SyncState;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.external.ISyncStateRepositoryPort;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.entity.SyncStateEntity;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.mapper.ISyncStateEntityMapper;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.repository.ISyncStateRepository;

import lombok.RequiredArgsConstructor;

/**
 * @brief JPA adapter for SyncState operations
 * 
 * Manages synchronization state tracking for enterprise data,
 * storing last sync timestamps for different sync types.
 */
@Repository("kardexSyncStatePortAdapter")@RequiredArgsConstructor
public class SyncStatePortAdapter implements ISyncStateRepositoryPort{

    private final ISyncStateRepository syncStateRepository;
    private final ISyncStateEntityMapper syncStateEntityMapper;

    /**
     * @brief Finds sync state by type and enterprise ID
     * @param syncType The type of synchronization
     * @param enterpriseId The enterprise identifier
     * @return Optional sync state if found
     */
    @Override
    public Optional<SyncState> findBySyncTypeAndEnterpriseId(String syncType, String enterpriseId) {
        Optional<SyncStateEntity> entity = syncStateRepository.findBySyncTypeAndEnterpriseId(syncType, enterpriseId);
        return entity.map(syncStateEntityMapper::toDomain);
    }

    /**
     * @brief Gets the last sync timestamp for a specific sync type and enterprise
     * @param syncType The type of synchronization
     * @param enterpriseId The enterprise identifier
     * @return Optional timestamp of last sync
     */
    @Override
    public Optional<Instant> findLastSyncFor(String syncType, String enterpriseId) {
        Optional<Instant> lastSync = syncStateRepository.findLastSyncFor(syncType, enterpriseId);
        return lastSync;
    }

    /**
     * @brief Saves or updates sync state
     * @param syncState The sync state to persist
     */
    @Override
    public void save(SyncState syncState) {
        SyncStateEntity entity = syncStateEntityMapper.toEntity(syncState);
        syncStateRepository.save(entity);
    }       
}
    

