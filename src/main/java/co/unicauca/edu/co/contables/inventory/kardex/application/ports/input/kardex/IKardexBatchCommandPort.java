package co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex;

import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response.KardexBatchProcessingResultDto;

/**
 * @brief Input port for Kardex batch command operations
 * 
 * Defines the contract for processing batch inventory movements
 * from external sources.
 */
public interface IKardexBatchCommandPort {
    
    /**
     * @brief Processes batch kardex records from external service
     * @param enterpriseId Enterprise identifier
     * @return Processing result with success/failure details
     */
    KardexBatchProcessingResultDto processBatchFromExternalService(String enterpriseId);
}
