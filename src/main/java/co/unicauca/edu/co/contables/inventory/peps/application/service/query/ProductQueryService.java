package co.unicauca.edu.co.contables.inventory.peps.application.service.query;

import java.util.List;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IProductQueryPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IMessageServicePort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.query.IProductQueryOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.config.i18n.MessageKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Service implementation for Product query operations
 * 
 * Handles retrieval of product information, specifically filtering by enterprise.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductQueryService implements IProductQueryPort {
    private final IProductQueryOutputPort productQueryOutputPort;
    private final IMessageServicePort messageServicePort;
    
    /**
     * @brief Retrieves all products for a specific enterprise
     * 
     * @param enterpriseId Identifier of the enterprise
     * @return List of products belonging to the enterprise
     */
    @Override
    public List<Product> findAll(String enterpriseId) {
        log.info(messageServicePort.getMessage(MessageKeys.LOG_PRODUCT_QUERY_ALL, enterpriseId));
        return productQueryOutputPort.findAll(enterpriseId);
    }
    
    
}
