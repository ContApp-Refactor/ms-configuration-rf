package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for stock sale request
 * 
 * Data transfer object sent to the Stock Service to register a stock decrease.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StockSellDtoRequest {
    private Long productId;

    private int quantity;

    private BigDecimal price;
}
