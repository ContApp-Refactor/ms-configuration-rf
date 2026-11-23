package co.unicauca.edu.co.contables.inventory.stock.application.ports.input;

import co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock;

/**
 * @Brief Interface for command operations on Stock entities.
 */
public interface IStockCommandPort {
    Stock save(Stock stock);
    Stock update(Long productId, String name);
    Stock registerPurchase(Stock stock);
    Stock registerSale(Stock stock);
    String deleteAllByEnterpriseId(String enterpriseId);
}
