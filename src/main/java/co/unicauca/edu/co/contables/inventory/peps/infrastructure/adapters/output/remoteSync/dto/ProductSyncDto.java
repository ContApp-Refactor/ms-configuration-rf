package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for product synchronization
 * 
 * Represents the product data structure received from the external Product Service.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductSyncDto {
    private Long productId;
    private String name;
    private String reference;
    private String enterpriseId;
    private String presentation;
    private boolean state;
    
}
