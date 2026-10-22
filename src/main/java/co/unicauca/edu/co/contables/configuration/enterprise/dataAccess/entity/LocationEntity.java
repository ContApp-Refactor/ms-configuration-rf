package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * @brief Entidad JPA para la ubicación geográfica de una empresa
 *
 * Mapea la tabla 'location'.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "location")
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;

    @ManyToOne
    private CityEntity city;

    @ManyToOne
    private CountryEntity country;

    @ManyToOne
    private DepartmentEntity department;
}
