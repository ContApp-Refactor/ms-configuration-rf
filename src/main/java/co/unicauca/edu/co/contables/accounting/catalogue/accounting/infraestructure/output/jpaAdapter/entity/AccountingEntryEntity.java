package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.annotations.TenantId;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.AccountingEntryStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "accounting_entries")
@Getter
@Setter
public class AccountingEntryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_enterprise", nullable = false)
    private String idEnterprise;

    @Column(nullable = false, unique = true, length = 50)
    private String code;
    
    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 255)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private AccountingEntryStatus status;
    
    // Usamos una relación directa con el recibo
    @Column(name = "source_document_id", nullable = false)
    private Long sourceDocumentId;

    @Column(name = "type", nullable = false, length = 50)
    private String type; 

    @Column(name = "center_cost_id")
    private Long centerCostId;
    
    @OneToMany(mappedBy = "accountingEntry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<AccountingMovementEntity> movements;

    @TenantId
    String tenantId;
}
