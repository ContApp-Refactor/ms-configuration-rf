package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.security;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
