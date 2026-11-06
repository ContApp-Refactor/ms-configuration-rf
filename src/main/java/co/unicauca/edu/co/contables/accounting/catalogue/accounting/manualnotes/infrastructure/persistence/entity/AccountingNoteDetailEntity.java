package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "manualnotes_accounting_note_details",
        indexes = {
                @Index(name = "idx_manualnotes_detail_account_note", columnList = "account_code, accounting_note_id"),
                @Index(name = "idx_manualnotes_detail_third_party", columnList = "third_party_id")
        })
public class AccountingNoteDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accounting_note_id", nullable = false)
    private AccountingNoteEntity accountingNote;

    @Column(name = "account_code", nullable = false, length = 20)
    private String accountCode;

    @Column(name = "third_party_id", length = 20)
    private String thirdPartyId;

    @Column(name = "cost_center_code", length = 20)
    private String costCenterCode;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal debit;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal credit;

    public AccountingNoteDetailEntity() {
    }

    public AccountingNoteDetailEntity(String accountCode, String thirdPartyId, String costCenterCode,
                                      String description, BigDecimal debit, BigDecimal credit) {
        this.accountCode = accountCode;
        this.thirdPartyId = thirdPartyId;
        this.costCenterCode = costCenterCode;
        this.description = description;
        this.debit = debit != null ? debit : BigDecimal.ZERO;
        this.credit = credit != null ? credit : BigDecimal.ZERO;
    }

    public void setAccountingNote(AccountingNoteEntity accountingNote) { this.accountingNote = accountingNote; }
    public String getAccountCode() { return accountCode; }
    public String getThirdPartyId() { return thirdPartyId; }
    public String getCostCenterCode() { return costCenterCode; }
    public String getDescription() { return description; }
    public BigDecimal getDebit() { return debit; }
    public BigDecimal getCredit() { return credit; }
}
