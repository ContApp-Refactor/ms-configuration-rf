package co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception;

/**
 * @brief Exception thrown when a portfolio write-off is not found.
 * This exception extends RuntimeException and is used to indicate that a requested portfolio write-off does not exist in the system.
 */
public class PortfolioWriteOffNotFoundException extends RuntimeException {
    public PortfolioWriteOffNotFoundException(String message) {
        super(message);
    }
}
