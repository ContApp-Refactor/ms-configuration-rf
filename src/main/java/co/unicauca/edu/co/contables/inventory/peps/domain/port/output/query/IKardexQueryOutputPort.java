package co.unicauca.edu.co.contables.inventory.peps.domain.port.output.query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.KardexMigration;

/**
 * @brief Output port for Kardex query operations
 * 
 * Provides interface for retrieving kardex records and inventory information
 * from the persistence layer.
 */
public interface IKardexQueryOutputPort {
   /**
    * @brief Retrieves all kardex records for a product
    * @param idProduct Product identifier
    * @return List of kardex records
    */
   List<Kardex> getAllKardex(Long idProduct);

   /**
    * @brief Calculates the total available amount for a product
    * @param idProduct Product identifier
    * @return Total available quantity
    */
   int getAvailableAmountByProduct(Long idProduct);

   /**
    * @brief Retrieves the first record with available amount for a product
    * @param idProduct Product identifier
    * @return Optional containing the first available kardex record
    */
   Optional<Kardex> getFirstRecordByAmountAvailable(Long idProduct);

   /**
    * @brief Finds movements for a product within a date range
    * @param productId Product identifier
    * @param startDate Start date of the range
    * @param endDate End date of the range
    * @return List of kardex movements
    */
   List<Kardex> findMovementsByProductAndDateRange(Long productId,LocalDate startDate,LocalDate endDate);

    /**
     * @brief Finds movements for a product before a specific date
     * @param productId Product identifier
     * @param startDate Date threshold
     * @return List of kardex movements
     */
    List<Kardex> findMovementsByProductBeforeDate(Long productId, LocalDate startDate);
   
    /**
     * @brief Finds a kardex record by invoice reference and product
     * @param factCode Invoice code
     * @param productId Product identifier
     * @return Optional containing the kardex record if found
     */
    Optional<Kardex> findByRefFacture(String factCode, Long productId);

    /**
     * @brief Finds available purchase records ordered by date
     * @param idProduct Product identifier
     * @return List of available purchase records
     */
    List<Kardex> findAvailablePurchasesOrderedByDate(Long idProduct);

    /**
     * @brief Finds a kardex record by ID
     * @param id Kardex identifier
     * @return Optional containing the kardex record if found
     */
    Optional<Kardex> findById(Long id);

    /**
     * @brief Finds the last kardex record for all products of an enterprise
     * @param enterpriseId Enterprise identifier
     * @return List of latest kardex migration records
     */
    List<KardexMigration> findLastKardexForAllProducts(String enterpriseId);

   
    /**
     * @brief Retrieves the latest kardex record for a product
     * @param productId Product identifier
     * @return The latest kardex record
     */
    Kardex getLatestKardexByProductId(Long productId);

    /**
     * @brief Checks if any kardex record exists for a product
     * @param productId Product identifier
     * @return True if exists, false otherwise
     */
    boolean existsByProduct_ProductId(Long productId);
}
