package co.unicauca.edu.co.contables.inventory.stock.application.ports.input;

/**
 * @Brief Interface for status operations on Stock entities.
 */
public interface IStockStatusPort {
    void inactivate(Long id);
    void activate(Long id);
}
