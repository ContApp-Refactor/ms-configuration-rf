package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.exception;

public class DuplicateAccountingNoteException extends RuntimeException {
    public DuplicateAccountingNoteException(String module, String originId) {
        super("Ya existe una nota contable para el origen " + module + ":" + originId);
    }
}
