package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad JPA que representa un tipo de empresa en la base de datos.
 * Mapea la tabla "enterprise_type".
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="enterprise_type")
public class EnterpriseTypeEntity {

    /**
     * Identificador único del tipo de empresa.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del tipo de empresa.
     */
    private String name;
}
