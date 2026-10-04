package co.unicauca.edu.co.contables.inventory.stock.application.ports.input;

import co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock;

/**
 * @Brief Interface for query operations on Stock entities.
 */
public interface IStockQueryPort {
    Stock findByProductId(Long id);

    boolean existsByProductId(Long id);
}
