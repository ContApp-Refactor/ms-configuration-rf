package co.unicauca.edu.co.contables.configuration.typesOfDocuments.dataAccess.entity;

import co.unicauca.edu.co.contables.configuration.classesOfDocuments.dataAccess.entity.DocumentClassEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;

@Entity
@Table(
    name = "types_of_documents",
    indexes = {
        @Index(name = "idx_doc_type_id_enterprise", columnList = "id_enterprise"),
        @Index(name = "idx_doc_type_prefix", columnList = "prefix"),
        @Index(name = "idx_doc_type_name", columnList = "name"),
        @Index(name = "idx_doc_type_module", columnList = "module"),
        @Index(name = "idx_doc_type_module_enterprise", columnList = "module,id_enterprise")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prefix", nullable = false)
    private String prefix;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_class_id", referencedColumnName = "id", nullable = false)
    private DocumentClassEntity documentClass;

    @Column(name = "module", nullable = false)
    private String module;

    @Column(name = "id_enterprise", nullable = false)
    private String idEnterprise;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private Boolean status = true;

    @TenantId
    @Column(name = "tenant_id")
    private String tenantId;
}


