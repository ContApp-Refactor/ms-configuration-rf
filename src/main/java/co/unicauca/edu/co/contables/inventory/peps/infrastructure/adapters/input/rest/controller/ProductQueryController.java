package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.edu.co.contables.inventory.peps.domain.port.output.query.IProductQueryOutputPort;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.ResponseDTO;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.ProductDTOResponse;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.mapper.IProductResponseMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * @brief REST controller for Product query operations
 * 
 * Handles HTTP requests for retrieving product information.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kardex/peps")
public class ProductQueryController {
    private final IProductResponseMapper productResponseMapper;
    private final IProductQueryOutputPort productQueryOutputPort;

    /**
     * @brief Retrieves all products for a specific enterprise
     * 
     * @param enterpriseId The enterprise identifier
     * @return Response with list of products
     */
    @GetMapping("/products/{enterpriseId}")
    public ResponseDTO<List<ProductDTOResponse>> getAllProductsByEnterprise(@PathVariable String enterpriseId) {
        List<ProductDTOResponse> productDtoResponses = productQueryOutputPort.findAll(enterpriseId)
                        .stream().map(productResponseMapper::toDtoResponse).toList();
        
        return ResponseDTO.<List<ProductDTOResponse>>builder()
                .data(productDtoResponses)
                .status(200)
                .message("Products retrieved successfully").build();
    }
    
}
