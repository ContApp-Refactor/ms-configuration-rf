package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import co.unicauca.edu.co.contables.inventory.stock.application.ports.input.IProductSyncCommandPort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;

import lombok.RequiredArgsConstructor;

/**
 * @brief REST controller for product synchronization operations
 * 
 * Provides HTTP endpoints for triggering product synchronization
 * from external product management systems.
 */
@RestController("kardexProductSyncController")@RequiredArgsConstructor
@RequestMapping("/api/kardex/weighted-average/sync")
public class ProductSyncController {
    @Qualifier("kardexProductCommandService")
    private final IProductSyncCommandPort productCommandPort;

    /**
     * @brief Triggers product synchronization for an enterprise
     * @param enterpriseId Enterprise identifier to sync products for
     * @return Response with synchronization result status
     */
    @GetMapping("/products/{enterpriseId}")
    public ResponseEntity<ResponseDto<String>> syncProducts(@PathVariable String enterpriseId) {
        String result = productCommandPort.syncProductsByEnterpriseId(enterpriseId);
        ResponseDto<String> responseDto = ResponseDto.<String>builder()
                .data(result)
                .status(200)
                .message("Synchronization completed").build();
        return ResponseEntity.ok(responseDto);
    }
}
