package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.exception;

public class AccountingNoteNotFoundException extends RuntimeException {
    public AccountingNoteNotFoundException(Long id) {
        super("No se encontró la nota contable con ID: " + id);
    }
}
