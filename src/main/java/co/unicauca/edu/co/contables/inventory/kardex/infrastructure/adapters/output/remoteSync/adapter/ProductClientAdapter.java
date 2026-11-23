package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.remoteSync.adapter;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.product.IProductClientPort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.remoteSync.mapper.IProductClientMapper;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductSyncServicePort;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.remoteSync.dto.ProductSyncDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief In-process adapter for the products module (paso 7).
 *
 * Replaces the former WebClient proxy to lb://PRODUCTS: products already
 * lives in this monolith, so the incremental sync is served by
 * {@link IProductSyncServicePort} directly.
 */
@Component("kardexProductClientAdapter")
@RequiredArgsConstructor
@Slf4j
public class ProductClientAdapter implements IProductClientPort {

    private final IProductClientMapper productClientMapper;
    private final IProductSyncServicePort productSyncServicePort;
    private final IFormatterResultOutputPort formatterResultOutputPort;

    @Override
    public List<Product> findAllProductsByEnterpriseId(String enterpriseId, Instant since) {
        try {
            return productSyncServicePort.findByEnterpriseId(enterpriseId, since)
                    .stream()
                    .map(this::toLocalDto)
                    .map(productClientMapper::toDomain)
                    .toList();
        } catch (Exception e) {
            log.error("Unexpected error when calling product service: {}", e.getMessage());
            formatterResultOutputPort.returnErrorGenericResponse(500,
                    "Unexpected error communicating with product service");
        }
        return List.of();
    }

    private ProductSyncDto toLocalDto(
            co.unicauca.edu.co.contables.inventory.products.application.dto.ProductSyncDto source) {
        return new ProductSyncDto(
                source.getProductId(),
                source.getName(),
                source.getReference(),
                source.getEnterpriseId(),
                source.getPresentation(),
                source.isState());
    }
}
