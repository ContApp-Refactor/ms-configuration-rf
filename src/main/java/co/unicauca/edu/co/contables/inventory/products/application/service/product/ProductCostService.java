package co.unicauca.edu.co.contables.inventory.products.application.service.product;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductCostPort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Sincroniza el costo del producto con el último precio de compra (HU4).
 *
 * El costo se recalcula desde las facturas de compra vigentes y se escribe en el
 * producto, de modo que las pantallas de factura de compra, factura de venta,
 * ajustes de inventario y facturación puedan leerlo como un campo simple.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCostService implements IProductCostPort {

    private final IProductPersistencePort productPersistencePort;

    @Override
    public void updateLastPurchaseCost(Long productId, double cost) {
        if (productId == null) {
            return;
        }
        productPersistencePort.findById(productId).ifPresentOrElse(product -> {
            product.setCost(cost);
            productPersistencePort.create(product);
            log.debug("Producto {} actualizado con ultimo precio de compra {}", productId, cost);
        }, () -> log.debug("Producto {} inexistente; se omite la sincronizacion del costo", productId));
    }
}
