package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.request;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for receiving adjustment entry operations in the Kardex system
 * Contains fields necessary for registering a product adjustment entry with custom date
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class KardexAdjustmentEntryDtoRequest {
    @NotNull(message = "{kardex.validation.quantity.notnull}")
    @Positive(message = "{kardex.validation.quantity.positive}")
    private Long quantity;

    @NotNull(message = "{kardex.validation.unitprice.notnull}")
    @Positive(message = "{kardex.validation.unitprice.positive}")
    private BigDecimal unitPrice;

    @NotNull(message = "{kardex.validation.productid.notnull}")
    @Positive(message = "{kardex.validation.productid.positive}")
    private Long productId;

    private String details;
    
    private ZonedDateTime date; // Puede ser nula
 
    private final MovementType type = MovementType.ADJUSTMENTENTRY;
}
