package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for product usage events received from PEPS
 *
 * Contains the necessary information to update the usage counter
 * when PEPS notifies that a product has been used.
 */
@Getter 
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductUsageEventDto {
    /**
     * @brief ID of the product used
     */
    private Long productId;

    /**
     * @brief ID of the enterprise
     */
    private String enterpriseId;

    /**
     * @brief Quantity of product used
     */
    private Integer quantityUsed;
}

