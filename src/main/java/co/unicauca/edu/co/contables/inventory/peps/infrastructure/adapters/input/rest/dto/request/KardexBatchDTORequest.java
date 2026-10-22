package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.input.rest.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import co.unicauca.edu.co.contables.inventory.peps.domain.enums.MovementType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for batch kardex processing request
 * 
 * Represents a single kardex movement record within a batch processing request.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class KardexBatchDTORequest {
    @NotNull(message = "The field 'details' cannot be null")
    private String details;
    @NotNull(message = "The field 'quantity' cannot be null")
    @Positive(message = "The quantity must be positive")
    private int quantity;

    private BigDecimal unitPrice;
    @NotNull(message = "The field 'factCode' cannot be null")
    private String factCode;
     
    @NotNull(message = "The field 'productId' cannot be null")
    @Positive(message = "The productId must be positive")
    private Long productId; 

     
    @NotNull(message = "Type cannot be null")
    private MovementType type;
    
}
