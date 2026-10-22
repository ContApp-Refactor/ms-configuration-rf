package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "receipt_details")
@Getter
@Setter
public class ReceiptDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "invoice_code")
    private String invoiceCode;

    @Column(name = "accounting_account", nullable = false)
    private Long accountingAccount;

    @Column(name = "amount_paid", nullable = false)
    private Long amountPaid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", nullable = false)
    private ReceiptEntity receipt;

    @TenantId
    String tenantId;

}
