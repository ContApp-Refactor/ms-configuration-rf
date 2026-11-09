package co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    /**
     * @brief Gets the unit price of the most recent commercial entry of a product
     *
     * Considers only PURCHASE and ADJUSTMENTENTRY movements (latest by date, ties broken by
     * highest id). Sales, exits, returns and non-commercial entries are ignored. Read-only.
     * @param productId Product identifier
     * @return Unit price of the latest purchase/adjustment entry, or empty if none exists
     */
    Optional<BigDecimal> findLastEntryUnitPrice(Long productId);

    /**
     * @brief Gets the current weighted-average unit cost of a product
     *
     * It is the {@code balanceUnitPrice} of the most recent kardex row (date desc, id desc) whose
     * balance unit price is not null and greater than zero; legacy rows without balance are skipped.
     * Read-only.
     * @param productId Product identifier
     * @return Current average unit cost, or empty if no row has a positive balance unit price
     */
    Optional<BigDecimal> findCurrentAverageUnitCost(Long productId);
}
