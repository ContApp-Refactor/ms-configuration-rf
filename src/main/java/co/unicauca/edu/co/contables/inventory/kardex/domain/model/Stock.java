package co.unicauca.edu.co.contables.inventory.kardex.domain.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Domain model representing current stock information
 * 
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @Builder
public class Stock {
    private Long id;

    private Long productId;

    private String enterpriseId;

    private int quantity;

    private BigDecimal price;

    private boolean status;   
}
