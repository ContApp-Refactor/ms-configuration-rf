package co.unicauca.edu.co.contables.inventory.peps.application.ports.input;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.KardexMigration;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.KardexReport;

/**
 * @brief Input port for Kardex query operations
 * 
 * Provides query capabilities for retrieving inventory movement records
 * and generating reports.
 */
public interface IKardexQueryPort {
    /**
     * @brief Retrieves paginated kardex reports for a specific product within a date range
     * @param productId Product identifier
     * @param start Start date filter
     * @param end End date filter
     * @param pageable Pagination parameters
     * @return Page of kardex reports
     */
    Page<KardexReport> getRecordsKardexByProduct(Long productId, LocalDate start, LocalDate end,Pageable pageable);

    /**
     * @brief Retrieves available quantity details for a product (FIFO lots)
     * @param productId Product identifier
     * @return List of kardex records representing available stock lots
     */
    List<Kardex> getKardexAvailableQuantityByProduct(Long productId);

    /**
     * @brief Gets the last kardex record for all products of an enterprise
     * @param enterpriseId Enterprise identifier to filter products
     * @return List of latest kardex records for each product
     */
    List<KardexMigration> findLastKardexForAllProducts(String enterpriseId);

}
