package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.product.IProductQueryPort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response.ProductDtoResponse;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.mapper.IProductResponseMapper;

import lombok.RequiredArgsConstructor;

/**
 * @brief REST controller for product query operations
 * 
 * Provides HTTP endpoints for retrieving product information
 * within enterprise context with proper response formatting.
 */
@RestController("kardexProductQueryController")@RequiredArgsConstructor
@RequestMapping("/api/kardex/weighted-average")
public class ProductQueryController {
    private final IProductResponseMapper productResponseMapper;
    private final IProductQueryPort productQueryPort;

    /**
     * @brief Retrieves all products for a specific enterprise
     * @param enterpriseId Enterprise identifier
     * @return Response with list of products for the enterprise
     */
    @GetMapping("/products/{enterpriseId}")
    public ResponseDto<List<ProductDtoResponse>> getAllProducts(@PathVariable String enterpriseId) {
        List<ProductDtoResponse> productDtoResponses = productQueryPort.findAll(enterpriseId)
                .stream().map(productResponseMapper::toDtoResponse).toList();
        return ResponseDto.<List<ProductDtoResponse>>builder()
                .data(productDtoResponses)
                .status(200)
                .message("Products retrieved successfully").build();
    }
}
