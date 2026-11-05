package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.request;


import java.time.ZonedDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for inventory adjustment exit request
 * 
 * Represents the data required to register a negative inventory adjustment.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdjustmentExitDTORequest {
    
    private String details;
    @NotNull(message = "The field 'quantity' cannot be null")
    @Positive(message = "The quantity must be positive")
    private int quantity;

    @NotNull(message = "The field 'productId' cannot be null")
    @Positive(message = "The productId must be positive")
    private Long productId;

     
    private ZonedDateTime date;
}
