package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.messageBroker.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class KardexRabbitDto{

    private Long quantity;

    private Long factCode;

    private BigDecimal unitPrice;

    private Long productId;

    private String details;
}
