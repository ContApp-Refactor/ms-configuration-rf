package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotBlank;
import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexBatchCommandPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.ResponseDTO;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.KardexBatchProcessingResultDTO;
import lombok.RequiredArgsConstructor;

/**
 * @brief REST controller for batch kardex operations
 * 
 * Provides HTTP endpoints for processing batch inventory movements
 * from external services.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kardex/peps/batch")
public class KardexBatchCommandController {
    private final IKardexBatchCommandPort kardexBatchCommandPort;

    /**
     * @brief Processes batch kardex records for an enterprise
     * 
     * @param enterpriseId Enterprise identifier
     * @return Response with processing results
     */
    @PostMapping("/process-enterprise/{enterpriseId}")
    public ResponseEntity<ResponseDTO<KardexBatchProcessingResultDTO>> processBatchForEnterprise(
            @PathVariable @NotBlank(message = "Enterprise ID cannot be blank") String enterpriseId) {
        
        KardexBatchProcessingResultDTO result = kardexBatchCommandPort.processBatchFromExternalService(enterpriseId);
        
        ResponseDTO<KardexBatchProcessingResultDTO> responseDto = ResponseDTO.<KardexBatchProcessingResultDTO>builder()
                .data(result)
                .status(200)
                .message(String.format("Batch processing completed. Processed: %d/%d records", 
                        result.getProcessedRecords(), result.getTotalRecords()))
                .build();
        
        return responseDto.of();
    }
    
}
