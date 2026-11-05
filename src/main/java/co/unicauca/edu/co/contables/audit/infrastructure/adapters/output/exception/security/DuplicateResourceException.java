package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.security;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
