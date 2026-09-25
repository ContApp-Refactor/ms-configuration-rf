package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity;

import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ePersonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;

/**
 * @brief Entidad JPA principal para terceros con multi-tenancy
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "THIRDS")
public class ThirdEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false, name = "th_id")
    private Long thId;

    @Column(name = "ent_id")
    private String entId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id_fk", referencedColumnName = "ti_id")
    private TypeIdEntity typeId;

    @Column(name = "th_person_type")
    private ePersonType personType;

    @Column(name = "th_names")
    private String names;

    @Column(name = "th_last_names")
    private String lastNames;

    @Column(name = "th_social_reason")
    private String socialReason;

    @Column(name = "th_gender")
    private String gender;

    @Column(name = "th_id_number")
    private Long idNumber;

    @Column(name = "th_verification_number")
    private Long verificationNumber;

    @Column(name = "th_state")
    @Builder.Default
    private Boolean state = true;

    @Column(name = "th_country")
    private String country;

    @Column(name = "th_province")
    private String province;

    @Column(name = "th_city")
    private String city;

    @Column(name = "th_address")
    private String address;

    @Column(name = "th_phone_number")
    private String phoneNumber;

    @Column(name = "th_email")
    private String email;

    @Column(name = "th_usage_count")
    @Builder.Default
    private Integer usageCount = 0;

    @TenantId
    @Column(name = "tenant_id")
    private String tenantId;
}
