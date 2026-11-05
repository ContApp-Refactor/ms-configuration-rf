package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.remoteSync.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class StockSellDtoRequest {

    private Long productId;

    private int quantity;

    private BigDecimal price;
}
