package co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @brief Excepción lanzada cuando no existe una materia con el código dado.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class SubjectNotFoundException extends RuntimeException {
    public SubjectNotFoundException(String code) {
        super("No existe la materia con código '" + code + "'");
    }
}
