package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.InventoryConfigurationTypeEnum;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.enums.StateEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;

import java.util.List;
import java.util.UUID;

/**
 * @brief Entidad JPA para empresas
 *
 * Mapea la tabla 'enterprise' y define las relaciones con responsabilidades
 * tributarias, tipo de contribuyente, tipo de empresa, tipo de persona y
 * ubicación.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "enterprise")
public class EnterpriseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String idUser;
    private String name;
    private String nit;
    private String DV;
    private String phone;
    private String branch;
    private String email;
    private String logo;

    private StateEnum state;

    private InventoryConfigurationTypeEnum inventoryConfigurationType;

    private Long mainActivity;
    private Long secondaryActivity;

    @ManyToMany
    private List<TaxLiabilityEntity> taxLiabilities;

    @ManyToOne
    private TaxPayerTypeEntity taxPayerType;

    @ManyToOne
    @JoinColumn(name = "enterprise_type_id", referencedColumnName = "id")
    private EnterpriseTypeEntity enterpriseType;

    @ManyToOne
    @JoinColumn(name = "person_type_id", referencedColumnName = "id")
    private PersonTypeEntity personType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private LocationEntity location;

    @TenantId
    private String tenantId;
}
