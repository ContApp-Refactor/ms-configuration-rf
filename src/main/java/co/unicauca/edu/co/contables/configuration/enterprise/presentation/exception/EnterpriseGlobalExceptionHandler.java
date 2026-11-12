package co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception;

import java.time.LocalDateTime;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import co.unicauca.edu.co.contables.commons.backup.BackupChecksumMismatchException;
import co.unicauca.edu.co.contables.commons.backup.BackupCorruptedException;
import co.unicauca.edu.co.contables.commons.backup.BackupException;
import co.unicauca.edu.co.contables.commons.backup.BackupIntegrityException;
import co.unicauca.edu.co.contables.commons.backup.BackupSizeExceededException;
import co.unicauca.edu.co.contables.commons.backup.UnsupportedBackupVersionException;
import co.unicauca.edu.co.contables.commons.exceptions.ErrorCode;
import co.unicauca.edu.co.contables.commons.exceptions.ErrorResponse;

/**
 * @brief Manejador de excepciones del módulo enterprise.
 *
 * Es necesario porque {@code GlobalExceptionHandler} (el advice global del
 * monolito) declara {@code @ExceptionHandler(Exception.class)} sin
 * {@code basePackages}: al resolver antes que
 * {@code ResponseStatusExceptionResolver}, convertiría cada excepción de
 * este módulo en un 500 genérico. Este advice acotado se resuelve primero
 * para las excepciones del paquete {@code ...configuration.enterprise}.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(basePackages = "co.unicauca.edu.co.contables.configuration.enterprise")
public class EnterpriseGlobalExceptionHandler {

    @ExceptionHandler(SubjectNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSubjectNotFound(SubjectNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex, request);
    }

    @ExceptionHandler(SubjectCodeAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleSubjectCodeAlreadyExists(SubjectCodeAlreadyExistsException ex,
            WebRequest request) {
        return build(HttpStatus.CONFLICT, ex, request);
    }

    @ExceptionHandler(SubjectInUseException.class)
    public ResponseEntity<ErrorResponse> handleSubjectInUse(SubjectInUseException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex, request);
    }

    @ExceptionHandler(EnterpriseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEnterpriseNotFound(EnterpriseNotFoundException ex,
            WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex, request);
    }

    @ExceptionHandler(BackupNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBackupNotFound(BackupNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex, request);
    }

    @ExceptionHandler(InvalidBackupFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBackupFile(InvalidBackupFileException ex,
            WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex, request);
    }

    @ExceptionHandler(BackupTooLargeException.class)
    public ResponseEntity<ErrorResponse> handleBackupTooLarge(BackupTooLargeException ex, WebRequest request) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE, ex, request);
    }

    @ExceptionHandler(InvalidRestoreRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRestoreRequest(InvalidRestoreRequestException ex,
            WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex, request);
    }

    /** El texto técnico de la BD queda en el log ({@code getMessage()}); al front va el legible. */
    @ExceptionHandler(RestoreFailedException.class)
    public ResponseEntity<ErrorResponse> handleRestoreFailed(RestoreFailedException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getUserMessage(), "BACKUP_RESTORE_FAILED", request);
    }

    /** D3: NIT repetido al restaurar como empresa nueva. */
    @ExceptionHandler(EnterpriseNitAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleNitAlreadyExists(EnterpriseNitAlreadyExistsException ex,
            WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), EnterpriseNitAlreadyExistsException.CODE, request);
    }

    /**
     * Jerarquía de {@code commons.backup} (corrupta, checksum, versión, módulo
     * desconocido, tamaño, integridad) y {@link UnsupportedBackupModuleException}:
     * el status sale del {@code @ResponseStatus} de cada subclase (422 por
     * defecto). Sin esto {@code GlobalExceptionHandler(Exception.class)} las
     * devolvería como 500.
     */
    @ExceptionHandler(BackupException.class)
    public ResponseEntity<ErrorResponse> handleBackupException(BackupException ex, WebRequest request) {
        ResponseStatus annotation = AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class);
        HttpStatus status = annotation != null ? annotation.code() : HttpStatus.UNPROCESSABLE_ENTITY;
        return build(status, ex.getMessage(), backupCode(ex), request);
    }

    private static String backupCode(BackupException ex) {
        if (ex instanceof BackupChecksumMismatchException) {
            return "BACKUP_CHECKSUM_MISMATCH";
        }
        if (ex instanceof BackupCorruptedException) {
            return "BACKUP_CORRUPTED";
        }
        if (ex instanceof UnsupportedBackupVersionException) {
            return "BACKUP_UNSUPPORTED_VERSION";
        }
        if (ex instanceof UnsupportedBackupModuleException) {
            return "BACKUP_UNSUPPORTED_MODULE";
        }
        if (ex instanceof PurgeNotSupportedException) {
            return "BACKUP_PURGE_UNSUPPORTED";
        }
        if (ex instanceof BackupSizeExceededException) {
            return "BACKUP_TOO_LARGE";
        }
        if (ex instanceof BackupIntegrityException) {
            return "BACKUP_INTEGRITY";
        }
        return "BACKUP_ERROR";
    }

    @ExceptionHandler(CopyProcessNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCopyProcessNotFound(CopyProcessNotFoundException ex,
            WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex, request);
    }

    @ExceptionHandler(CopyProcessNotTerminalException.class)
    public ResponseEntity<ErrorResponse> handleCopyProcessNotTerminal(CopyProcessNotTerminalException ex,
            WebRequest request) {
        return build(HttpStatus.CONFLICT, ex, request);
    }

    @ExceptionHandler(CorruptBackupException.class)
    public ResponseEntity<ErrorResponse> handleCorruptBackup(CorruptBackupException ex, WebRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex, request);
    }

    @ExceptionHandler(RestoreInPlaceConflictException.class)
    public ResponseEntity<ErrorResponse> handleRestoreInPlaceConflict(RestoreInPlaceConflictException ex,
            WebRequest request) {
        return build(HttpStatus.CONFLICT, ex, request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, Exception ex, WebRequest request) {
        return build(status, ex.getMessage(), ErrorCode.GENERIC_ERROR.getCode(), request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, String code,
            WebRequest request) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .code(code)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return new ResponseEntity<>(body, status);
    }
}
