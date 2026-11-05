package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IProductCommandPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.ResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief REST controller for Product command operations
 * 
 * Handles HTTP requests for managing product lifecycle (deletion).
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kardex/peps")
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
    public ResponseEntity<ResponseDTO<String>> deleteProduct(@PathVariable Long productId ) {

            log.info("Deleting product with ID {}", productId);
            String result = productCommandPort.deleteById(productId);
            
            return ResponseEntity.ok(ResponseDTO.<String>builder()
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
    public ResponseEntity<ResponseDTO<String>> deleteAllProducts(@PathVariable String enterpriseId) {
        log.info("Deleting all products for enterprise {}", enterpriseId);
        String result = productCommandPort.deleteAllByEnterpriseId(enterpriseId);
        
        return ResponseEntity.ok(ResponseDTO.<String>builder()
            .data(result)
            .status(200)
            .message("Products deleted successfully")
            .build());
    }

}
