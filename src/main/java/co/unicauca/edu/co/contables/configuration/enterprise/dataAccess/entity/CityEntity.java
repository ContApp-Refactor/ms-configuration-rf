package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * @brief Entidad JPA para ciudades
 *
 * Mapea la tabla 'city' y su relación con el departamento.
 */
@Entity(name = "EnterpriseCityEntity")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "city")
public class CityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private DepartmentEntity department;

    private String name;
}
