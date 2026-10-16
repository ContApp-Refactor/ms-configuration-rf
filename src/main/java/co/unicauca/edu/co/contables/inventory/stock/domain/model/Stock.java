package co.unicauca.edu.co.contables.inventory.stock.domain.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Domain model representing stock information
 * 
 * Important: Products is equal to Stock in this context.
 * Encapsulates stock attributes and behaviors such as buying and selling.
 */
@Getter
@Setter
@NoArgsConstructor
public class Stock {
    private Long id;

    private Long productId;

    private String name;

    private String enterpriseId;

    private int quantity;

    private BigDecimal price;

    private boolean state;

    public void activate() {
        this.state = true;
    }
    
    public void inactivate() {
        this.state = false;
    }

    public boolean isActive() {
        return this.state;
    }

    public void buy(int amount, BigDecimal price) {
        if (this.isActive() == false) {
            throw new IllegalStateException("Stock is not active");
        }
        if (amount < 0 || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount and price must be positive");
        }
        this.quantity += amount;
        this.price = price;
    }

    public void sell(int amount, BigDecimal price) {
        if (this.isActive() == false) {
            throw new IllegalStateException("Stock is not active");
        }
        if (amount < 0 || amount > this.quantity) {
            throw new IllegalArgumentException("Invalid amount for exit");
        }
        this.quantity -= amount;
        this.price = price;
    }
}
