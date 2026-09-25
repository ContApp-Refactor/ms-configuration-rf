package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.TenantId;

import java.util.UUID;

/**
 * Entidad JPA que representa una materia (Subject).
 * Mapea la tabla "subjects" en la base de datos.
 */
@Entity
@Table(name = "subjects")
@Data
public class SubjectEntity {

    /**
     * Identificador único de la materia (UUID autogenerado).
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Código de la materia (enviado por el usuario).
     */
    @Column(nullable = false, unique = true)
    private String code;

    /**
     * Nombre de la materia.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Identificador del inquilino (tenant) para multi-tenancy.
     */
    @TenantId
    String tenantId;
}
