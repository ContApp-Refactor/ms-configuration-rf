package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import co.unicauca.edu.co.contables.inventory.peps.application.ports.input.IKardexQueryPort;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.KardexMigration;
import co.unicauca.edu.co.contables.inventory.peps.domain.model.KardexReport;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.ResponseDTO;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.request.KardexByDateDTORequest;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.KardexAvailableQuantityDTOResponse;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.KardexRecordsDTOResponse;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.ListLastProductKardexDtoResponse;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response.PageResponseDTO;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.mapper.IKardexRestMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * @brief REST controller for Kardex PEPS query operations
 * 
 * Handles HTTP requests for retrieving kardex records, reports, and inventory status.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kardex/peps")
public class KardexPEPSQueryController {

    private final IKardexQueryPort kardexQueryPort;
    private final IKardexRestMapper kardexRestMapper;

    /**
     * @brief Retrieves a paginated list of kardex records filtered by date
     * 
     * @param productId The product identifier
     * @param kardex Filter criteria (start date, end date)
     * @param pageable Pagination information
     * @return Response with paginated kardex records
     */
    @GetMapping("/kardexlist")
    public ResponseDTO<PageResponseDTO<KardexRecordsDTOResponse>> getKardexListPEPSByDate(
        @RequestParam @NotNull(message = "The product ID is required.") Long productId,
        @Valid @ModelAttribute KardexByDateDTORequest kardex, 
        Pageable pageable) {

        // 1. Llama al servicio
        Page<KardexReport> kardexReports = kardexQueryPort.getRecordsKardexByProduct(
            productId, 
            kardex.getStartDate(), 
            kardex.getEndDate(), 
            pageable
        );
        
        // 2. Mapea la página a DTOs
        Page<KardexRecordsDTOResponse> kardexRecordsPage = kardexReports.map(kardexRestMapper::toDTORecord);

        // 3. Convierte a PageResponseDTO (estructura JSON estable)
        PageResponseDTO<KardexRecordsDTOResponse> pageResponse = PageResponseDTO.fromPage(kardexRecordsPage);

        // 4. Construye la respuesta
        return ResponseDTO.<PageResponseDTO<KardexRecordsDTOResponse>>builder()
                .data(pageResponse)
                .status(200)
                .message("Kardex records retrieved successfully")
                .build();
    }

    /**
     * @brief Retrieves available quantity details for a product (FIFO lots)
     * 
     * @param productId The product identifier
     * @return Response with list of available quantities per lot
     */
    @GetMapping("/kardex-available-quantity/{productId}")
    public ResponseEntity<ResponseDTO<List<KardexAvailableQuantityDTOResponse>>> getAvailable(@PathVariable Long productId) {

        List<Kardex> listResponse=kardexQueryPort.getKardexAvailableQuantityByProduct(productId);


        List<KardexAvailableQuantityDTOResponse> listKardexPurchaseDTOResponse=kardexRestMapper.toKardexDTO(listResponse);
        ResponseDTO<List<KardexAvailableQuantityDTOResponse>> listResponseDTO=ResponseDTO.<List<KardexAvailableQuantityDTOResponse>>builder()
        .data(listKardexPurchaseDTOResponse)
        .status(200)
        .message("kardex purchase registered sucesfully").build();
        return  listResponseDTO.of();


    }
    

     /**
     * @brief Retrieves the last kardex record for all products of an enterprise
     * @param enterpriseId Enterprise identifier to filter products
     * @return Response with list of last kardex records for each product
     */
    @GetMapping("/last-kardex-peps-all-products")
    public ResponseDTO<List<ListLastProductKardexDtoResponse>> getLastKardexForAllProducts(
        @RequestParam @NotNull(message = "The enterprise ID is required.") String enterpriseId) {
        List<KardexMigration> KardexMigrations=kardexQueryPort.findLastKardexForAllProducts(enterpriseId);
        List<ListLastProductKardexDtoResponse> response=kardexRestMapper.toListLastProductKardexDtoResponseList(KardexMigrations);

        return ResponseDTO.<List<ListLastProductKardexDtoResponse>>builder()
                .data(response)
                .status(200)
                .message("Last kardex records for all products retrieved successfully")
                .build();

    }
    

   
    
}
