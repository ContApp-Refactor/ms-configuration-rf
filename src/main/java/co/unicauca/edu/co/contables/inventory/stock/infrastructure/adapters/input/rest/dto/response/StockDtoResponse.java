package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.input.rest.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class StockDtoResponse {
    private Long id;

    private Long productId;

    private String name;

    private String enterpriseId;

    private int quantity;

    private BigDecimal price;

    private boolean state;   
}
