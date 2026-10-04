package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity;

import java.util.List;

import org.hibernate.annotations.TenantId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief JPA Entity representing a Product
 * 
 * Maps to the 'peps_product' table. Stores product reference data needed
 * for inventory management.
 * 
 * El nombre explícito es obligatorio: facture y products también declaran una
 * clase llamada ProductEntity y Hibernate rechaza dos entidades con el mismo
 * nombre en un mismo EntityManager.
 */
@Entity(name = "PepsProductEntity")
@Table(name = "peps_product")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="product_id", nullable = false, unique = true)
    private Long productId;
    
    @Column(name ="name", nullable = false, length =50 )
    private String name;

    @Column(name ="reference", nullable = false, length =50 )
    private String reference;

    @Column(name ="presentation", nullable = false, length =50 )
    private String presentation;

    @Column(name="enterprise_id", nullable = false)
    private String enterpriseId;

    @Column(nullable = false)
    private boolean state;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product")
    private List<KardexEntity> recordsKardex;

    
    /* 
    @TenantId
    String tenantId;
    */
}
