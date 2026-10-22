package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity;


import jakarta.persistence.*;
import lombok.*;

/**
 * @brief Entidad JPA para estados/departamentos con clave compuesta
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "STATES")
@IdClass(StateEntityId.class)
public class StateEntity {

    @Id
    @Column(name = "st_code", length = 10)
    private String stateCode;

    @Id
    @Column(name = "co_code", length = 3)
    private String countryCode;

    @Column(name = "st_name", length = 100, nullable = false)
    private String stateName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "co_code", referencedColumnName = "co_code", insertable = false, updatable = false)
    private CountryEntity country;
}
