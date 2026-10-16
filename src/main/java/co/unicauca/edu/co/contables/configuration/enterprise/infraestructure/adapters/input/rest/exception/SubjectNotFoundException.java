package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando no existe una materia con el código solicitado.
 * Se traduce automáticamente en una respuesta HTTP 404.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class SubjectNotFoundException extends RuntimeException {

    /**
     * Crea la excepción para el código inexistente.
     *
     * @param code el código de la materia que no se encontró
     */
    public SubjectNotFoundException(String code) {
        super("No existe la materia con código '" + code + "'");
    }
}
