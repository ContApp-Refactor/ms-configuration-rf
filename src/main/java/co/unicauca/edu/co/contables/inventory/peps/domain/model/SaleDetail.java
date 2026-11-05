package co.unicauca.edu.co.contables.inventory.peps.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Represents details of a sale transaction
 * 
 * Captures the quantity and price information for a specific part of a sale,
 * typically corresponding to a specific lot consumed.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleDetail {
    private int quantityUsed;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    
    /**
     * @brief Creates a sale detail from lot consumption
     * @param quantityUsed Quantity consumed
     * @param unitPrice Unit price of the lot
     * @return New SaleDetail
     */
    public static SaleDetail fromLotConsumption(int quantityUsed, BigDecimal unitPrice) {
        validateQuantityUsed(quantityUsed);
        validateUnitPrice(unitPrice);
        
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantityUsed));
        
        return SaleDetail.builder()
            .quantityUsed(quantityUsed)
            .unitPrice(unitPrice)
            .totalPrice(total)
            .build();
    }
    
    /**
     * @brief Creates a sale detail from a Balance object
     * @param balance Source balance
     * @param quantityConsumed Quantity to consume
     * @return New SaleDetail
     */
    public static SaleDetail fromBalance(Balance balance, int quantityConsumed) {
        if (!balance.canSatisfyDemand(quantityConsumed)) {
            throw new IllegalArgumentException("Balance cannot satisfy demand");
        }
        return fromLotConsumption(quantityConsumed, balance.getUnitPrice());
    }
    
    /**
     * @brief Calculates the average unit price
     * @return Average price (Total / Quantity)
     */
    public BigDecimal getAverageUnitPrice() {
        if (quantityUsed <= 0) {
            return BigDecimal.ZERO;
        }
        return totalPrice.divide(BigDecimal.valueOf(quantityUsed), 2, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * @brief Recalculates total price based on quantity and unit price
     */
    public void recalculateTotal() {
        this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(this.quantityUsed));
    }
    
    /**
     * @brief Checks if the detail is valid
     * @return true if quantity > 0 and prices are valid
     */
    public boolean isValid() {
        return quantityUsed > 0 && 
               unitPrice != null && 
               unitPrice.compareTo(BigDecimal.ZERO) >= 0 &&
               totalPrice != null;
    }
    
    // Validaciones privadas
    private static void validateQuantityUsed(int quantityUsed) {
        if (quantityUsed <= 0) {
            throw new IllegalArgumentException("Quantity used must be positive");
        }
    }
    
    private static void validateUnitPrice(BigDecimal unitPrice) {
        Objects.requireNonNull(unitPrice, "Unit price cannot be null");
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SaleDetail that = (SaleDetail) o;
        return quantityUsed == that.quantityUsed &&
               Objects.equals(unitPrice, that.unitPrice) &&
               Objects.equals(totalPrice, that.totalPrice);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(quantityUsed, unitPrice, totalPrice);
    }
    
    @Override
    public String toString() {
        return String.format("SaleDetail{quantityUsed=%d, unitPrice=%s, totalPrice=%s}", 
            quantityUsed, unitPrice, totalPrice);
    }

}
