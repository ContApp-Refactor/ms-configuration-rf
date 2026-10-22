package co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.exception;

public class AccountingEntryNotFoundException extends RuntimeException {
    public AccountingEntryNotFoundException(String message) {
        super(message);
    }
}
