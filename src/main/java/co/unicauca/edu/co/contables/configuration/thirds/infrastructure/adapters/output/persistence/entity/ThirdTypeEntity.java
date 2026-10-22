package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;

/**
 * @brief Entidad JPA para tipos de tercero con multi-tenancy
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "THIRD_TYPE")
public class ThirdTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tt_id")
    private Long ttId;

    @Column(name = "tt_name")
    private String ttName;

    @Column(name = "tt_entid")
    private String ttentId;

    @TenantId
    @Column(name = "tenant_id")
    private String tenantId;

    @Builder.Default
    @Column(name = "tt_status")
    private Boolean status = true;
}
