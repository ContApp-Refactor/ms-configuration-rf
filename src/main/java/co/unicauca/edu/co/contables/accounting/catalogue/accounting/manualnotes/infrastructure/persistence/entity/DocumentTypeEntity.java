package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity(name = "RegcontableDocumentType")
@Table(name = "regcontable_document_types",
        uniqueConstraints = @UniqueConstraint(name = "uk_regcontable_document_type_enterprise_code",
                columnNames = {"enterprise_id", "code"}))
public class DocumentTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(name = "enterprise_id", nullable = false, length = 36)
    private String enterpriseId;

    @Column(nullable = false, length = 30)
    private String prefix;

    @Column(name = "current_sequence", nullable = false)
    private Long currentSequence;

    public Long getCurrentSequence() { return currentSequence; }
    public void setCurrentSequence(Long currentSequence) { this.currentSequence = currentSequence; }
    public void setCode(String code) { this.code = code; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public String getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(String enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getPrefix() { return prefix; }
}
