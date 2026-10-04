package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.mapper;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Stock;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.dto.StockBuyDtoRequest;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.dto.StockDtoResponse;
import co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.dto.StockSellDtoRequest;

/**
 * @brief Mapper for Stock service DTOs
 * 
 * Handles conversion between Stock domain models and various DTOs
 * used for communicating with the external Stock Service.
 */
@Mapper(componentModel = "spring")
public interface IStockClientMapper {
    
    /**
     * @brief Converts Stock domain to Buy Request DTO
     * @param stock The stock domain object
     * @return DTO for buy request
     */
    StockBuyDtoRequest toDtoRequest(Stock stock);

    /**
     * @brief Converts Stock Response DTO to domain model
     * @param dto The stock response DTO
     * @return The stock domain object
     */
    Stock toDomain(StockDtoResponse dto);

    /**
     * @brief Converts Stock domain to Sell Request DTO
     * @param stock The stock domain object
     * @return DTO for sell request
     */
    StockSellDtoRequest toSellDtoRequest(Stock stock);
}
