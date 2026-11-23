package co.unicauca.edu.co.contables.inventory.kardex.application.service.kardex.command;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexBatchCommandPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.kardex.IKardexCommandRepositoryPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.kardex.IKardexExternalClientPort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response.KardexBatchErrorDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response.KardexBatchProcessingResultDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Service for batch kardex processing
 * 
 * Handles batch processing of kardex records from external sources
 * with transaction management and error tracking.
 */
@Service("kardexKardexBatchCommandService")@RequiredArgsConstructor
@Slf4j
public class KardexBatchCommandService implements IKardexBatchCommandPort {

    private final IKardexExternalClientPort kardexExternalClientPort;
    private final IKardexCommandRepositoryPort kardexCommandRepositoryPort;

    /**
     * @brief Processes batch kardex records with transactional integrity
     * 
     * All records must be saved successfully or none will be persisted.
     * Tracks individual record failures for detailed error reporting.
     * 
     * @param enterpriseId Enterprise identifier
     * @return Processing result with counts and error details
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public KardexBatchProcessingResultDto processBatchFromExternalService(String enterpriseId) {
        log.info("Starting batch processing for enterprise: {}", enterpriseId);

        // Clear kardex table before processing new batch
        kardexCommandRepositoryPort.deleteAll();
        
        List<KardexBatchErrorDto> errors = new ArrayList<>();
        int processedCount = 0;
        int failedCount = 0;
        
        try {
            // Fetch data from external service
            List<Kardex> kardexList = kardexExternalClientPort.findKardexByEnterpriseId(enterpriseId);
            
            if (kardexList == null || kardexList.isEmpty()) {
                log.warn("No kardex records found for enterprise: {}", enterpriseId);
                return KardexBatchProcessingResultDto.builder()
                        .totalRecords(0)
                        .processedRecords(0)
                        .failedRecords(0)
                        .success(true)
                        .errors(new ArrayList<>())
                        .build();
            }
            
            int totalRecords = kardexList.size();
            log.info("Processing {} kardex records", totalRecords);
            
            // Process each kardex record
            for (int i = 0; i < kardexList.size(); i++) {
                Kardex kardex = kardexList.get(i);
                try {
                    // Set date before saving
                    kardex.addDate();
                    
                    // Save based on type
                    switch (kardex.getType()) {
                        case PURCHASE:
                            kardexCommandRepositoryPort.registerPurchase(kardex);
                            break;
                        case SALE:
                            kardexCommandRepositoryPort.registerSale(kardex);
                            break;
                        case PURCHASERETURN:
                            kardexCommandRepositoryPort.registerReturnOnPurchase(kardex);
                            break;
                        case SALESRETURN:
                            kardexCommandRepositoryPort.registerReturnOnSale(kardex);
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid movement type: " + kardex.getType());
                    }
                    
                    processedCount++;
                    log.debug("Successfully processed record {}/{} - ProductId: {}, FactCode: {}", 
                            i + 1, totalRecords, kardex.getProductId(), kardex.getFactCode());
                    
                } catch (Exception e) {
                    failedCount++;
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
                    
                    log.error("Failed to process record {}/{} - ProductId: {}, FactCode: {}, Error: {}", 
                            i + 1, totalRecords, kardex.getProductId(), kardex.getFactCode(), errorMsg);
                    
                    errors.add(KardexBatchErrorDto.builder()
                            .productId(kardex.getProductId())
                            .factCode(kardex.getFactCode())
                            .errorMessage(errorMsg)
                            .recordIndex(i)
                            .build());
                    
                    // If any record fails, throw exception to rollback transaction
                    throw new RuntimeException(
                            String.format("Failed to process record at index %d (ProductId: %s, FactCode: %s): %s", 
                                    i, kardex.getProductId(), kardex.getFactCode(), errorMsg), e);
                }
            }
            
            log.info("Batch processing completed successfully. Processed: {}/{}", processedCount, totalRecords);
            
            return KardexBatchProcessingResultDto.builder()
                    .totalRecords(totalRecords)
                    .processedRecords(processedCount)
                    .failedRecords(failedCount)
                    .success(true)
                    .errors(errors)
                    .build();
                    
        } catch (Exception e) {
            log.error("Batch processing failed for enterprise: {}. Error: {}", enterpriseId, e.getMessage());
            
            // Build error result
            return KardexBatchProcessingResultDto.builder()
                    .totalRecords(processedCount + failedCount)
                    .processedRecords(0) // None saved due to rollback
                    .failedRecords(processedCount + failedCount)
                    .success(false)
                    .errors(errors)
                    .build();
        }
    }
}
