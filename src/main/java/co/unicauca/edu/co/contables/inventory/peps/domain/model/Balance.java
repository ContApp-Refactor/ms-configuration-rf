package co.unicauca.edu.co.contables.inventory.peps.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Represents a balance entry in the inventory
 * 
 * Tracks the quantity and price of a specific lot of goods, used for
 * valuation and FIFO calculations.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Balance {
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

  
    /**
     * @brief Creates a balance from a movement
     * @param quantity Quantity of the movement
     * @param unitPrice Unit price of the movement
     * @return New Balance instance
     */
    public static Balance fromMovement(int quantity, BigDecimal unitPrice) {
        validateQuantity(quantity);
        validateUnitPrice(unitPrice);
        
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));
        
        return Balance.builder()
            .quantity(quantity)
            .unitPrice(unitPrice)
            .totalPrice(total)
            .build();
    }
    
    /**
     * @brief Creates a deep copy of a balance
     * @param original Balance to copy
     * @return New Balance instance with same values
     */
    public static Balance copy(Balance original) {
        if (original == null) {
            return null;
        }
        return new Balance(original.quantity, original.unitPrice, original.totalPrice);
    }
    
    /**
     * @brief Reduces the quantity of the balance
     * 
     * Used when consuming stock from this specific lot (FIFO).
     * Updates total price accordingly.
     * 
     * @param amountToReduce Amount to subtract
     * @throws IllegalArgumentException if amount is invalid or exceeds available quantity
     */
    public void reduceQuantity(int amountToReduce) {
        if (amountToReduce <= 0) {
            throw new IllegalArgumentException("Amount to reduce must be positive");
        }
        if (this.quantity < amountToReduce) {
            throw new IllegalArgumentException(
                String.format("Cannot reduce %d from balance with only %d available", 
                    amountToReduce, this.quantity)
            );
        }
        
        this.quantity -= amountToReduce;
        this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }
    
    /**
     * @brief Checks if the balance can satisfy a demand
     * @param demandQuantity Quantity required
     * @return true if balance has enough quantity, false otherwise
     */
    public boolean canSatisfyDemand(int demandQuantity) {
        return this.quantity >= demandQuantity;
    }
    
    /**
     * @brief Checks if the balance is empty
     * @return true if quantity is zero or less
     */
    public boolean isEmpty() {
        return this.quantity <= 0;
    }
    
    /**
     * @brief Calculates price for a specific quantity from this balance
     * @param qty Quantity to calculate price for
     * @return Total price for the requested quantity
     */
    public BigDecimal getPriceForQuantity(int qty) {
        if (qty > this.quantity) {
            throw new IllegalArgumentException("Requested quantity exceeds available quantity");
        }
        return this.unitPrice.multiply(BigDecimal.valueOf(qty));
    }
    
    /**
     * @brief Recalculates the total price based on current quantity and unit price
     */
    public void recalculateTotal() {
        this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }
    
    /**
     * @brief Checks if the balance matches a specific unit price
     * @param price Price to compare
     * @return true if prices match
     */
    public boolean matchesUnitPrice(BigDecimal price) {
        return this.unitPrice.compareTo(price) == 0;
    }
    
    // Validaciones privadas
    private static void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
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
        Balance balance = (Balance) o;
        return quantity == balance.quantity &&
               Objects.equals(unitPrice, balance.unitPrice) &&
               Objects.equals(totalPrice, balance.totalPrice);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(quantity, unitPrice, totalPrice);
    }
    
    @Override
    public String toString() {
        return String.format("Balance{quantity=%d, unitPrice=%s, totalPrice=%s}", 
            quantity, unitPrice, totalPrice);
    }

    
}
