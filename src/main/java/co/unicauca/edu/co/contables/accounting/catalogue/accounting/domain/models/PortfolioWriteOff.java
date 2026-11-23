package co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.AccountingEntryStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.ProcessingStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.enums.SourceDocumentType;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.WriteOffStatus;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PortfolioWriteOff {
    private Long id;
    private Long originalWriteOffId;
    private String code;
    private String justification;
    private Long totalAmount;
    private LocalDate writeOffDate;
    private Long debitAuxiliaryAccount;
    private Long thirdId;
    private WriteOffStatus status;
    private String enterpriseId;
    private Long centerCostId;
    private List<WriteOffDetail> details;
    private ProcessingStatus processingStatus;

public AccountingEntry generateAccountingEntry(Function<Long, AccountCatalogue> accountFinder, String entryCode) {
    List<AccountingMovement> movements = new ArrayList<>();

    // 1. Débito a la cuenta de gasto/provisión
    AccountCatalogue debitAccount = accountFinder.apply(this.debitAuxiliaryAccount);
    if (debitAccount == null) {
        throw new IllegalStateException("No se encontró la cuenta contable de débito con código: " + this.debitAuxiliaryAccount);
    }
    movements.add(AccountingMovement.builder()
            .account(debitAccount.getId())
            .thirdPartyId(this.thirdId)
            .description("Castigo de cartera " + this.code)
            .debit(BigDecimal.valueOf(this.totalAmount))
            .credit(BigDecimal.ZERO)
            .build());

    // 2. Créditos a las cuentas por cobrar de cada factura
    for (WriteOffDetail detail : this.details) {
        AccountCatalogue creditAccount = accountFinder.apply(detail.getCreditInvoiceAccount());
        if (creditAccount == null) {
            throw new IllegalStateException("No se encontró la cuenta contable de crédito para la factura "
                    + detail.getInvoiceCode() + " con código: " + detail.getCreditInvoiceAccount());
        }
        movements.add(AccountingMovement.builder()
                .account(creditAccount.getId())
                .thirdPartyId(this.thirdId)
                .description("Castigo factura " + detail.getInvoiceCode())
                .debit(BigDecimal.ZERO)
                .credit(detail.getAmountWrittenOff())
                .build());
    }

    validateDoubleEntry(movements);

    return AccountingEntry.builder()
            .code(entryCode)
            .date(this.writeOffDate)
            .description("Contabilización de Castigo de Cartera " + this.code)
            .status(AccountingEntryStatus.ACTIVE)
            .sourceDocumentId(this.originalWriteOffId)
            .type(SourceDocumentType.PORTFOLIO_WRITEOFF.name())
            .idEnterprise(this.enterpriseId)
            .centerCostId(this.centerCostId)
            .movements(movements)
            .build();
}

private void validateDoubleEntry(List<AccountingMovement> movements) {
    BigDecimal totalDebits = movements.stream().map(AccountingMovement::getDebit).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal totalCredits = movements.stream().map(AccountingMovement::getCredit).reduce(BigDecimal.ZERO, BigDecimal::add);

    if (totalDebits.compareTo(totalCredits) != 0) {
        throw new IllegalStateException("Error de Partida Doble: La suma de débitos no es igual a la suma de créditos.");
    }
}
}
