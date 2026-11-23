package co.unicauca.edu.co.contables.inventory.peps.application.ports.input;

import java.util.List;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;

/**
 * @brief Input port for Kardex command operations
 * 
 * Defines the contract for processing inventory movement commands including
 * purchases, sales, and their respective returns using PEPS (FIFO) method.
 */
public interface  IKardexCommandPort {
    /**
     * @brief Registers a purchase transaction
     * @param kardex Purchase details to register
     * @return Processed kardex with updated balances
     */
     Kardex registerPurchase(Kardex kardex);

    /**
     * @brief Registers a sale transaction
     * @param kardex Sale details to register
     * @return Processed kardex with updated balances
     */
     Kardex registerSale(Kardex kardex);

    /**
     * @brief Registers a purchase return transaction
     * @param kardex Purchase return details
     * @return Processed kardex record
     */
     Kardex registerPurchaseReturn(Kardex kardex);

    /**
     * @brief Registers a sale return transaction
     * @param kardex Sale return details
     * @return List of processed kardex records (may generate multiple records for LIFO return logic)
     */
     List<Kardex> registerSaleReturn(Kardex kardex);

    /**
     * @brief Registers a non-commercial exit (e.g., damage, loss)
     * @param kardex Exit details
     * @return Processed kardex record
     */
     Kardex registerNonCommercialExit(Kardex kardex);

    /**
     * @brief Registers a non-commercial entry (e.g., donation, found inventory)
     * @param kardex Entry details
     * @return Processed kardex record
     */
     Kardex registerNonCommercialEntry(Kardex kardex);

    /**
     * @brief Registers an inventory adjustment exit
     * @param kardex Adjustment details
     * @return Processed kardex record
     */
     Kardex registerAdjustmentExit(Kardex kardex);

    /**
     * @brief Registers an inventory adjustment entry
     * @param kardex Adjustment details
     * @return Processed kardex record
     */
     Kardex registerAdjustmentEntry(Kardex kardex);
     
     /**
     * @brief Deletes all kardex records
     */
     void deleteAll();
    
}
