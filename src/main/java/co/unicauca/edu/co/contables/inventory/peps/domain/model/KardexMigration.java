package co.unicauca.edu.co.contables.inventory.peps.domain.model;

import java.math.BigDecimal;
import co.unicauca.edu.co.contables.inventory.peps.domain.enums.MovementType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Represents a Kardex record during migration
 * 
 * Used to transfer inventory state between systems or versions.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class KardexMigration {
    
    private Long productId;

    private Long quantity;

    private String factCode;

    private BigDecimal unitPrice;

    private String details;

    private MovementType type;

    private Long balanceQuantity;

    private BigDecimal balanceUnitPrice;

    private BigDecimal totalBalance;


    /**
     * @brief Validates that the migration data is consistent
     * @throws IllegalStateException if data is invalid
     */
    public void validate() {
        if (productId == null) {
            throw new IllegalStateException("Product ID cannot be null");
        }
        if (quantity != null && quantity < 0) {
            throw new IllegalStateException("Quantity cannot be negative");
        }
        if (balanceQuantity != null && balanceQuantity < 0) {
            throw new IllegalStateException("Balance quantity cannot be negative");
        }
        if (unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Unit price cannot be negative");
        }
    }

    /**
     * @brief Checks if there is stock in the balance
     * @return true if balance quantity > 0
     */
    public boolean hasBalanceStock() {
        return balanceQuantity != null && balanceQuantity > 0;
    }

    /**
     * @brief Calculates the total value of the original movement
     * @return Total value (quantity * unit price)
     */
    public BigDecimal calculateMovementValue() {
        if (quantity == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }


}
