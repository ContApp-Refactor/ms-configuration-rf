package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * @brief JPA entity for tracking synchronization state
 * 
 */
@Entity
@Table(name = "sync_state")
@Getter @Setter
public class SyncStateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sync_type", nullable = false)
    private String syncType;
    
    @Column(name = "enterprise_id", nullable = false)
    private String enterpriseId;
    
    @Column(name = "last_sync_date", nullable = false)
    private Instant lastSyncDate;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    /**
     * @brief Sets creation and update timestamps on entity creation
     */
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    /**
     * @brief Updates the modified timestamp on entity update
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
