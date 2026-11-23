package co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @brief Excepción lanzada cuando no existe un proceso de copia con el id
 * dado.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CopyProcessNotFoundException extends RuntimeException {
    public CopyProcessNotFoundException(UUID idProceso) {
        super("No existe el proceso de copia con id '" + idProceso + "'");
    }
}
