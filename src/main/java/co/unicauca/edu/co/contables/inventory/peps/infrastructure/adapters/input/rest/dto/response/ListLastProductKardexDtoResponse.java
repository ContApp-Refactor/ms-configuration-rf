package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.response;

import java.math.BigDecimal;

import co.unicauca.edu.co.contables.inventory.peps.domain.enums.MovementType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for the last kardex record of a product
 * 
 * Used to display the most recent status of a product's inventory.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ListLastProductKardexDtoResponse {
    private Long productId;

    private Long quantity;

    private String factCode;

    private BigDecimal unitPrice;

    private String details;

    private MovementType type;

    private Long balanceQuantity;

    private BigDecimal balanceUnitPrice;

    private BigDecimal totalBalance;
}
