package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad JPA que representa un tipo de persona en la base de datos.
 * Mapea la tabla "person_type".
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="person_type")
public class PersonTypeEntity {

    /**
     * Identificador único del tipo de persona.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tipo de persona (natural o jurídica).
     */
    private String type;

    /**
     * Nombre(s) de la persona natural.
     */
    private String name;

    /**
     * Apellido(s) de la persona natural.
     */
    private String surname;

    /**
     * Razón social para personas jurídicas.
     */
    private String bussinessName;
}
