package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import co.unicauca.edu.co.contables.inventory.peps.domain.enums.MovementType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

import org.hibernate.annotations.TenantId;

/**
 * @brief JPA Entity representing a Kardex record
 * 
 * Maps to the 'kardex' table. Stores information about inventory movements
 * (purchases, sales, returns) including quantity, price, and balance.
 */
@Entity
@Table(name = "peps_kardex")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class KardexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idKardex;

    @Column(name="fact_code")
    private String factCode;

    @Column(name = "date", nullable=false)
    private  ZonedDateTime date;

    @Column(name="detail", nullable = false)
    private String details;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name="unit_price")
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private MovementType type;

    @Column(name="available_quantity")
    private int availableQuantity;

    @ManyToOne
    @JoinColumn(name="product_id",referencedColumnName = "product_id", nullable = false)
    private ProductEntity product;

   @OneToMany(mappedBy = "movementSale")
    private List<DetailOutputEntity> detailsOutput;

    @OneToMany(mappedBy = "movementOrigin")
    private List<DetailOutputEntity> detailsOrigin;
    /* 

    @TenantId
    String tenantId;
    */
}
