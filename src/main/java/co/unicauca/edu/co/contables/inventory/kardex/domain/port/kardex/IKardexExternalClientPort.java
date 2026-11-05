package co.unicauca.edu.co.contables.inventory.kardex.domain.port.kardex;

import java.util.List;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;

/**
 * @brief Output port for external kardex system integration
 * 
 * Provides interface for retrieving kardex information
 * from external kardex management systems.
 */
public interface IKardexExternalClientPort {
    
    /**
     * @brief Retrieves kardex records by enterprise ID
     * @param enterpriseId Enterprise identifier
     * @return List of kardex records for the enterprise
     */
    List<Kardex> findKardexByEnterpriseId(String enterpriseId);
}
