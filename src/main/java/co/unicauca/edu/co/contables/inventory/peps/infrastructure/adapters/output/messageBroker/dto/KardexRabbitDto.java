package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.messageBroker.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for Kardex messages in RabbitMQ
 * 
 * Represents the data structure for inventory movement messages
 * sent to or received from the message broker.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KardexRabbitDto {
    /**
     * @brief Quantity of items in the movement
     */
    private Long quantity;

    /**
     * @brief Invoice or reference code
     */
    private String factCode;

    /**
     * @brief Unit price of the product
     */
    private BigDecimal unitPrice;

    /**
     * @brief ID of the product involved
     */
    private Long productId;

    /**
     * @brief Additional details or description
     */
    private String details;
}
