package co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;

/**
 * @brief Input port for Kardex query operations
 * 
 * Provides query capabilities for retrieving inventory movement records
 * with optional date filtering and pagination support.
 */
public interface IKardexQueryPort {
    /**
     * @brief Finds kardex records for a specific product
     * @param productId Product identifier
     * @param pageable Pagination parameters
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return Paginated kardex records
     */
    Page<Kardex>  findProductId(Long productId, Pageable pageable, LocalDate startDate, LocalDate endDate);
    
    /**
     * @brief Gets the last kardex record for all products of an enterprise
     * @param enterpriseId Enterprise identifier to filter products
     * @return List of latest kardex records for each product
     */
    List<Kardex> findLastKardexForAllProducts(String enterpriseId);
    
    /**
     * @brief Gets the most recent kardex record for a specific product
     * @param productId Product identifier
     * @return Latest kardex record or null if none exists
     */
    Kardex getLatestKardexByProductId(Long productId);
}
