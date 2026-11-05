package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.rest.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.edu.co.contables.inventory.products.application.dto.ProductSyncDto;
import co.unicauca.edu.co.contables.inventory.products.application.ports.input.IProductSyncServicePort;


import lombok.RequiredArgsConstructor;

/**
 * @brief Controlador REST para sincronización de productos
 *
 * Expone endpoints para sincronización de datos de productos con sistemas
 * externos, permitiendo consultas basadas en tiempo y empresa.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products/sync")
public class ProductRestSyncController {
    private final IProductSyncServicePort productSyncServicePort;

    @GetMapping("/findByEnterpriseId/{enterpriseId}")
    public List<ProductSyncDto> findByEnterpriseId(
            @PathVariable String enterpriseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since)
    {   
        List<ProductSyncDto> productSyncDtos = productSyncServicePort.findByEnterpriseId(enterpriseId, since);
        return productSyncDtos;
    }

}
