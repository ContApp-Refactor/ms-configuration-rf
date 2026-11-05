package co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @brief Excepción lanzada cuando el restore falla por violación de
 * integridad referencial (por ejemplo, ids de ciudad/departamento/país que
 * no existen en el destino).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RestoreFailedException extends RuntimeException {
    public RestoreFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
