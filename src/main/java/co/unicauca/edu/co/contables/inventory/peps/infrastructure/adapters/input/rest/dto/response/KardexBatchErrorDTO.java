package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for batch processing error
 * 
 * Details an error encountered while processing a specific record in a batch.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class KardexBatchErrorDTO {
      private Long productId;
    
    private String factCode;
    
    private String errorMessage;
    
    private int recordIndex;
}
