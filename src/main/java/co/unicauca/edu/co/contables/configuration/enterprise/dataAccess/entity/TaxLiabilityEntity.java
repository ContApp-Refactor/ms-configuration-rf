package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * @brief Entidad JPA para la responsabilidad tributaria
 *
 * Mapea la tabla 'tax_liability'.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "tax_liability")
public class TaxLiabilityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}
