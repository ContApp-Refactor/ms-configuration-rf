package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexQueryPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.request.KardexFilterDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response.KardexDtoResponse;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response.ListLastProductKardexDtoResponse;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.mapper.IKardexResponseMapper;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

/**
 * @brief REST controller for Kardex inventory movement queries
 * 
 * Provides HTTP endpoints for retrieving inventory movement records
 * with pagination and optional date filtering capabilities.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kardex/weighted-average")
@Validated
public class KardexQueryController {

    private final IKardexQueryPort kardexQueryPort;
    private final IKardexResponseMapper kardexResponseMapper;

    /**
     * @brief Retrieves kardex records for a specific product with pagination
     * @param productId Product identifier to filter records
     * @param filter Optional date range filter
     * @param pageable Pagination parameters
     * @return Paginated response with kardex records
     */
    @GetMapping("/kardex-by-product")
    public ResponseDto<Page<KardexDtoResponse>> getKardexByProductId(
            @RequestParam @NotNull(message = "The product ID is required.") Long productId,
            @Valid @ModelAttribute KardexFilterDto filter,
            Pageable pageable) {

        Page<Kardex> kardexPage = kardexQueryPort.findProductId(productId, pageable, filter.getStartDate(), filter.getEndDate());
        Page<KardexDtoResponse> kardexDtoResponses = kardexPage.map(kardexResponseMapper::toDtoResponse);
        return ResponseDto.<Page<KardexDtoResponse>>builder()
                .data(kardexDtoResponses)
                .status(200)
                .message("Kardex records retrieved successfully").build();
    }

    /**
     * @brief Retrieves the last kardex record for all products of an enterprise
     * @param enterpriseId Enterprise identifier to filter products
     * @return Response with list of last kardex records for each product
     */
    @GetMapping("/last-kardex-all-products")
    public ResponseDto<List<ListLastProductKardexDtoResponse>> getLastKardexForAllProducts(
            @RequestParam @NotNull(message = "The enterprise ID is required.") String enterpriseId) {
        List<Kardex> kardexList = kardexQueryPort.findLastKardexForAllProducts(enterpriseId);
        List<ListLastProductKardexDtoResponse> response = kardexResponseMapper.toListLastProductKardexDtoResponseList(kardexList);
        return ResponseDto.<List<ListLastProductKardexDtoResponse>>builder()
                .data(response)
                .status(200)
                .message("Last kardex records for all products retrieved successfully")
                .build();
    }

    /**
     * @brief Retrieves the most recent kardex record for a specific product
     * @param productId Product identifier
     * @return Response with the latest kardex record
     */
    @GetMapping("/latest-kardex-by-product")
    public ResponseDto<KardexDtoResponse> getLatestKardexByProductId(
            @RequestParam @NotNull(message = "The product ID is required.") Long productId) {
        Kardex kardex = kardexQueryPort.getLatestKardexByProductId(productId);
        KardexDtoResponse response = kardexResponseMapper.toDtoResponse(kardex);
        return ResponseDto.<KardexDtoResponse>builder()
                .data(response)
                .status(200)
                .message("Latest kardex record retrieved successfully")
                .build();
    }
}
