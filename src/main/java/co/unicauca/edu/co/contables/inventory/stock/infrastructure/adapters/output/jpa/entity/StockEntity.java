package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.jpa.entity;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.TenantId;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief JPA entity for Stock.
 * 
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class StockEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id",nullable = false, unique = true)
    private Long productId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String enterpriseId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean state;

    @UpdateTimestamp
    private Instant lastModifiedDate;

    @TenantId
    String tenantId;
}
