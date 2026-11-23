package co.unicauca.edu.co.contables.inventory.kardex.domain.port.kardex;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;

/**
 * @brief Output port for Kardex read operations
 * 
 * Provides query capabilities for retrieving inventory movement
 * records with various filtering and pagination options.
 */
public interface IKardexQueryRepositoryPort {
    /**
     * @brief Finds kardex records by product with pagination
     * @param productId Product identifier
     * @param pageable Pagination parameters
     * @return Paginated kardex records
     */
    Page<Kardex> findProductId(Long productId, Pageable pageable);
    
    /**
     * @brief Finds kardex records by product and date range
     * @param productId Product identifier
     * @param pageable Pagination parameters
     * @param startDate Start date filter
     * @param endDate End date filter
     * @return Paginated kardex records within date range
     */
    Page<Kardex> findProductIdAndDate(Long productId, Pageable pageable, LocalDate startDate, LocalDate endDate);
    
    /**
     * @brief Finds specific kardex records by invoice, product and movement type
     * @param factCode Invoice/document code
     * @param productId Product identifier
     * @param type Movement type
     * @return List of matching kardex records
     */
    List<Kardex> findByFactCodeAndProductIdAndType(String factCode, Long productId, MovementType type);
    
    /**
     * @brief Gets the most recent kardex record for a product
     * @param productId Product identifier
     * @return Latest kardex record or null if none exists
     */
    Kardex getLatestKardexByProductId(Long productId);
    
    /**
     * @brief Checks if a specific kardex combination already exists
     * @param factCode Invoice/document code
     * @param productId Product identifier
     * @param type Movement type
     * @return True if combination exists, false otherwise
     */
    boolean existsByFactCodeAndProductIdAndType(String factCode, Long productId, MovementType type);
    
    /**
     * @brief Gets the last kardex record for all products of an enterprise
     * @param enterpriseId Enterprise identifier to filter products
     * @return List of latest kardex records for each product
     */
    List<Kardex> findLastKardexForAllProducts(String enterpriseId);
}
