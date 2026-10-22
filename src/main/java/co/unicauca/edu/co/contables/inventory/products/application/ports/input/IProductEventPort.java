package co.unicauca.edu.co.contables.inventory.products.application.ports.input;

import co.unicauca.edu.co.contables.inventory.products.application.dto.ProductSyncDto;

/**
 * @brief Puerto de entrada para publicación de eventos de productos
 *
 * Define contrato para notificación de cambios en productos a sistemas externos,
 * utilizado para integración con otros servicios y sistemas de mensajería.
 */
public interface IProductEventPort {
    /**
     * @brief Publica evento de creación de stock de producto
     * @param productSyncDto datos del producto para sincronización
     */
    void publishCreatedStockEvent(ProductSyncDto productSyncDto);

    void publishUpdatedStockEvent(ProductSyncDto productSyncDto);

    void publishDeletedStockEvent(ProductSyncDto productSyncDto);

}
