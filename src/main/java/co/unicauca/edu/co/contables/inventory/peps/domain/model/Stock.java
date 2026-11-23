package co.unicauca.edu.co.contables.inventory.peps.domain.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Represents current stock information
 * 
 * A simplified view of inventory for a product in an enterprise.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @Builder
public class Stock {
    private Long id;

    private Long productId;

    private String enterpriseId;

    private int quantity;

    private BigDecimal price;

    private boolean status;  
}
