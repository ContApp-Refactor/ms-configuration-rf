package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.response;

import java.math.BigDecimal;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
