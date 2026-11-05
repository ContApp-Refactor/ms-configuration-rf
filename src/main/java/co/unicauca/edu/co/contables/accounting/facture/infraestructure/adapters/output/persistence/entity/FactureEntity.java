package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.persistence.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import co.unicauca.edu.co.contables.accounting.facture.domain.model.eFactureType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "FACTURES")
public class FactureEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false, name = "fact_id")
    private Long factId;

    @Column(name = "ent_id")
    private String entId;

    @Column(name="th_id")
    private Long thId;

    @Column(name="fact_code", unique = true)
    private Long factCode;

  
    
    

    @Column(name="fact_observations")
    private String factObservations;

    @Enumerated(EnumType.STRING)
    @Column(name="fact_type")
    private eFactureType factureType;
    
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
        name = "FACTURES_AND_PRODUCTS",
        joinColumns = @JoinColumn(name = "fact_id"),
        inverseJoinColumns = @JoinColumn(name = "prod_id")
    )
    @Default
    private Set<ProductEntity> factProducts = new HashSet<>();
    
    @Column(name="fact_subtotals")
    private Double factSubtotals;
    
    @Column(name="fact_sales_tax")
    private Double facSalesTax;
    
    @Column(name="fact_withholding_source")
    private Double facWithholdingSource;
    
    @Column(name="descounts")
    private Double descounts;

    @Column(name="fact_created_at")
    @CreationTimestamp
    private LocalDateTime creationDate;
    
    @Column(name="fact_updated_at")
    @UpdateTimestamp
    private LocalDateTime updateDate;
}
