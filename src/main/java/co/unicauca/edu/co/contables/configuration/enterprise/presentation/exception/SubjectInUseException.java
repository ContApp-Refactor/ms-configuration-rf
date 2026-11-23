package co.unicauca.edu.co.contables.configuration.enterprise.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @brief Excepcion lanzada al eliminar una materia asignada a una o mas empresas.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class SubjectInUseException extends RuntimeException {
    public SubjectInUseException(String code) {
        super("La materia '" + code + "' estÃ¡ asignada a una o mÃ¡s empresas; quÃ­tala de ellas antes de eliminarla");
    }
}
