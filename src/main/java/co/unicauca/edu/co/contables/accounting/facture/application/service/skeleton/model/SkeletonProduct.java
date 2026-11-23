package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.input.rest.data.request.IntegerListConverter;

@Entity
@Table(name = "skeleton_products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkeletonProduct {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id", nullable = false)
    private SkeletonFacture facture;
    
    @Column(nullable = false)
    private Long productId;
    
    @Column(nullable = false)
    private Integer amount;
    
    private String description;
    
    private Double discount;
    
    @Column(nullable = false)
    private Double unitPrice;
    
    @Column(nullable = false)
    private Double subtotal;
    
    @Convert(converter = IntegerListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<Integer> taxPercentage;
    
    public double getBasePrice() {
        // Aplica descuento
        double priceAfterDiscount = unitPrice * (1 - discount / 100.0);

        // Suma los porcentajes de impuestos
        int totalTaxPercent = taxPercentage != null ? 
            taxPercentage.stream().mapToInt(Integer::intValue).sum() : 0;

        // Calcula el valor de impuestos
        double taxAmount = priceAfterDiscount * (totalTaxPercent / 100.0);

        // Resta los impuestos al precio después del descuento
        return priceAfterDiscount - taxAmount;
    }
}
