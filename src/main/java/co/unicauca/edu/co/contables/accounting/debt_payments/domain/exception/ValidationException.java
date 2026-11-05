package co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception;

public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
