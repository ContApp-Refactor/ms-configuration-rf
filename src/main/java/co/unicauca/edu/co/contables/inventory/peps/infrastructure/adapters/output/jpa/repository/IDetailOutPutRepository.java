package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.repository;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity.DetailOutputEntity;

/**
 * @brief Repository for DetailOutput entities
 * 
 * Handles database operations for detail output records, including
 * retrieval by sale ID and updates to quantity/price.
 */
public interface  IDetailOutPutRepository extends JpaRepository<DetailOutputEntity,Long>{

  /**
   * @brief Finds detail outputs by movement sale ID with fetched origin
   * @param idMovementSale The ID of the movement sale
   * @return List of detail outputs with initialized movement origin
   */
  @Query("SELECT d FROM DetailOutputEntity d JOIN FETCH d.movementOrigin WHERE d.movementSale.idKardex = :idMovementSale")
  List<DetailOutputEntity> findByMovementSaleId(@Param("idMovementSale") Long idMovementSale);

  /**
   * @brief Finds detail outputs by movement sale ID
   * @param idKardex The ID of the kardex sale
   * @return List of detail outputs
   */
  @Query("SELECT d FROM DetailOutputEntity d WHERE d.movementSale.idKardex = :idKardex")
    List<DetailOutputEntity> findByMovementSale(@Param("idKardex") Long idKardex);
    
  /**
   * @brief Finds detail outputs by movement sale ID ordered descending
   * @param idKardex The ID of the kardex sale
   * @return List of detail outputs ordered by ID descending
   */
  @Query("SELECT d FROM DetailOutputEntity d WHERE d.movementSale.idKardex = :idKardex ORDER BY d.idDetailOutput DESC")
  List<DetailOutputEntity> findByMovementSaleOrderedDesc(@Param("idKardex") Long idKardex);
 
  /**
   * @brief Updates quantity and price of a detail output
   * @param detailId The ID of the detail output
   * @param quantity The new quantity
   * @param unitPrice The new unit price
   */
  @Modifying
  @Query("UPDATE DetailOutputEntity d SET d.quantityUsed = :quantity, d.unitPrice = :unitPrice WHERE d.idDetailOutput = :detailId")
  void updateQuantityAndPrice(@Param("detailId") Long detailId, 
                               @Param("quantity") int quantity, 
                               @Param("unitPrice") BigDecimal unitPrice);
}

