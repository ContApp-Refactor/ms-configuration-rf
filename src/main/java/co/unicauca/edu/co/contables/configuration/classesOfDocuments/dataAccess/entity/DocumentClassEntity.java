package co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

/**
 * @brief Entidad JPA para clases de documento
 *
 * Representa la tabla 'classes_of_documents' en la base de datos, almacenando
 * información de clases de documento asociadas a una empresa específica.
 * Incluye índices para optimizar consultas por empresa y nombre.
 */
@Entity
@Table(
    name = "classes_of_documents",
    indexes = {
        @Index(name = "idx_doc_class_id_enterprise", columnList = "id_enterprise"),
        @Index(name = "idx_doc_class_name", columnList = "name")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "id_enterprise", nullable = false)
    private String idEnterprise;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private Boolean status = true;

    @TenantId
    @Column(name = "tenant_id")
    private String tenantId;
}


