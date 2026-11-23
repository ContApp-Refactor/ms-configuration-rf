package co.unicauca.edu.co.contables.accounting.facture.application.service.skeleton.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "skeleton_factures", uniqueConstraints =
        @UniqueConstraint(name = "uk_skeleton_facture_tenant_enterprise_code",
                columnNames = {"tenant_id", "ent_id", "fact_code"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkeletonFacture {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "fact_code", nullable = false)
    private Long factCode;
    
    @Column(name = "ent_id", nullable = false)
    private String entId;
    
    @Column(nullable = false)
    private Long thId;
    
    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<SkeletonProduct> products = new HashSet<>();
    
    @Column(nullable = false)
    private String totalValue;
    
    @Column(nullable = false)
    private String totalPay;
    
    @Column(nullable = false)
    private String pendingValue;

    @Column(name = "issue_date")
    private LocalDate issueDate;
    
    private LocalDate expirationDate;
    
    private Long accountingAccount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkeletonFactureType factureType;

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_status", nullable = false, length = 20)
    @Builder.Default
    private PurchaseInvoiceStatus purchaseStatus = PurchaseInvoiceStatus.ACTIVE;

    @Version
    private long version;

    @TenantId
    @Column(name = "tenant_id", nullable = false, length = 80)
    private String tenantId;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "voided_at")
    private LocalDateTime voidedAt;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (issueDate == null) {
            issueDate = LocalDate.now();
        }
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
    
    // Métodos helper para mantener la relación bidireccional
    public void addProduct(SkeletonProduct product) {
        products.add(product);
        product.setFacture(this);
    }
    
    public void removeProduct(SkeletonProduct product) {
        products.remove(product);
        product.setFacture(null);
    }
}
