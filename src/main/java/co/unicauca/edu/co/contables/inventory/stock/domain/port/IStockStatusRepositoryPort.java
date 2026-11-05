package co.unicauca.edu.co.contables.inventory.stock.domain.port;

public interface IStockStatusRepositoryPort {
    void activate(Long productId);
    void inactivate(Long productId);
}
