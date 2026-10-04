package co.unicauca.edu.co.contables.inventory.stock.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * @brief Domain model for tracking synchronization state
 *
 * Modelo unico para la tabla {@code sync_state}: lo comparten stock y peps
 * desde que inventory-peps-rf se fusiono en el monolito. Ademas de los
 * atributos, concentra las reglas de validacion de la sincronizacion de
 * productos que peps trae en su dominio.
 */
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SyncState {
     private Long id;
    
    private String syncType;
    
    private String enterpriseId;
    
    private Instant lastSyncDate;
    
    private Instant createdAt;
    
    private Instant updatedAt;



    //  Crear nuevo estado de sincronización para productos
    public static SyncState createForProductSync(String enterpriseId, Instant initialSyncDate) {
        validateEnterpriseId(enterpriseId);
        validateSyncDate(initialSyncDate);
        
        SyncState syncState = new SyncState();
        syncState.setSyncType("products");
        syncState.setEnterpriseId(enterpriseId);
        syncState.setLastSyncDate(initialSyncDate);
        syncState.setCreatedAt(Instant.now());
        syncState.setUpdatedAt(Instant.now());
        
        return syncState;
    }

    // Crear estado inicial por defecto (primera sincronización)
    public static SyncState createInitialProductSync(String enterpriseId) {
        Instant defaultDate = Instant.parse("2000-01-01T00:00:00Z");
        return createForProductSync(enterpriseId, defaultDate);
    }

    //Actualizar fecha de sincronización
    public void updateSyncDate(Instant newSyncDate) {
        validateSyncDate(newSyncDate);
        
        if (this.lastSyncDate != null && newSyncDate.isBefore(this.lastSyncDate)) {
            throw new IllegalArgumentException("New sync date cannot be before last sync date");
        }
        
        this.lastSyncDate = newSyncDate;
        this.updatedAt = Instant.now();
    }

    //Marcar como iniciado (para tracking)
    public void markSyncStarted() {
        this.updatedAt = Instant.now();
    }

    // Verificar si es una primera sincronización
    public boolean isFirstSync() {
        return this.lastSyncDate == null || 
               this.lastSyncDate.equals(Instant.parse("2000-01-01T00:00:00Z"));
    }

    //Verificar si la sincronización está desactualizada
    public boolean isOutdated(Instant threshold) {
        if (this.lastSyncDate == null) return true;
        return this.lastSyncDate.isBefore(threshold);
    }

    //  Verificar si necesita sincronización (más de X minutos)
    public boolean needsSyncAfterMinutes(long minutes) {
        if (this.lastSyncDate == null) return true;
        
        Instant threshold = Instant.now().minusSeconds(minutes * 60);
        return this.lastSyncDate.isBefore(threshold);
    }

    // Obtener tiempo transcurrido desde última sincronización
    public long getMinutesSinceLastSync() {
        if (this.lastSyncDate == null) return Long.MAX_VALUE;
        return (Instant.now().toEpochMilli() - this.lastSyncDate.toEpochMilli()) / (1000 * 60);
    }

    //  Obtener tiempo transcurrido desde creación
    public long getMinutesSinceCreation() {
        if (this.createdAt == null) return 0;
        return (Instant.now().toEpochMilli() - this.createdAt.toEpochMilli()) / (1000 * 60);
    }

    //Verificar si es para productos
    public boolean isProductSync() {
        return "products".equals(this.syncType);
    }

    //Verificar si fue actualizado recientemente
    public boolean wasUpdatedInLastMinutes(long minutes) {
        if (this.updatedAt == null) return false;
        
        Instant threshold = Instant.now().minusSeconds(minutes * 60);
        return this.updatedAt.isAfter(threshold);
    }

    /**
     * @brief Validates state before update
     */
    public void validateForUpdate() {
        if (this.enterpriseId == null || this.enterpriseId.trim().isEmpty()) {
            throw new IllegalStateException("Cannot update sync state without enterprise ID");
        }
        if (this.syncType == null || this.syncType.trim().isEmpty()) {
            throw new IllegalStateException("Cannot update sync state without sync type");
        }
    }

    /**
     * @brief Validates date consistency
     */
    public void validateDateConsistency() {
        if (this.createdAt != null && this.updatedAt != null && this.updatedAt.isBefore(this.createdAt)) {
            throw new IllegalStateException("Updated date cannot be before created date");
        }
        
        if (this.lastSyncDate != null && this.createdAt != null && this.lastSyncDate.isBefore(this.createdAt)) {
            throw new IllegalStateException("Last sync date cannot be before created date");
        }
    }

    /**
     * @brief Resets to initial state
     */
    public void resetToInitialState() {
        this.lastSyncDate = Instant.parse("2000-01-01T00:00:00Z");
        this.updatedAt = Instant.now();
    }

    // Métodos de validación privados y estáticos
    private static void validateEnterpriseId(String enterpriseId) {
        if (enterpriseId == null || enterpriseId.trim().isEmpty()) {
            throw new IllegalArgumentException("Enterprise ID cannot be empty");
        }
        if (enterpriseId.length() > 50) { // Asumiendo límite razonable
            throw new IllegalArgumentException("Enterprise ID is too long");
        }
    }

    private static void validateSyncDate(Instant syncDate) {
        Objects.requireNonNull(syncDate, "Sync date cannot be null");
        
        if (syncDate.isAfter(Instant.now().plusSeconds(60))) { // Permitir 1 minuto de diferencia por reloj
            throw new IllegalArgumentException("Sync date cannot be in the future");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SyncState syncState = (SyncState) o;
        return Objects.equals(id, syncState.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("SyncState{id=%d, type='%s', enterpriseId='%s', lastSync=%s, minutesSinceLastSync=%d}", 
            id, syncType, enterpriseId, lastSyncDate, getMinutesSinceLastSync());
    }

}
