package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.hibernate.annotations.TenantId;

import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief JPA entity for persisting Kardex inventory movement records
 * 
 */
@Entity(name = "WeightedAverageKardexEntity")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name = "kardex")
public class KardexEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String factCode;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType type;

    @Column(nullable = false)
    private int balanceQuantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balanceUnitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalBalance;

    @Column(nullable = false)
    private ZonedDateTime date;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @TenantId
    String tenantId;
}
