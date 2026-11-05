package co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.dataAccess.entity;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.enums.AccountType;
import co.unicauca.edu.co.contables.accounting.catalogue.banks.dataAccess.entity.BankEntity;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.adapters.output.jpaAdapter.entity.AccountCatalogueEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;

/**
 * @brief Entidad que representa una cuenta bancaria en el sistema
 *
 * Almacena la información de cuentas bancarias con índices para
 * consultas por empresa, número de cuenta y banco.
 */
@Entity
@Table(
    name = "bank_accounts",
    indexes = {
        @Index(name = "idx_bank_account_id_enterprise", columnList = "id_enterprise"),
        @Index(name = "idx_bank_account_account_number", columnList = "account_number"),
        @Index(name = "idx_bank_account_bank_id", columnList = "bank_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false)
    private Long accountNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private BankEntity bank;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accounting_account_id", nullable = false)
    private AccountCatalogueEntity accountingAccount;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private Boolean status = true;

    @Column(name = "id_enterprise", nullable = false)
    private String idEnterprise;

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    @TenantId
    @Column(name = "tenant_id")
    private String tenantId;
}
