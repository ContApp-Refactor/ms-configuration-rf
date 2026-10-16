package co.unicauca.edu.co.contables.inventory.peps.domain.port.output.query;

import java.math.BigDecimal;
import java.util.List;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.DetailOutput;

/**
 * @brief Output port for Detail Output query operations
 * 
 * Provides interface for retrieving and managing detail output records
 * associated with kardex movements.
 */
public interface IDetailQueryOutPutPort {

    /**
     * @brief Finds detail outputs by sale movement ID
     * @param idMovementsale Sale movement identifier
     * @return List of detail outputs
     */
    List<DetailOutput> findByMovementSale(Long idMovementsale);

    /**
     * @brief Deletes a detail output by ID
     * @param id Detail output identifier
     */
    void deleteById(Long id);

    /**
     * @brief Finds detail outputs by sale movement ID ordered descending
     * @param idKardexSale Sale movement identifier
     * @return List of detail outputs ordered by ID descending
     */
    List<DetailOutput> findByMovementSaleOrderedDesc(Long idKardexSale);

    /**
     * @brief Updates a detail output record
     * @param detail Detail output to update
     * @return Updated detail output
     */
    DetailOutput update(DetailOutput detail);
    
    /**
     * @brief Updates only the used quantity of an output detail
     * 
     * The unit price (unit_value) remains unchanged as it represents the lot's unit price.
     * Foreign keys (id_movement_origin, id_movement_sale) are preserved intact.
     * 
     * @param detailId ID of the detail to update
     * @param newQuantity New used quantity
     * @param unitPrice Unit price (kept same, passed for consistency)
     */
    void updateQuantityAndPrice(Long detailId, int newQuantity, BigDecimal unitPrice);
    
}
