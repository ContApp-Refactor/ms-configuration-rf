package co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @brief Excepción lanzada cuando ya existe una materia con el código dado.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class SubjectCodeAlreadyExistsException extends RuntimeException {
    public SubjectCodeAlreadyExistsException(String code) {
        super("Ya existe una materia con el código '" + code + "'");
    }
}
