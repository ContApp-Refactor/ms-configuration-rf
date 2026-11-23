package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * @brief Entidad JPA para departamentos
 *
 * Mapea la tabla 'department' y su relación con país y ciudades.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "department")
public class DepartmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private CountryEntity country;

    private String name;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CityEntity> cities;
}
