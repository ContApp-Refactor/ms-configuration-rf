package co.unicauca.edu.co.contables.accounting.treasury.domain.model;

public record PaymentMethodData(Long id, boolean requiresBankAccount, Long accountingAccountId) {
}
