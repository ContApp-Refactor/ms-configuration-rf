package co.unicauca.edu.co.contables.inventory.peps.application.ports.input;

import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.KardexBatchProcessingResultDTO;

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
    KardexBatchProcessingResultDTO processBatchFromExternalService(String enterpriseId);
    
}
