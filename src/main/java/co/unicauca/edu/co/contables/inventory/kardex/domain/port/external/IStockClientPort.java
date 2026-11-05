package co.unicauca.edu.co.contables.inventory.kardex.domain.port.external;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Stock;

/**
 * @brief Output port for external stock system integration
 * 
 * Provides interface for communicating inventory changes
 * to external stock management systems.
 */
public interface IStockClientPort {
    /**
     * @brief Notifies external system of stock increase
     * @param stock Stock information to add
     */
    void buyStock(Stock stock);
    
    /**
     * @brief Notifies external system of stock decrease
     * @param stock Stock information to deduct
     */
    void sellStock(Stock stock);
}
