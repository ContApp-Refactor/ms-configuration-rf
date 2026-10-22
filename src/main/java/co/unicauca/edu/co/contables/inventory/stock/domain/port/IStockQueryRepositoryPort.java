package co.unicauca.edu.co.contables.inventory.stock.domain.port;

import co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock;

public interface IStockQueryRepositoryPort {
    Stock findByProductId(Long productId);

    boolean existsByProductId(Long productId);
}
