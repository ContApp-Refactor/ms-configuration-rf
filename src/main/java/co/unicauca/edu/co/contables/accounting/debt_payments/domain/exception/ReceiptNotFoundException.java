package co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception;

/**
 * @brief Exception thrown when a receipt is not found.
 * This exception extends RuntimeException and is used to indicate that a requested receipt does not exist in the system.
 */
public class ReceiptNotFoundException extends RuntimeException {
    public ReceiptNotFoundException(String message) {
        super(message);
    }
}
