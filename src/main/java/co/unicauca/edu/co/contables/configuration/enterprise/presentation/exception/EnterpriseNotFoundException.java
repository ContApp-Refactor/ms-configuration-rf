package co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @brief Excepción lanzada cuando no existe una empresa con el id dado.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EnterpriseNotFoundException extends RuntimeException {
    public EnterpriseNotFoundException(UUID id) {
        super("No existe la empresa con id '" + id + "'");
    }
}
