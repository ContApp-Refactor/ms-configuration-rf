package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.adapter;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.IKardexExternalClientPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.config.IKardexExternalClient;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.dto.KardexExternalResponseDTO;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.mapper.IKardexExternalClientMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Adapter for External Kardex Service communication
 * 
 * Implements the output port to retrieve Kardex history from an external legacy or backup system.
 * Handles data mapping and error management during the retrieval process.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KardexExternalClientAdapter implements IKardexExternalClientPort {

    private final IKardexExternalClientMapper kardexExternalClientMapper;
    private final IKardexExternalClient kardexExternalClient;
    private final IFormatterResultOutputPort formatterResultOutputPort;

    /**
     * @brief Retrieves Kardex records for a specific enterprise
     * 
     * Fetches historical or external Kardex data associated with the given enterprise ID.
     * Maps the external DTOs to domain models.
     * 
     * @param enterpriseId The unique identifier of the enterprise
     * @return List of Kardex domain objects, or an empty list if not found or on error
     */
    @Override
    public List<Kardex> findKardexByEnterpriseId(String enterpriseId) {
       
       
        try{

            log.info("Fetching kardex records for enterprise: {}", enterpriseId);
            KardexExternalResponseDTO response=kardexExternalClient.findKardexByEnterpriseId(enterpriseId);

            if (response == null || response.getData() == null) {
                log.warn("No data received from external kardex service for enterprise: {}", enterpriseId);
                formatterResultOutputPort.returnErrorGenericResponse(500,"No data received from external kardex service");
            }

            log.info("Retrieved {} kardex records from external service", response.getData().size());
            
            return response.getData()
                    .stream()
                    .map(kardexExternalClientMapper::toDomain)
                    .toList();


        }catch (RestClientResponseException e) {
            // El dispatch es por código de estado, no por subclase: RestClient no
            // garantiza lanzar HttpServerErrorException.ServiceUnavailable ni
            // HttpClientErrorException.NotFound, y decidir por subclase perdería
            // los errores 503/404 que este adaptador sí traduce.
            int status = e.getStatusCode().value();
            if (status == 503) {
                log.error("Kardex external service is unavailable (503): {}", e.getMessage());
                formatterResultOutputPort.returnErrorGenericResponse(503, "Kardex external service is unavailable");
            } else if (status == 404) {
                log.warn("No kardex records found for enterprise: {}", enterpriseId);
                formatterResultOutputPort.returnErrorGenericResponse(404, "No kardex records found for the given enterprise ID");
            } else {
                log.error("Error calling kardex external service: {} - {}", e.getStatusCode(), e.getStatusText());
                formatterResultOutputPort.returnErrorGenericResponse(500, "Error communicating with kardex external service");
            }

        } catch (Exception e) {
            log.error("Unexpected error calling kardex external service: {}", e.getMessage(), e);
            formatterResultOutputPort.returnErrorGenericResponse(500,"Unexpected error communicating with kardex external service");    
        }   
        return List.of();
    }
    
}
