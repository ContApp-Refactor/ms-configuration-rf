package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad JPA que representa un tipo de contribuyente en la base de datos.
 * Mapea la tabla "tax_payer_type".
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="tax_payer_type")
public class TaxPayerTypeEntity {

    /**
     * Identificador único del tipo de contribuyente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre o descripción del tipo de contribuyente.
     */
    private String name;
}
