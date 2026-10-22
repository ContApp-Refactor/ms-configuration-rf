package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.TenantId;

import java.util.UUID;

/**
 * @brief Entidad JPA para materias (subjects)
 *
 * Mapea la tabla 'subjects'.
 */
@Entity
@Table(name = "subjects")
@Data
public class SubjectEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @TenantId
    private String tenantId;
}
