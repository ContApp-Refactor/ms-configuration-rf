package co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.infrastructure.config;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.controller.AccountingNoteController;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.exception.AccountingNoteNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.manualnotes.domain.exception.DuplicateAccountingNoteException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Map;

@RestControllerAdvice(basePackageClasses = AccountingNoteController.class)
public class RegcontableExceptionHandler {
    @ExceptionHandler(AccountingNoteNotFoundException.class)
    ResponseEntity<?> notFound(AccountingNoteNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateAccountingNoteException.class)
    ResponseEntity<?> conflict(DuplicateAccountingNoteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> integrityConflict(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "La operación viola una restricción de unicidad o integridad."));
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<?> invalidState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    ResponseEntity<?> badRequest(Exception ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
