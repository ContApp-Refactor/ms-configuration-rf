package co.unicauca.edu.co.contables.configuration.helpCenter.dataAccess.entity;

import co.unicauca.edu.co.contables.configuration.helpCenter.domain.models.DocumentModule;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "help_center",
    indexes = {
        @Index(name = "idx_help_center_module", columnList = "module"),
        @Index(name = "idx_help_center_module_id", columnList = "module_id"),
        @Index(name = "idx_help_center_name", columnList = "name")
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

    @Column(name = "module_id", nullable = false)
    private Integer moduleId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private Boolean status = true;
}
