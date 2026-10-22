package co.unicauca.edu.co.contables.inventory.stock.domain.port;

import java.util.List;

import co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock;

public interface IStockCommandRepositoryPort {
    Stock save(Stock stock);
    String saveAll(List<Stock> stocks);
    Stock update(Long productId, String name);
    Stock registerPurchase(Stock stock);
    Stock registerSale(Stock stock);
    String deleteAllByEnterpriseId(String enterpriseId);
    int deleteByProductId(Long productId);
}
