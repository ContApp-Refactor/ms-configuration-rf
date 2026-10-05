package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.product.IProductCommandPort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief REST controller for product command operations
 * 
 * Provides HTTP endpoints for product modification operations
 * including deletion with proper response formatting.
 */
@RestController("kardexProductCommandController")@RequiredArgsConstructor
@RequestMapping("/api/kardex/weighted-average")
@Slf4j
public class ProductCommandController {
    
    private final IProductCommandPort productCommandPort;

    /**
     * @brief Deletes a specific product by ID and enterprise ID
     * @param productId Product identifier
     * @param enterpriseId Enterprise identifier
     * @return Response indicating deletion result
     */
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<ResponseDto<String>> deleteProduct(
            @PathVariable Long productId) {

            log.info("Deleting product with ID {} for enterprise {}", productId);
            String result = productCommandPort.deleteById(productId);
            
            return ResponseEntity.ok(ResponseDto.<String>builder()
                .data(result)
                .status(200)
                .message("Product deleted successfully")
                .build());
    }

    /**
     * @brief Deletes all products for a specific enterprise
     * @param enterpriseId Enterprise identifier
     * @return Response indicating deletion result
     */
    @DeleteMapping("/products/enterprise/{enterpriseId}")
    public ResponseEntity<ResponseDto<String>> deleteAllProducts(@PathVariable String enterpriseId) {
        log.info("Deleting all products for enterprise {}", enterpriseId);
        String result = productCommandPort.deleteAllByEnterpriseId(enterpriseId);
        
        return ResponseEntity.ok(ResponseDto.<String>builder()
            .data(result)
            .status(200)
            .message("Products deleted successfully")
            .build());
    }
}
