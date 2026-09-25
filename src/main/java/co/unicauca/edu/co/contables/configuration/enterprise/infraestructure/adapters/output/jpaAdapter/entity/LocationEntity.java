package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad JPA que representa una ubicación en la base de datos.
 * Mapea la tabla "location" y define las relaciones con otras entidades.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="location")
public class LocationEntity {

    /**
     * Identificador único de la ubicación.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Dirección específica de la ubicación.
     */
    private String address;

    /**
     * Ciudad a la que pertenece la ubicación.
     */
    @ManyToOne
    CityEntity city;

    /**
     * País donde se encuentra la ubicación.
     */
    @ManyToOne
    CountryEntity country;

    /**
     * Departamento al que pertenece la ubicación.
     */
    @ManyToOne
    DepartmentEntity department;
}
