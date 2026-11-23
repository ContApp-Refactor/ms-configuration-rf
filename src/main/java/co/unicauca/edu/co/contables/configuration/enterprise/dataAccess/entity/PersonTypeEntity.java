package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * @brief Entidad JPA para el tipo de persona (natural o jurídica)
 *
 * Mapea la tabla 'person_type'.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "person_type")
public class PersonTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String name;
    private String surname;
    private String bussinessName;
}
