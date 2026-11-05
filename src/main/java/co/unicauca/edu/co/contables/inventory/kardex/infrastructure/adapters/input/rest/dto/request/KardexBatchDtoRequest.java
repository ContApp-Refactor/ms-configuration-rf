package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.input.rest.dto.request;

import java.math.BigDecimal;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief DTO for batch kardex processing request
 * 
 * Represents a single kardex record to be processed in batch
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class KardexBatchDtoRequest {
    
    @NotNull(message = "Product ID cannot be null")
    private Long productId;
    
    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
    
    @NotNull(message = "Fact code cannot be null")
    private String factCode;
    
    @NotNull(message = "Unit price cannot be null")
    @Positive(message = "Unit price must be positive")
    private BigDecimal unitPrice;
    
    private String details;
    
    @NotNull(message = "Type cannot be null")
    private MovementType type;
    
    private Integer balanceQuantity;
    
    private BigDecimal balanceUnitPrice;
    
    private BigDecimal totalBalance;
}
