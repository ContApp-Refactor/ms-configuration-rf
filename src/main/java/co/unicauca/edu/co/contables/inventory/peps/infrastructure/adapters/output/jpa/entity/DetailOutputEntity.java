package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.jpa.entity;

import java.math.BigDecimal;

import org.hibernate.annotations.TenantId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief JPA Entity for FIFO output details
 * 
 * Maps to the 'DetailOutputFIFO' table. Links sale movements to their
 * corresponding purchase movements (lots) to track cost layers.
 */
@Entity
@Table(name = "DetailOutputFIFO")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DetailOutputEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetailOutput;

    @Column(name = "quantity_used", nullable = false)
    private int quantityUsed;

    @Column(name="unit_price", nullable = false)
    private BigDecimal unitPrice;

   @ManyToOne
    @JoinColumn(name = "id_movement_sale")
    private KardexEntity movementSale;

    @ManyToOne
    @JoinColumn(name = "id_movement_origin")
    private KardexEntity movementOrigin;

    /* 
    @TenantId
    String tenantId;

    */
}
