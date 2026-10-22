package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for batch processing error details
 * 
 * Contains information about individual record failures in batch processing
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class KardexBatchErrorDto {
    
    private Long productId;
    
    private String factCode;
    
    private String errorMessage;
    
    private int recordIndex;
}
