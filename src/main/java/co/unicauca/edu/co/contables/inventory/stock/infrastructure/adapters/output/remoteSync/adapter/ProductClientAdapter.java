package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.remoteSync.adapter;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;


import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductSyncServicePort;
import co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IProductClientPort;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.remoteSync.dto.ProductSyncDto;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.remoteSync.mapper.IProductClientMapper;

import lombok.RequiredArgsConstructor;

/**
 * @brief Adaptador de sincronización de productos
 *
 * Antes de la migración llamaba por HTTP a {@code lb://PRODUCTS}
 * (IProductClient + ClientConfig + WebClient con propagación de JWT). Como el
 * monolito tiene el código de products en proceso, ahora inyecta el puerto de
 * aplicación {@link IProductSyncServicePort} y mapea su DTO al DTO local que ya
 * conoce {@link IProductClientMapper}.
 *
 * Se descartan los campos que el DTO de products agrega respecto al de stock
 * (quantity, cost): la cantidad y el precio de cada stock los gobierna el
 * módulo de stock, no el de productos.
 */
@Component
@RequiredArgsConstructor
public class ProductClientAdapter implements IProductClientPort {

    private final IProductClientMapper productClientMapper;
    private final IProductSyncServicePort productSyncServicePort;

    @Override
    public List<Stock> findAllProductsByEnterpriseId(String enterpriseId, Instant since) {
        return productSyncServicePort.findByEnterpriseId(enterpriseId, since)
                .stream()
                .map(product -> productClientMapper.toDomain(toLocalDto(product)))
                .toList();
    }

    /**
     * Proyecta el DTO de sincronización de products sobre el DTO local de stock.
     * Los nombres de los campos comunes coinciden, por eso solo se copian.
     */
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
