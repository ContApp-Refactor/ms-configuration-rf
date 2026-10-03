package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.exception;

import java.time.LocalDateTime;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import co.unicauca.edu.co.contables.commons.exceptions.ErrorCode;
import co.unicauca.edu.co.contables.commons.exceptions.ErrorResponse;

/**
 * Manejador de excepciones del módulo enterprise.
 *
 * <p>Es <strong>necesario</strong> porque {@code GlobalExceptionHandler}
 * (el advice global del monolito) declara {@code @ExceptionHandler(Exception.class)}
 * sin {@code basePackages}: al resolver antes que {@code ResponseStatusExceptionResolver},
 * convertiría cada excepción de este módulo en un 500 genérico y perderíamos los
 * 404/409 que el frontend espera. Este advice acotado se resuelve primero para
 * las excepciones del paquete {@code ...configuration.enterprise}.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(basePackages = "co.unicauca.edu.co.contables.configuration.enterprise")
public class EnterpriseGlobalExceptionHandler {

    /**
     * Maneja la materia inexistente con un 404.
     *
     * @param ex      la excepción lanzada
     * @param request la solicitud que causó el error
     * @return 404 con el mensaje de la excepción
     */
    @ExceptionHandler(SubjectNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSubjectNotFound(
            SubjectNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex, request);
    }

    /**
     * Maneja el código de materia duplicado con un 409.
     *
     * @param ex      la excepción lanzada
     * @param request la solicitud que causó el error
     * @return 409 con el mensaje de la excepción
     */
    @ExceptionHandler(SubjectCodeAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleSubjectCodeAlreadyExists(
            SubjectCodeAlreadyExistsException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex, request);
    }

    /**
     * Construye la respuesta de error estandarizada del monolito.
     *
     * @param status  el estado HTTP a devolver
     * @param ex      la excepción original
     * @param request la solicitud web
     * @return la respuesta de error con timestamp, status, message, code y path
     */
    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, Exception ex, WebRequest request) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .code(ErrorCode.GENERIC_ERROR.getCode())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return new ResponseEntity<>(body, status);
    }
}
