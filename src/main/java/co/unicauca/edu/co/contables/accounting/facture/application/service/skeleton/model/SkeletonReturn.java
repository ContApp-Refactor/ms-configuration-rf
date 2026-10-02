package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "skeleton_returns")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkeletonReturn {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long originalFactCode;
    
    @Column(nullable = false)
    private Long productId;
    
    @Column(nullable = false)
    private Integer returnedQuantity;
    
    @Column(nullable = false)
    private LocalDateTime returnDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnType returnType;
    
    private String details;
    
    @PrePersist
    protected void onCreate() {
        returnDate = LocalDateTime.now();
    }
    
    public enum ReturnType {
        RETURN_ON_SALE,
        RETURN_ON_PURCHASE
    }
}
