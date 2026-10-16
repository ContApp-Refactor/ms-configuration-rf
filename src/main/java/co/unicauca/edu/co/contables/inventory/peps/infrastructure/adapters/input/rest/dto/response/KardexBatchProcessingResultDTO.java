package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for batch processing result
 * 
 * Summarizes the outcome of a batch kardex processing operation.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class KardexBatchProcessingResultDTO {
    private int totalRecords;
    
    private int processedRecords;
    
    private int failedRecords;
    
    private boolean success;
    
    private List<KardexBatchErrorDTO> errors;
}
