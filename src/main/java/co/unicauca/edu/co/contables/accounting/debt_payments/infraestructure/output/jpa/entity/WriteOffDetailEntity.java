package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "write_off_details")
@Getter
@Setter
public class WriteOffDetailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "amount_written_off", nullable = false)
    private Long amountWrittenOff;

    @Column(name = "accounting_account", nullable = false)
    private Long accountingAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "write_off_id", nullable = false)
    private PortfolioWriteOffEntity portfolioWriteOff;

    @TenantId
    String tenantId;
}
