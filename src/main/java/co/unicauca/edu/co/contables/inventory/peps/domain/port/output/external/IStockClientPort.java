package co.unicauca.edu.co.contables.inventory.peps.domain.port.output.external;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Stock;

/**
 * @brief Output port for stock service client
 * 
 * Provides interface for communicating stock changes to
 * external stock management systems.
 */
public interface IStockClientPort {
    /**
     * @brief Notifies a stock purchase (increase)
     * @param stock Stock information to update
     */
    void buyStock(Stock stock);

    /**
     * @brief Notifies a stock sale (decrease)
     * @param stock Stock information to update
     */
    void sellStock(Stock stock);
}
