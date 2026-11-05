package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.persistence.entity;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "PRODUCTS")
public class ProductEntity {
    @Id
    @Column(name = "prod_id", nullable = false, unique = true)
    private long productId;

    @Column(name = "prod_code", nullable = false)
    private String code;

    @Column(name = "prod_descount", nullable = false)
    private double descount;

    @Column(name = "prod_amount", nullable = false)
    private double amount;

    @Column(name = "prod_description", nullable = false)
    private String description;
    
    @Column(name = "prod_vat", nullable = false)
    private double vat;
    
    @Column(name = "prod_unit_price", nullable = false)
    private double unitPrice;
    
    @Column(name = "prod_subtotal", nullable = false)
    private double subtotal;

    @ManyToMany(mappedBy = "factProducts", fetch = FetchType.LAZY)
    private Set<FactureEntity> factures;
}
