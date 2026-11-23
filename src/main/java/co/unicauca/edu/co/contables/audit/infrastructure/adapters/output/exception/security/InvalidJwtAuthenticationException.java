package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.security;

public class InvalidJwtAuthenticationException extends RuntimeException {

    public InvalidJwtAuthenticationException(String message) {
        super(message);
    }
}
