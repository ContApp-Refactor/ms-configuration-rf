package co.unicauca.edu.co.contables.inventory.stock.domain.port;

import java.time.Instant;
import java.util.Optional;

import co.unicauca.edu.co.contables.inventory.stock.domain.model.SyncState;

/**
 * @brief Output port for synchronization state management
 * 
 * Manages tracking of synchronization timestamps for different
 * data types and enterprises to enable incremental sync operations.
 */
public interface ISyncStateRepositoryPort {
     /**
      * @brief Finds sync state by type and enterprise
      * @param syncType Type of synchronization operation
      * @param enterpriseId Enterprise identifier
      * @return Optional sync state if found
      */
     Optional<SyncState> findBySyncTypeAndEnterpriseId(String syncType, String enterpriseId);
     
     /**
      * @brief Gets last sync timestamp for specific sync type and enterprise
      * @param syncType Type of synchronization operation
      * @param enterpriseId Enterprise identifier
      * @return Optional timestamp of last sync operation
      */
     Optional<Instant> findLastSyncFor(String syncType, String enterpriseId);
     
     /**
      * @brief Saves or updates sync state
      * @param syncState Sync state to persist
      */
     void save(SyncState syncState);
}
