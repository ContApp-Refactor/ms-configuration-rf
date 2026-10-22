package co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @brief Excepción lanzada cuando un restore {@code inplace=true} no pudo
 * sobrescribir (borrar la ubicación/tipo de persona anteriores de) la
 * empresa destino, normalmente por una violación de integridad referencial.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class RestoreInPlaceConflictException extends RuntimeException {
    public RestoreInPlaceConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
