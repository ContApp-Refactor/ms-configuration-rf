package co.unicauca.edu.co.contables.inventory.products.application.ports.input;

/**
 * @brief Puerto de entrada para gestión de uso de productos
 *
 * Define contrato para operaciones relacionadas con el contador de uso de productos,
 * utilizado cuando otros servicios notifican que han utilizado un producto.
 */
public interface IProductUsagePort {
    /**
     * @brief Incrementa el contador de uso de un producto
     * @param productId ID del producto
     */
    void incrementUsageCount(Long productId);
}

