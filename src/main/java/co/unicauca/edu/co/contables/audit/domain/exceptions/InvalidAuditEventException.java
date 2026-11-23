package co.unicauca.edu.co.contables.audit.domain.exceptions;

public class InvalidAuditEventException extends AuditDomainException {

    public InvalidAuditEventException(String message) {
        super(message);
    }

    public InvalidAuditEventException(String message, Throwable cause) {
        super(message, cause);
    }

}
