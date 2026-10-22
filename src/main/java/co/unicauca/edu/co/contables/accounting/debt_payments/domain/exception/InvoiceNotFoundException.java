package co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception;
/**
 * @brief Exception thrown when an invoice is not found.
 * This exception extends RuntimeException and is used to indicate that a requested invoice does not exist in the system.
 */
public class InvoiceNotFoundException extends RuntimeException {
    public InvoiceNotFoundException(String message) {
        super(message);
    }
}
