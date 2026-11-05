package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.edu.co.contables.inventory.kardex.application.ports.input.kardex.IKardexCommandPort;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.Kardex;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.ResponseDto;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.request.KardexAdjustmentEntryDtoRequest;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.request.KardexAdjustmentExitDtoRequest;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response.KardexDtoResponse;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.mapper.IKardexResponseMapper;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.mapper.IKardexRestMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * @brief REST controller for Kardex inventory movement commands
 * 
 * Provides HTTP endpoints for registering inventory transactions
 * including purchases and sales with automatic validation.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kardex/weighted-average")
@Validated
public class KardexCommandController {

    private final IKardexCommandPort kardexCommandPort;
    private final IKardexRestMapper kardexRestMapper;
    private final IKardexResponseMapper kardexResponseMapper;

    /**
     * @brief Registers an adjustment entry transaction with custom date validation
     * Accepts optional date field for special adjustment date handling
     * @param kardexAdjustmentEntryDtoRequest Adjustment entry transaction data with optional date
     * @return Response with processed kardex information
     */
    @PostMapping("/purchase-adjustment")
    public ResponseEntity<ResponseDto<KardexDtoResponse>> adjustmentEntry(@Valid @RequestBody KardexAdjustmentEntryDtoRequest kardexAdjustmentEntryDtoRequest) {
        Kardex response = kardexCommandPort.registerAdjustmentEntry(kardexRestMapper.toDomain(kardexAdjustmentEntryDtoRequest));
        KardexDtoResponse kardexDtoResponse = kardexResponseMapper.toDtoResponse(response);
        ResponseDto<KardexDtoResponse> responseDto = ResponseDto.<KardexDtoResponse>builder()
                .data(kardexDtoResponse)
                .status(200)
                .message("Kardex adjustment entry registered successfully").build();
        return responseDto.of();
    }

    /**
     * @brief Registers an adjustment exit transaction with custom date validation
     * Accepts optional date field for special adjustment date handling
     * @param kardexAdjustmentExitDtoRequest Adjustment exit transaction data with optional date
     * @return Response with processed kardex information
     */
    @PostMapping("/sale-adjustment")
    public ResponseEntity<ResponseDto<KardexDtoResponse>> adjustmentExit(@Valid @RequestBody KardexAdjustmentExitDtoRequest kardexAdjustmentExitDtoRequest) {
        Kardex response = kardexCommandPort.registerAdjustmentExit(kardexRestMapper.toDomain(kardexAdjustmentExitDtoRequest));
        KardexDtoResponse kardexDtoResponse = kardexResponseMapper.toDtoResponse(response);
        ResponseDto<KardexDtoResponse> responseDto = ResponseDto.<KardexDtoResponse>builder()
                .data(kardexDtoResponse)
                .status(200)
                .message("Kardex adjustment exit registered successfully").build();
        return responseDto.of();    
    }

    /**
     * @brief Deletes all kardex records
     * @return Response confirming deletion
     */
    @DeleteMapping("/delete-all")
    public ResponseEntity<ResponseDto<Void>> deleteAllKardex() {
        kardexCommandPort.deleteAll();
        
        ResponseDto<Void> responseDto = ResponseDto.<Void>builder()
                .data(null)
                .status(200)
                .message("All kardex records deleted successfully")
                .build();
        return responseDto.of();
    }
}
