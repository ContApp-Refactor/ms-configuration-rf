package co.unicauca.edu.co.contables.inventory.products.application.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.products.application.dto.ProductSyncDto;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductSyncServicePort;
import co.unicauca.edu.co.contables.inventory.products.application.ports.output.IProductSyncPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSyncService implements IProductSyncServicePort{

    private final IProductSyncPort productSyncPort;

    @Override
    public List<ProductSyncDto> findByEnterpriseId(String enterpriseId, Instant since) {
        log.info("Finding products for enterpriseId: {} since: {}", enterpriseId, since);
        return productSyncPort.findByEnterpriseId(enterpriseId, since);
    }
    
}
