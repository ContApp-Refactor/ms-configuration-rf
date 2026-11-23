package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for sale detail
 * 
 * Represents the specific lot usage details in a sale transaction (FIFO allocation).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleDetailDTO {
    private int quantityUsed;        
    private BigDecimal unitPrice;  
    private BigDecimal totalPrice;
}
