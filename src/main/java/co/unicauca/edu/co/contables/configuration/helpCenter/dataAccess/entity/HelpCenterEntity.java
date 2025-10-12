package co.unicauca.edu.co.contables.configuration.helpCenter.dataAccess.entity;

import co.unicauca.edu.co.contables.configuration.helpCenter.domain.models.DocumentModule;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;

@Entity
@Table(
    name = "help_center",
    indexes = {
        @Index(name = "idx_help_center_id_enterprise", columnList = "id_enterprise"),
        @Index(name = "idx_help_center_module", columnList = "module"),
        @Index(name = "idx_help_center_name", columnList = "name"),
        @Index(name = "idx_help_center_module_enterprise", columnList = "module,id_enterprise")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HelpCenterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "module", nullable = false, length = 50)
    private DocumentModule module;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "id_enterprise", nullable = false)
    private String idEnterprise;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private Boolean status = true;

    @TenantId
    @Column(name = "tenant_id")
    private String tenantId;
}
