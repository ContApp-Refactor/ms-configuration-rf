package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Entidad JPA que representa un departamento en la base de datos.
 * Mapea la tabla "department" y define las relaciones con otras entidades.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="department")
public class DepartmentEntity {

    /**
     * Identificador único del departamento.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * País al que pertenece el departamento.
     */
    @ManyToOne
    @JoinColumn(name="country_id")
    private CountryEntity country;

    /**
     * Nombre del departamento.
     */
    private String name;

    /**
     * Lista de ciudades que pertenecen al departamento.
     */
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CityEntity> cities;
}
