package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.PersonClassification;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;

/**
 * @brief Entidad JPA para tipos de identificación con clasificación de persona
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "TYPE_ID")
public class TypeIdEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ti_id")
    private Long id;

    @Column(name = "ti_code")
    private String tiId;

    @Column(name = "ti_name")
    private String tiName;

    @Column(name = "ti_entid")
    private String tientId;

    @TenantId
    @Column(name = "tenant_id")
    private String tenantId;

    @Builder.Default
    @Column(name = "ti_status")
    private Boolean status = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "ti_classification")
    private PersonClassification classification;
}
