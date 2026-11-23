package co.unicauca.edu.co.contables.inventory.peps.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Represents the detail of an output movement
 * 
 * Links a sale movement to the specific origin movement (purchase) 
 * that supplied the goods, enforcing FIFO logic.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetailOutput {
    private Long idDetailOutput;
    private int quantityUsed;
    private BigDecimal unitPrice;
    private Kardex movementSale;
    private Kardex movementOrigin;


    
    /**
     * @brief Creates a new output detail
     * @param amountUsed Quantity consumed from the origin movement
     * @param unitPrice Unit price of the origin movement
     * @param movementSale The sale movement
     * @param movementOrigin The origin movement (purchase)
     * @return New DetailOutput instance
     */
     public static DetailOutput create(int amountUsed, BigDecimal unitPrice, 
                                    Kardex movementSale, Kardex movementOrigin) {
        validateAmountUsed(amountUsed);
        validateUnitPrice(unitPrice);
        Objects.requireNonNull(movementSale, "Sale movement cannot be null");
        Objects.requireNonNull(movementOrigin, "Origin movement cannot be null");
        validateFIFORule(movementOrigin, movementSale);
        
        DetailOutput detail = new DetailOutput();
        detail.setQuantityUsed(amountUsed);
        detail.setUnitPrice(unitPrice);
        detail.setMovementSale(movementSale);
        detail.setMovementOrigin(movementOrigin);
        
        return detail;
    }

    /**
     * @brief Calculates the total value of this detail
     * @return Total value (quantity * unit price)
     */
    public BigDecimal getTotalValue() {
        return this.unitPrice.multiply(BigDecimal.valueOf(this.quantityUsed));
    }

    /**
     * @brief Checks if this detail belongs to a specific sale movement
     * @param saleMovement Movement to check
     * @return true if it belongs to the sale
     */
    public boolean belongsToSaleMovement(Kardex saleMovement) {
        return this.movementSale != null && this.movementSale.equals(saleMovement);
    }

    /**
     * @brief Validates FIFO compliance
     * @return true if origin date is before or equal to sale date
     */
    public boolean isValidFIFO() {
        if (movementOrigin == null || movementSale == null) return false;
        return this.movementOrigin.getDate().isBefore(this.movementSale.getDate()) ||
               this.movementOrigin.getDate().isEqual(this.movementSale.getDate());
    }

    /**
     * @brief Checks if origin and sale movements belong to the same product
     * @return true if product IDs match
     */
    public boolean belongsToSameProduct() {
        if (movementOrigin == null || movementSale == null) return false;
        if (movementOrigin.getProduct() == null || movementSale.getProduct() == null) return false;
        
        return this.movementOrigin.getProduct().getProductId()
               .equals(this.movementSale.getProduct().getProductId());
    }

    /**
     * @brief Validates consistency of the detail
     * @throws IllegalArgumentException if validation fails
     */
    public void validateConsistency() {
        if (!isValidFIFO()) {
            throw new IllegalArgumentException("FIFO violation: origin movement is newer than sale movement");
        }
        if (!belongsToSameProduct()) {
            throw new IllegalArgumentException("Origin and sale movements must belong to the same product");
        }
        if (!movementOrigin.canReduceQuantity(quantityUsed)) {
            throw new IllegalArgumentException("Origin movement does not have enough available quantity");
        }
    }

     // Métodos de validación estáticos
    private static void validateAmountUsed(int amountUsed) {
        if (amountUsed <= 0) {
            throw new IllegalArgumentException("Amount used must be greater than zero");
        }
    }
    
    private static void validateUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
    }
    
    private static void validateFIFORule(Kardex origin, Kardex sale) {
        if (origin.getDate() != null && sale.getDate() != null && 
            origin.getDate().isAfter(sale.getDate())) {
            throw new IllegalArgumentException("FIFO violation: origin movement is newer than sale movement");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetailOutput that = (DetailOutput) o;
        return Objects.equals(idDetailOutput, that.idDetailOutput);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(idDetailOutput);
    }
    
    @Override
    public String toString() {
        return String.format("DetailOutput{id=%d, amountUsed=%d, unitPrice=%s}", 
            idDetailOutput, quantityUsed, unitPrice);
    }
    

}
