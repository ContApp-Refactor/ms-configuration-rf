package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity;


import jakarta.persistence.*;
import lombok.*;

/**
 * @brief Entidad JPA para ciudades con clave compuesta triple
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "CITIES")
@IdClass(CityEntityId.class)
public class CityEntity {

    @Id
    @Column(name = "ci_code", length = 10)
    private String cityCode;

    @Id
    @Column(name = "st_code", length = 10)
    private String stateCode;

    @Id
    @Column(name = "co_code", length = 3)
    private String countryCode;

    @Column(name = "ci_name", length = 100, nullable = false)
    private String cityName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "st_code", referencedColumnName = "st_code", insertable = false, updatable = false),
        @JoinColumn(name = "co_code", referencedColumnName = "co_code", insertable = false, updatable = false)
    })
    private StateEntity state;
}
