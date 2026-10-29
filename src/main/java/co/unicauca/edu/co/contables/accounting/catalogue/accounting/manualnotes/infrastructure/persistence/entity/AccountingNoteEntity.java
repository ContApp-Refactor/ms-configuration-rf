package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "manualnotes_accounting_notes",
        uniqueConstraints = @UniqueConstraint(name = "uk_manualnotes_origin",
                columnNames = {"origin_module", "origin_id"}),
        indexes = @Index(name = "idx_manualnotes_note_date_type", columnList = "date, document_type_code"))
public class AccountingNoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String number;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 255)
    private String concept;

    @Column(name = "document_type_code", nullable = false, length = 20)
    private String documentTypeCode;

    @Column(name = "origin_module", nullable = false, length = 80)
    private String originModule;

    @Column(name = "origin_id", nullable = false, length = 100)
    private String originId;

    // Persist the company used during validation so queries can enforce tenant/company scope.
    @Column(name = "enterprise_id", length = 100)
    private String enterpriseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoteStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "annulled_at")
    private LocalDateTime annulledAt;

    @Column(name = "annulment_reason", length = 500)
    private String annulmentReason;

    @OneToMany(mappedBy = "accountingNote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountingNoteDetailEntity> details = new ArrayList<>();

    public enum NoteStatus { DRAFT, POSTED, ANNULLED }

    public AccountingNoteEntity() {
    }

    public AccountingNoteEntity(String number, String documentTypeCode, LocalDate date, String concept,
                                String originModule, String originId, String enterpriseId) {
        this.number = number;
        this.documentTypeCode = documentTypeCode;
        this.date = date;
        this.concept = concept;
        this.originModule = originModule;
        this.originId = originId;
        this.enterpriseId = enterpriseId;
        this.status = NoteStatus.POSTED;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = NoteStatus.POSTED;
        }
    }

    public void addDetail(AccountingNoteDetailEntity detail) {
        details.add(detail);
        detail.setAccountingNote(this);
    }

    public void replaceDetails(List<AccountingNoteDetailEntity> replacement) {
        details.clear();
        replacement.forEach(this::addDetail);
    }

    public void updateDraft(LocalDate date, String documentTypeCode, String concept,
                            String enterpriseId, List<AccountingNoteDetailEntity> replacement) {
        if (status != NoteStatus.DRAFT) {
            throw new IllegalStateException("Solo se pueden editar notas en estado borrador.");
        }
        this.date = date;
        this.documentTypeCode = documentTypeCode;
        this.concept = concept;
        this.enterpriseId = enterpriseId;
        replaceDetails(replacement);
    }

    public void post() {
        if (status != NoteStatus.DRAFT) {
            throw new IllegalStateException("Solo se pueden contabilizar notas en estado borrador.");
        }
        this.status = NoteStatus.POSTED;
    }

    public void annul(String reason) {
        if (status == NoteStatus.ANNULLED) {
            throw new IllegalStateException("La nota ya está anulada.");
        }
        this.status = NoteStatus.ANNULLED;
        this.annulledAt = LocalDateTime.now();
        this.annulmentReason = reason;
    }

    public void setStatus(NoteStatus status) { this.status = status; }

    public Long getId() { return id; }
    public String getNumber() { return number; }
    public LocalDate getDate() { return date; }
    public String getConcept() { return concept; }
    public String getDocumentTypeCode() { return documentTypeCode; }
    public String getOriginModule() { return originModule; }
    public String getOriginId() { return originId; }
    public String getEnterpriseId() { return enterpriseId; }
    public NoteStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getAnnulledAt() { return annulledAt; }
    public String getAnnulmentReason() { return annulmentReason; }
    public List<AccountingNoteDetailEntity> getDetails() { return details; }
}
