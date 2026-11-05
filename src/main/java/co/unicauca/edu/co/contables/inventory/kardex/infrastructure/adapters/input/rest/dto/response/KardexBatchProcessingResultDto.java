package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for batch processing results
 * 
 * Contains information about the batch processing operation results
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class KardexBatchProcessingResultDto {
    
    private int totalRecords;
    
    private int processedRecords;
    
    private int failedRecords;
    
    private boolean success;
    
    private List<KardexBatchErrorDto> errors;
}
