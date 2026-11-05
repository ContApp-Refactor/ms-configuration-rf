package co.unicauca.edu.co.contables.inventory.products.application.ports.input;

/**
 * @brief Puerto de entrada para mantener el costo del producto al día con el
 * último precio de compra (HU4 del módulo comercial).
 *
 * Existe separado de {@code IProductServicePort} a propósito: la sincronización
 * desde facturación solo debe poder escribir el costo, sin pasar por la
 * validación de edición completa del producto (nombre, categoría, unicidad)
 * ni por los eventos de sincronización de stock.
 */
public interface IProductCostPort {

    /**
     * @brief Actualiza el costo del producto con el último precio de compra vigente
     * @param productId identificador del producto a actualizar
     * @param cost último precio de compra vigente
     */
    void updateLastPurchaseCost(Long productId, double cost);
}
