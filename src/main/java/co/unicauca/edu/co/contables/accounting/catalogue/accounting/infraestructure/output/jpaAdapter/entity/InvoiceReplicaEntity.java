package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.InvoiceStatus;

import org.hibernate.annotations.TenantId;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "AccountingInvoiceReplicaEntity")
@Getter
@Setter
@Table(name = "accounting_invoice_replica")
public class InvoiceReplicaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fact_code", nullable = false)
    private Long factCode;

    @Column(name = "ent_id", nullable = false)
    private String entId;

    @Column(name = "third_id", nullable = false)
    private Long thirdId;

    @Column(name = "total_value", nullable = false)
    private BigDecimal totalValue;

    @Column(name = "total_pay", nullable = false)
    private BigDecimal totalPay;

    @Column(name = "pending_value", nullable = false)
    private BigDecimal pendingValue;

     @Column(name = "creation_date", nullable = false)
    private LocalDate creationDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @UpdateTimestamp
    private LocalDate lastUpdateAt;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "accounting_account", nullable = false)
    private Long accountingAccount;

    @Enumerated(EnumType.STRING) 
    @Column(name = "status", nullable = false)
    private InvoiceStatus status;

    @TenantId
    String tenantId;
}
