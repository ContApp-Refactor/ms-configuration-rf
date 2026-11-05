package co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity;

import org.hibernate.annotations.TenantId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief JPA entity for persisting product information
 * 
 */
@Entity(name = "WeightedAverageProductEntity")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name = "kardex_products")
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long productId;

    @Column(nullable = false, unique = true)
    private String reference;

    @Column(nullable = false)
    private String name;

    private String presentation;

    @Column(name = "enterprise_id", nullable = false)
    private String enterpriseId;

    @Column(nullable = false)
    private boolean state;

    @TenantId
    String tenantId;
}
