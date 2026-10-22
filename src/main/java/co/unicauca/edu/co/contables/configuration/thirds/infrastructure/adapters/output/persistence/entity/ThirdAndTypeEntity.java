package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity;

import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.identifiers.ThirdsAndTypeId;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;

/**
 * @brief Entidad para relación terceros-tipos usando @EmbeddedId
 */
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "thirds_and_types")
public class ThirdAndTypeEntity {

    @EmbeddedId
    private ThirdsAndTypeId id;

    @ManyToOne
    @MapsId("thId")
    @JoinColumn(name = "th_id", nullable = false)
    private ThirdEntity third;

    @ManyToOne
    @MapsId("ttId")
    @JoinColumn(name = "tt_id", nullable = false)
    private ThirdTypeEntity thirdType;

    @TenantId
    @Column(name = "tenant_id")
    private String tenantId;
}
