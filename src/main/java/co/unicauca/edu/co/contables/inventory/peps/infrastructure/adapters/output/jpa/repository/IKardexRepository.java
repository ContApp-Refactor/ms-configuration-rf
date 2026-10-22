package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.KardexMigration;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.KardexEntity;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;



/**
 * @brief Repository for Kardex entities
 * 
 * Handles database operations for Kardex records, including FIFO logic queries,
 * balance calculations, and movement history retrieval.
 */
public interface  IKardexRepository extends JpaRepository<KardexEntity, Long>{
   
    /**
     * @brief Finds all Kardex entries for a product
     * @param idProduct The product ID
     * @return List of Kardex entries
     */
    List<KardexEntity> findByProductId(Long idProduct);

    /**
     * @brief Finds the latest Kardex entry by product ID
     * @param productId The product ID
     * @return The latest Kardex entry
     */
    KardexEntity findTopByProductIdOrderByDateDesc(Long productId);

      /**
       * @brief Calculates total available amount for a product
       * @param productId The product ID
       * @return Total available quantity
       */
      @Query("SELECT SUM(k.availableQuantity) "+
         "FROM KardexEntity k "+
         "WHERE k.product.productId = :productId")
      int sumAvailableAmountByProduct(@Param("productId") Long productId);


      /**
       * @brief Finds the first record with available amount (FIFO)
       * @param productId The product ID
       * @return Optional containing the first available lot
       */
      @Query("SELECT k "+
          "FROM KardexEntity k "+
          "WHERE k.product.productId = :productId "+
          "AND k.availableQuantity > 0 "+
          "ORDER BY k.date ASC ")
      Optional<KardexEntity> findFirstByAvailableAmount(@Param("productId") Long productId);


      /**
       * @brief Updates the available amount of a Kardex entry
       * @param idKardex The ID of the Kardex entry
       * @param newAmount The new available amount
       * @return Number of rows affected
       */
      @Modifying
      @Transactional
      @Query("UPDATE KardexEntity k SET k.availableQuantity = :newAmount WHERE k.idKardex = :idKardex")
      int updateAvaliableAmount(@Param("idKardex") Long idKardex, @Param("newAmount") int newAmount);


      /**
       * @brief Finds movements within a date range
       * @param productId The product ID
       * @param startDate Start date
       * @param endDate End date
       * @return List of movements in range
       */
      @Query("SELECT DISTINCT k "+
      "FROM KardexEntity k "+ 
      "JOIN FETCH k.product "+
      "LEFT JOIN FETCH k.detailsOutput "+
      "WHERE k.product.productId = :productId AND k.date >= :startDate AND k.date <= :endDate ORDER BY k.date ASC")
      List<KardexEntity> findMovementsInDateRange(Long productId, ZonedDateTime startDate, ZonedDateTime endDate);
   

      /**
       * @brief Finds movements before a specific date
       * @param productId The product ID
       * @param startDate The cutoff date
       * @return List of movements before the date
       */
      @Query("SELECT DISTINCT k "+
      "FROM KardexEntity k "+
      "JOIN FETCH k.product "+
      "LEFT JOIN FETCH k.detailsOutput "+
      "WHERE k.product.productId = :productId AND k.date < :startDate ORDER BY k.date ASC")
      List<KardexEntity> findMovementsBeforeDate(Long productId, ZonedDateTime startDate);


      /**
       * @brief Finds a Kardex entry by invoice code and product
       * @param factCode Invoice code
       * @param product Product ID
       * @return Optional containing the Kardex entry
       */
      @Query("SELECT k FROM KardexEntity k WHERE k.factCode = :factCode AND k.product.id = :productId")
      Optional<KardexEntity> findByFactCode(@Param("factCode") String factCode, @Param("productId")long product);

      /**
     * @brief Finds a Kardex record by invoice code and product ID
     * 
     * In case of multiple records, returns the oldest one (first by date).
     * 
     * @param factCode Invoice code
     * @param productId Product ID
     * @return Optional with the first record found ordered by date ascending
     */
     Optional<KardexEntity> findFirstByFactCodeAndProduct_ProductIdOrderByDateAsc(String factCode, Long productId);

      /**
       * @brief Finds available purchases ordered by date (FIFO)
       * @param productId The product ID
       * @return List of available purchase entries
       */
      @Query("SELECT k FROM KardexEntity k " +
           "JOIN FETCH k.product " +
           "WHERE k.product.productId = :productId " +
           "AND k.availableQuantity > 0 " +
           "ORDER BY k.date ASC")
      List<KardexEntity> findAvailablePurchasesOrderedByDate(@Param("productId") Long productId);

     
    /**
     * @brief Calculates the last Kardex state for all products of an enterprise
     * @param enterpriseId The enterprise ID
     * @return List of migration data with calculated balances
     */
    @Query("""
        SELECT new co.unicauca.edu.co.contables.inventory.peps.domain.model.KardexMigration(
            p.productId,
            CAST(COALESCE(SUM(k.availableQuantity), 0) AS long),
            CAST(p.reference AS string),
            COALESCE(
                SUM(k.availableQuantity * k.unitPrice) / 
                NULLIF(SUM(k.availableQuantity), 0), 
                0
            ),
            CONCAT(p.name, ' - ', p.presentation),
            co.unicauca.edu.co.contables.inventory.peps.domain.enums.MovementType.PURCHASE,
            CAST(COALESCE(SUM(k.availableQuantity), 0) AS long),
            COALESCE(
                SUM(k.availableQuantity * k.unitPrice) / 
                NULLIF(SUM(k.availableQuantity), 0), 
                0
            ),
            COALESCE(SUM(k.availableQuantity * k.unitPrice), 0)
        )
        FROM PepsProductEntity p
        LEFT JOIN p.recordsKardex k
        WHERE p.enterpriseId = :enterpriseId
            AND p.state = true
            AND k.availableQuantity > 0
        GROUP BY p.productId, p.name, p.reference, p.presentation
        HAVING SUM(k.availableQuantity) > 0
        ORDER BY p.name ASC
        """)
    List<KardexMigration> findLastKardexForAllProductsByEnterpriseId(@Param("enterpriseId") String enterpriseId);


    /**
     * @brief Checks if any Kardex entries exist for a product
     * @param productId The product ID
     * @return True if exists, false otherwise
     */
    boolean existsByProduct_ProductId(Long productId);

}
