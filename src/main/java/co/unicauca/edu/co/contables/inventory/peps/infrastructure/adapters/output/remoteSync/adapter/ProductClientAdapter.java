package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.adapter;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;

import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductSyncServicePort;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Product;
import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.external.IProductClientPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.dto.ProductSyncDto;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.mapper.IProductClientMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Adapter for Product Service communication
 * 
 * Antes de la migración llamaba por HTTP a {@code lb://PRODUCTS}
 * (IProductClient + ClientConfig + WebClient). Como el monolito tiene el
 * código de products en proceso, ahora inyecta {@link IProductSyncServicePort}
 * y proyecta su DTO sobre el DTO local que ya conoce {@link IProductClientMapper}.
 *
 * Se descartan los campos que el DTO de products agrega respecto al de peps
 * (quantity, cost): peps no los usa para su sincronización.
 */
@Component("pepsProductClientAdapter")
@RequiredArgsConstructor
@Slf4j
public class ProductClientAdapter  implements IProductClientPort {
    private final IProductClientMapper productClientMapper;
    private final IProductSyncServicePort productSyncServicePort;
    private final IFormatterResultOutputPort formatterResultOutputPort;

    /**
     * @brief Retrieves all products for an enterprise modified after a specific time
     * 
     * Queries the in-process product service for products associated with the
     * enterprise that have been created or modified since the provided timestamp.
     * 
     * @param enterpriseId The unique identifier of the enterprise
     * @param since The timestamp to filter products modified after this time
     * @return List of Product domain objects, or an empty list on error
     */
    @Override
    public List<Product> findAllProductsByEnterpriseId(String enterpriseId, Instant since) {
        try {
            return productSyncServicePort.findByEnterpriseId(enterpriseId, since)
            .stream()
            .map(this::toLocalDto)
            .map(productClientMapper::toDomain)
            .toList();

        } catch(Exception e){
            log.error("Unexpected error when calling product service: {}", e.getMessage());
            formatterResultOutputPort.returnErrorGenericResponse(500,"Unexpected error communicating with product service");    
        }
        return List.of();
    }

    /**
     * Proyecta el DTO de sincronización de products sobre el DTO local de peps.
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
