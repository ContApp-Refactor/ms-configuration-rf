package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.replicas;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.TenantId;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "invoice_replica")
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
    private Long totalValue;

    @Column(name = "total_pay", nullable = false)
    private Long totalPay;

    @Column(name = "pending_value", nullable = false)
    private Long pendingValue;

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
