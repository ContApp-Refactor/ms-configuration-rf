package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.config;


import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.dto.KardexExternalResponseDTO;

public interface IKardexExternalClient {
    
     /**
     * @brief Retrieves kardex records by enterprise ID
     * @param enterpriseId The enterprise identifier
     * @return Response with list of kardex records
     */
    @GetExchange("/api/kardex/weighted-average/last-kardex-all-products")
    KardexExternalResponseDTO findKardexByEnterpriseId(@RequestParam String enterpriseId);
}
