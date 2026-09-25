package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad JPA que representa un país en la base de datos.
 * Mapea la tabla "country".
 */
@Entity(name = "EnterpriseCountryEntity")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="country")
public class CountryEntity {

    /**
     * Identificador único del país.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del país.
     */
    private String name;
}
