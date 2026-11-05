package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.security;

public class MissingHeaderException extends RuntimeException {
    public MissingHeaderException(String message) {
        super(message);
    }

}
