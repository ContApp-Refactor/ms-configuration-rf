package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.messageBroker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductAsyncDto {
    private Long productId;
    private String name;
    private String enterpriseId;
}
