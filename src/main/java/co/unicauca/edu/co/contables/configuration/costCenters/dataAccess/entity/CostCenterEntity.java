package co.unicauca.edu.co.contables.configuration.costCenters.dataAccess.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;

import java.util.List;

@Entity
@Table(
    name = "cost_centers",
    indexes = {
        @Index(name = "idx_cost_center_id_enterprise", columnList = "id_enterprise"),
        @Index(name = "idx_cost_center_code", columnList = "code"),
        @Index(name = "idx_cost_center_name", columnList = "name")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostCenterEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "code", nullable = false)
        private String code;

        @Column(name = "name", nullable = false)
        private String name;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "parent_id", referencedColumnName = "id")
        private CostCenterEntity parent;

        @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<CostCenterEntity> children;

        // Id de empresa obligatorio
        @Column(name = "id_enterprise", nullable = false)
        private String idEnterprise;

        @Builder.Default
        @Column(name = "status", nullable = false)
        private Boolean status = true;

        @TenantId
        @Column(name = "tenant_id")
        private String tenantId;
}
