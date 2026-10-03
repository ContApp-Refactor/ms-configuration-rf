package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando se intenta crear o renombrar una materia con un
 * código que ya existe. Se traduce automáticamente en una respuesta HTTP 409.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class SubjectCodeAlreadyExistsException extends RuntimeException {

    /**
     * Crea la excepción para el código duplicado.
     *
     * @param code el código de materia duplicado
     */
    public SubjectCodeAlreadyExistsException(String code) {
        super("Ya existe una materia con el código '" + code + "'");
    }
}
