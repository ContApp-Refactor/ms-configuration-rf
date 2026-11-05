package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * @brief Entidad JPA para el tipo de empresa
 *
 * Mapea la tabla 'enterprise_type'.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "enterprise_type")
public class EnterpriseTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}
