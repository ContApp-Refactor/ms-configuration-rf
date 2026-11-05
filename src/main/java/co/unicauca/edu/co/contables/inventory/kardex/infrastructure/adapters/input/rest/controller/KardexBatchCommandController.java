package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexBatchCommandPort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response.KardexBatchProcessingResultDto;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

/**
 * @brief REST controller for batch kardex operations
 * 
 * Provides HTTP endpoints for processing batch inventory movements
 * from external services.
 */
@RestController("kardexKardexBatchCommandController")@RequiredArgsConstructor
@RequestMapping("/api/kardex/weighted-average/batch")
@Validated
public class KardexBatchCommandController {

    private final IKardexBatchCommandPort kardexBatchCommandPort;

    /**
     * @brief Processes batch kardex records for an enterprise
     * 
     * @param enterpriseId Enterprise identifier
     * @return Response with processing results
     */
    @PostMapping("/process-enterprise/{enterpriseId}")
    public ResponseEntity<ResponseDto<KardexBatchProcessingResultDto>> processBatchForEnterprise(
            @PathVariable @NotBlank(message = "Enterprise ID cannot be blank") String enterpriseId) {
        
        KardexBatchProcessingResultDto result = kardexBatchCommandPort.processBatchFromExternalService(enterpriseId);
        
        ResponseDto<KardexBatchProcessingResultDto> responseDto = ResponseDto.<KardexBatchProcessingResultDto>builder()
                .data(result)
                .status(200)
                .message(String.format("Batch processing completed. Processed: %d/%d records", 
                        result.getProcessedRecords(), result.getTotalRecords()))
                .build();
        
        return responseDto.of();
    }
}
