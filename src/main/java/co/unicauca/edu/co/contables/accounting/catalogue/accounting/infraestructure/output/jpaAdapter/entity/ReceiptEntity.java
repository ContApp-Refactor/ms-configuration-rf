package co.unicauca.edu.co.contables.accounting.catalogue.accounting.infraestructure.output.jpaAdapter.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.hibernate.annotations.TenantId;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.ProcessingStatus;

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

@Entity(name = "AccountingReceiptEntity")
@Table(name = "accounting_receipts")
@Getter
@Setter
public class ReceiptEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long originalReceiptId;

    @Column(nullable = false, length = 50)
    private String receiptCode;

    @Column(nullable = false, length = 36) // UUID suele tener 36 caracteres
    private String enterpriseId;

    @Column(nullable = false)
    private Long receiptTypeId;

    @Column(nullable = false)
    private Long thirdPartyId;

    @Column(nullable = false)
    private Long paymentMethodId;

    @Column(nullable = false)
    private Long paymentMethodAccount; // <-- CAMPO AÑADIDO

    @Column(name = "ledger_account_id") // El nombre de la columna puede ser diferente
    private Long ledgerAccountId; // <-- CAMPO AÑADIDO

    @Column(nullable = false, length = 50)
    private String status;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false, precision = 18, scale = 2) // <-- AJUSTADO PARA BIGDECIMAL
    private BigDecimal totalAmount;

    @Column(length = 255)
    private String observations;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ProcessingStatus processingStatus; // <-- CAMPO AÑADIDO

    @Column(name = "center_cost_id") 
    private Long centerCostId;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ReceiptDetailEntity> details;

    @TenantId
    String tenantId;
}
