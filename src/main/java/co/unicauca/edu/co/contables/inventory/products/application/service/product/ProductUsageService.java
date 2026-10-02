package co.unicauca.edu.co.contables.inventory.products.application.service.product;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductUsagePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductPersistencePort;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.product.ProductNotFoundException;
import co.unicauca.edu.co.contables.inventory.products.domain.model.Product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Servicio para gestión de uso de productos
 *
 * Maneja la lógica de negocio relacionada con el contador de uso de productos
 * cuando son utilizados por otros servicios.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductUsageService implements IProductUsagePort {

    private final IProductPersistencePort productPersistencePort;

    @Override
    public void incrementUsageCount(Long productId) {
        log.info("Incrementing usage count for productId: {}", productId);

        Product product = productPersistencePort.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found: {}", productId);
                    return new ProductNotFoundException();
                });

        product.incrementUsageCount();
        productPersistencePort.create(product);

        log.info("Usage count incremented successfully for productId: {}. New count: {}",
                 productId, product.getUsageCount());
    }
}

