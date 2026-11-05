package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.input.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.unicauca.edu.co.contables.inventory.stock.application.ports.input.IStockCommandPort;
import co.unicauca.edu.co.contables.inventory.stock.application.ports.input.IStockQueryPort;
import co.unicauca.edu.co.contables.inventory.stock.application.ports.input.IStockStatusPort;
import co.unicauca.edu.co.contables.inventory.stock.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.input.rest.dto.ResponseDto;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.input.rest.dto.request.StockBuyDtoRequest;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.input.rest.dto.request.StockSellDtoRequest;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.input.rest.dto.response.StockDtoResponse;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.input.rest.mapper.IStockRestMapper;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stock")
@Validated
public class StockController {
    
    private final IStockCommandPort stockCommandPort;
    private final IStockQueryPort stockQueryPort;
    private final IStockRestMapper stockRestMapper;
    private final IStockStatusPort stockStatusPort;

    @PutMapping("/buy")
    public ResponseEntity<ResponseDto<StockDtoResponse>> createStock(@Valid @RequestBody StockBuyDtoRequest stockDtoRequest) {
        Stock stock = stockRestMapper.toDomain(stockDtoRequest);
        Stock createdStock = stockCommandPort.registerPurchase(stock);
        StockDtoResponse stockDtoResponse = stockRestMapper.toDtoResponse(createdStock);
        return ResponseDto.<StockDtoResponse>builder()
                .data(stockDtoResponse)
                .status(200)
                .message("Stock bought successfully").build().of();
    }

    @PutMapping("/sell")
    public ResponseEntity<ResponseDto<StockDtoResponse>> sellStock(@Valid @RequestBody StockSellDtoRequest stockDtoRequest) {
        Stock stock = stockRestMapper.toDomain(stockDtoRequest);
        Stock updatedStock = stockCommandPort.registerSale(stock);
        StockDtoResponse stockDtoResponse = stockRestMapper.toDtoResponse(updatedStock);
        return ResponseDto.<StockDtoResponse>builder()
                .data(stockDtoResponse)
                .status(200)
                .message("Stock sold successfully").build().of();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<StockDtoResponse>> getStockById(@NotNull @PathVariable Long id) {
        Stock stock = stockQueryPort.findByProductId(id);
        StockDtoResponse stockDtoResponse = stockRestMapper.toDtoResponse(stock);
        return ResponseDto.<StockDtoResponse>builder()
                .data(stockDtoResponse)
                .status(200)
                .message("Stock retrieved successfully").build().of();
        }

    @PutMapping("/status/activate/{id}")
    public ResponseEntity<ResponseDto<String>> updateStockStatus(@NotNull @PathVariable Long id) {
        stockStatusPort.activate(id);
        return ResponseDto.<String>builder()
                .data("Stock status updated successfully")
                .status(200)
                .message("Stock status updated successfully").build().of();
    }

    @PutMapping("/status/deactivate/{id}")
    public ResponseEntity<ResponseDto<String>> deactivateStockStatus(@NotNull @PathVariable Long id) {
        stockStatusPort.inactivate(id);
        return ResponseDto.<String>builder()
                .data("Stock status updated successfully")
                .status(200)
                .message("Stock status updated successfully").build().of();
    }

    @DeleteMapping("/all/{enterpriseId}")
    public ResponseEntity<ResponseDto<String>> deleteAllStockByEnterpriseId(@NotNull @PathVariable String enterpriseId) {
        String result = stockCommandPort.deleteAllByEnterpriseId(enterpriseId);
        return ResponseDto.<String>builder()
                .data(result)
                .status(200)
                .message("Delete all operation completed for enterprise").build().of();
    }

}
