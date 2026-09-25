package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad JPA que representa una responsabilidad tributaria en la base de datos.
 * Mapea la tabla "tax_liability".
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="tax_liability")
public class TaxLiabilityEntity {

    /**
     * Identificador único de la responsabilidad tributaria.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre o descripción de la responsabilidad tributaria.
     */
    private String name;
}
