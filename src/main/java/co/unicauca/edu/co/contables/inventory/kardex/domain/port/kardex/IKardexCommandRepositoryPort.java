package co.unicauca.edu.co.contables.inventory.kardex.domain.port.kardex;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;

/**
 * @brief Output port for Kardex write operations
 * 
 * Defines the contract for persisting inventory movement records
 * in the underlying data storage system.
 */
public interface IKardexCommandRepositoryPort {
    /**
     * @brief Persists a purchase transaction
     * @param kardex Purchase record to save
     * @return Saved kardex with generated identifiers
     */
    Kardex registerPurchase(Kardex kardex);
    
    /**
     * @brief Persists a sale transaction
     * @param kardex Sale record to save
     * @return Saved kardex with generated identifiers
     */
    Kardex registerSale(Kardex kardex);
    
    /**
     * @brief Persists a purchase return transaction
     * @param kardex Return record to save
     * @return Saved kardex with generated identifiers
     */
    Kardex registerReturnOnPurchase(Kardex kardex);
    
    /**
     * @brief Persists a sale return transaction
     * @param kardex Return record to save
     * @return Saved kardex with generated identifiers
     */
    Kardex registerReturnOnSale(Kardex kardex);
    
    /**
     * @brief Deletes all kardex records
     */
    void deleteAll();
}
