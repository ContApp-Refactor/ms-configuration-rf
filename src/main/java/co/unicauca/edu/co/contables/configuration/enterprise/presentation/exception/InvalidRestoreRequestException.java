package co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @brief Excepción lanzada cuando la solicitud de restore es inválida: ni
 * {@code backupId} ni archivo, ambos a la vez, o {@code inplace=true} sin
 * {@code targetEnterpriseId}.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRestoreRequestException extends RuntimeException {
    public InvalidRestoreRequestException(String message) {
        super(message);
    }
}
