package co.unicauca.edu.co.contables.inventory.peps.application.service.command;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexBatchCommandPort;
import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexCommandPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.IKardexExternalClientPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.KardexBatchErrorDTO;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.KardexBatchProcessingResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * @brief Service for batch kardex processing
 * 
 * Handles batch processing of kardex records from external sources
 * with transaction management and error tracking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KardexBatchCommandService implements IKardexBatchCommandPort {

    private final IKardexExternalClientPort kardexExternalClientPort;
    private final IKardexCommandPort kardexCommandPort;
    

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
    @Transactional(rollbackFor=Exception.class)
    public KardexBatchProcessingResultDTO processBatchFromExternalService(String enterpriseId) {
      log.info("Starting batch processing for enterprise: {}", enterpriseId);

      kardexCommandPort.deleteAll();
       
        List<KardexBatchErrorDTO> errors = new ArrayList<>();
        int processedCount = 0;
        int failedCount = 0;

        try{
            List<Kardex> kardexList=kardexExternalClientPort.findKardexByEnterpriseId(enterpriseId);
            if(kardexList==null || kardexList.isEmpty()){
                log.warn("No kardex records found for enterprise: {}", enterpriseId);

                return KardexBatchProcessingResultDTO.builder()
                        .totalRecords(0)
                        .processedRecords(0)
                        .failedRecords(0)
                        .success(true)
                        .errors(new ArrayList<>())
                        .build();
            }
            int totalRecords = kardexList.size();
            log.info("Processing {} kardex records", totalRecords);
            
            for(int i=0;i<kardexList.size();i++){
               Kardex kardex = kardexList.get(i);
               try{
                    switch (kardex.getType()) {
                        case PURCHASE:
                            kardexCommandPort.registerPurchase(kardex);
                        break;
                    
                        default:
                             throw new IllegalArgumentException("Invalid movement type: " + kardex.getType());
                    }

                    processedCount++;
                    log.debug("Successfully processed record {}/{} - ProductId: {}, FactCode: {}", 
                            i + 1, totalRecords, kardex.getProduct().getProductId(), kardex.getFactCode());

               } catch (Exception e) {
                 failedCount++;
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
                    
                    log.error("Failed to process record {}/{} - ProductId: {}, FactCode: {}, Error: {}", 
                            i + 1, totalRecords, kardex.getProduct().getProductId(), kardex.getFactCode(), errorMsg);
                    
                    errors.add(KardexBatchErrorDTO.builder()
                            .productId(kardex.getProduct().getProductId())
                            .factCode(kardex.getFactCode())
                            .errorMessage(errorMsg)
                            .recordIndex(i)
                            .build());
                    
                    // If any record fails, throw exception to rollback transaction
                    throw new RuntimeException(
                            String.format("Failed to process record at index %d (ProductId: %s, FactCode: %s): %s", 
                                    i, kardex.getProduct().getProductId(), kardex.getFactCode(), errorMsg), e);
               }
            }
             log.info("Batch processing completed successfully. Processed: {}/{}", processedCount, totalRecords);
            
            return KardexBatchProcessingResultDTO.builder()
                    .totalRecords(totalRecords)
                    .processedRecords(processedCount)
                    .failedRecords(failedCount)
                    .success(true)
                    .errors(errors)
                    .build();

        }catch(Exception e){
            log.error("Batch processing failed for enterprise: {}. Error: {}", enterpriseId, e.getMessage());
            //Build error result
            return KardexBatchProcessingResultDTO.builder()
                    .totalRecords(processedCount + failedCount)
                    .processedRecords(0) // None saved due to rollback
                    .failedRecords(processedCount + failedCount)
                    .success(false)
                    .errors(errors)
                    .build();

        }


    }
    
}
