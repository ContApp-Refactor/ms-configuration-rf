package co.unicauca.edu.co.contables.audit.domain.exceptions;

public class ExportAuditException extends AuditDomainException {

    public ExportAuditException(String message) {
        super(message);
    }

    public ExportAuditException(String message, Throwable cause) {
        super(message, cause);
    }

}
