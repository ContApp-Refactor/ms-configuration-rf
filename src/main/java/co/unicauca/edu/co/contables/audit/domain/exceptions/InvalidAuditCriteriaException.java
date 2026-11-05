package co.unicauca.edu.co.contables.audit.domain.exceptions;

public class InvalidAuditCriteriaException extends AuditDomainException {

    public InvalidAuditCriteriaException(String message) {
        super(message);
    }

    public InvalidAuditCriteriaException(String message, Throwable cause) {
        super(message, cause);
    }

}
