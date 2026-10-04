package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.remoteSync.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class ProductSyncDto {
    private Long productId;
    private String name;
    private String reference;
    private String enterpriseId;
    private String presentation;
    private boolean state;

}
