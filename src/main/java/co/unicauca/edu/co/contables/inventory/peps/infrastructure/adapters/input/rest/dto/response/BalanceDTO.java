package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for inventory balance
 * 
 * Represents the remaining quantity and value of a specific lot or total inventory.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BalanceDTO {
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
