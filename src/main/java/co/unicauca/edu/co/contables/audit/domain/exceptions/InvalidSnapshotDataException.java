package co.unicauca.edu.co.contables.audit.domain.exceptions;

public class InvalidSnapshotDataException extends AuditDomainException {

    public InvalidSnapshotDataException(String message) {
        super(message);
    }

    public InvalidSnapshotDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
