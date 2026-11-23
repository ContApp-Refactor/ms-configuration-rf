package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TenantId;

/**
 * @brief Entidad intermedia para relación muchos a muchos terceros-tipos con multi-tenancy
 */
@Entity
@Table(name = "thirds_and_types")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ThirdsAndTypesId.class)
public class ThirdsAndTypesEntity {

    @Id
    @Column(name = "th_id")
    private Long thId;

    @Id
    @Column(name = "tt_id")
    private Long ttId;

    @TenantId
    @Column(name = "tenant_id")
    private String tenantId;

    @ManyToOne
    @JoinColumn(name = "th_id", insertable = false, updatable = false)
    private ThirdEntity third;

    @ManyToOne
    @JoinColumn(name = "tt_id", insertable = false, updatable = false)
    private ThirdTypeEntity thirdType;
}
