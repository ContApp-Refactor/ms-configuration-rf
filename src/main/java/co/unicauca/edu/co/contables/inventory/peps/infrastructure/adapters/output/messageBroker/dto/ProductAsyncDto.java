package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for asynchronous product synchronization
 * 
 * Carries product information for updates or creation events
 * processed asynchronously via the message broker.
 */
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductAsyncDto {
    /**
     * @brief Unique identifier of the product
     */
    private Long productId;

    /**
     * @brief Name of the product
     */
    private String name;

    /**
     * @brief Product reference code
     */
    private String reference;

    /**
     * @brief ID of the enterprise owning the product
     */
    private String enterpriseId;

    /**
     * @brief Product presentation/packaging
     */
    private String presentation;

    /**
     * @brief Initial or current quantity
     */
    private Integer quantity;

    /**
     * @brief Cost of the product
     */
    private double cost;

    /**
     * @brief Active state of the product
     */
    private boolean state;
}
