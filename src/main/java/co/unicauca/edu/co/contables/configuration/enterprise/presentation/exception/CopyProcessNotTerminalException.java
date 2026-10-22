package co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @brief Excepción lanzada al intentar eliminar un proceso de copia que
 * todavía no llegó a un estado terminal (COMPLETADO, ERROR o CANCELADO).
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class CopyProcessNotTerminalException extends RuntimeException {
    public CopyProcessNotTerminalException(UUID idProceso) {
        super("El proceso de copia '" + idProceso + "' aún no está en un estado terminal");
    }
}
