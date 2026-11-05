package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.security;

public class AuthenticationRequiredException extends RuntimeException {

    public AuthenticationRequiredException(String message) {
        super(message);
    }
}
