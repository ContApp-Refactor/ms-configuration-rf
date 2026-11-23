package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity;

import java.math.BigDecimal;

import org.hibernate.annotations.TenantId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "accounting_movements")
@Getter
@Setter
public class AccountingMovementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id",nullable = false)
    private Long account; // ID de la cuenta en AccountCatalogueEntity

    @Column(nullable = false)
    private Long thirdPartyId;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal debit;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal credit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accounting_entry_id", nullable = false)
    private AccountingEntryEntity accountingEntry;

    @TenantId
    String tenantId;
}
