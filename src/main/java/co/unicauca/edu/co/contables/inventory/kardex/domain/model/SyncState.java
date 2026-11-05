package co.unicauca.edu.co.contables.inventory.kardex.domain.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @brief Domain model for tracking synchronization state
 * 
 */
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class SyncState {

    private Long id;
    
    private String syncType;
    
    private String enterpriseId;
    
    private Instant lastSyncDate;
    
    private Instant createdAt;
    
    private Instant updatedAt;
}
