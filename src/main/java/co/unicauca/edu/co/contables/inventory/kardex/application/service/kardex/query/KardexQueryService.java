package co.unicauca.edu.co.contables.inventory.kardex.application.service.kardex.query;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexQueryPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.kardex.IKardexQueryRepositoryPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Service implementation for Kardex query operations
 * 
 * Provides read access to inventory movement records with
 * optional date filtering and pagination support.
 */
@Service("kardexKardexQueryService")@RequiredArgsConstructor
@Slf4j
public class KardexQueryService implements IKardexQueryPort {

    private final IKardexQueryRepositoryPort kardexQueryRepositoryPort;

    /**
     * @brief Retrieves kardex records for a product with optional date filtering
     * @param productId Product identifier
     * @param pageable Pagination parameters
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return Paginated kardex records
     */
    @Override
    public Page<Kardex> findProductId(Long productId, Pageable pageable, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return kardexQueryRepositoryPort.findProductIdAndDate(productId, pageable, startDate, endDate);
        }
        return kardexQueryRepositoryPort.findProductId(productId, pageable);
    }

    /**
     * @brief Gets the last kardex record for all products of an enterprise
     * @param enterpriseId Enterprise identifier to filter products
     * @return List of latest kardex records for each product
     */
    @Override
    public List<Kardex> findLastKardexForAllProducts(String enterpriseId) {
        log.info("Fetching last kardex record for all products of enterprise: {}", enterpriseId);
        return kardexQueryRepositoryPort.findLastKardexForAllProducts(enterpriseId);
    }
    
    /**
     * @brief Gets the most recent kardex record for a specific product
     * @param productId Product identifier
     * @return Latest kardex record or null if none exists
     */
    @Override
    public Kardex getLatestKardexByProductId(Long productId) {
        log.info("Fetching latest kardex record for product: {}", productId);
        return kardexQueryRepositoryPort.getLatestKardexByProductId(productId);
    }
    
}
