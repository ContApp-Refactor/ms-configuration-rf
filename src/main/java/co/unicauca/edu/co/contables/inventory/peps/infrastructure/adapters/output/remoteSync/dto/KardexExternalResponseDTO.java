package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.dto;

import java.util.List;

import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.request.KardexBatchDTORequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for external kardex service response
 * 
 * Represents the response structure from the external microservice
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class KardexExternalResponseDTO {
    private List<KardexBatchDTORequest> data;
    
    private int status;
    
    private String message;
}
