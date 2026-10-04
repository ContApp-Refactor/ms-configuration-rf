package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.remoteSync.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for stock service response
 * 
 * Represents the stock information returned by the Stock Service.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class StockDtoResponse {
    private Long id;

    private Long productId;

    private String enterpriseId;

    private int quantity;

    private BigDecimal price;

    private boolean status;   
    
}
