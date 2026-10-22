package co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @brief Excepción lanzada cuando el ZIP de backup está corrupto, no contiene
 * {@code enterprise.json} o su contenido no se puede deserializar.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidBackupFileException extends RuntimeException {
    public InvalidBackupFileException(String message) {
        super(message);
    }

    public InvalidBackupFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
