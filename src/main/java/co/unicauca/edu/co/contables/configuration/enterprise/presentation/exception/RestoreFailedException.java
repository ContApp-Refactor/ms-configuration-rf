package co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @brief Excepción lanzada cuando el restore falla por violación de
 * integridad referencial (por ejemplo, ids de ciudad/departamento/país que
 * no existen en el destino).
 *
 * {@link #getMessage()} conserva la causa técnica (texto de la BD) para el
 * log; {@link #getUserMessage()} es el texto en español que se muestra al
 * usuario (advice HTTP y {@code errorResumen} del proceso de copia).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RestoreFailedException extends RuntimeException {

    private static final String DEFAULT_USER_MESSAGE =
            "No se pudo restaurar la copia: contiene datos que la base de datos rechazó.";

    private final String userMessage;

    public RestoreFailedException(String message, Throwable cause) {
        this(message, DEFAULT_USER_MESSAGE, cause);
    }

    public RestoreFailedException(String message, String userMessage, Throwable cause) {
        super(message, cause);
        this.userMessage = userMessage == null || userMessage.isBlank() ? DEFAULT_USER_MESSAGE : userMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
