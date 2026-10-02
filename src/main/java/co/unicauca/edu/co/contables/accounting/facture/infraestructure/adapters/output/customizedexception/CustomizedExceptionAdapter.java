package co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.customizedexception;

import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import co.unicauca.edu.co.contables.accounting.facture.domain.exception.FactureNotFound;
import co.unicauca.edu.co.contables.accounting.facture.infraestructure.adapters.output.customizedexception.data.response.ExceptionResponse;

import java.net.http.HttpHeaders;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice(basePackages = "co.unicauca.edu.co.contables.accounting.facture")
@RestController
public class CustomizedExceptionAdapter extends ResponseEntityExceptionHandler {
    /**
     * Maneja todas las excepciones que no son manejadas específicamente por otros
     * métodos.
     *
     * @param ex      la excepción que se lanzó
     * @param request la solicitud web actual
     * @return un ResponseEntity que contiene un ExceptionResponse con los detalles
     *         de la excepción
     *         y un estado INTERNAL_SERVER_ERROR
     */
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Object> handleAllExcpetions(Exception ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(LocalDateTime.now(), ex.getMessage(),
                Arrays.asList(request.getDescription(false)));

        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Maneja la excepción FactureNotFound. Devuelve un ResponseEntity
     * con un estado NOT_FOUND y un ExceptionResponse que contiene
     * la descripción de la excepción y los detalles de la solicitud
     * web.
     *
     * @param ex      la excepción que se lanzó
     * @param request la solicitud web actual
     * @return un ResponseEntity que contiene un ExceptionResponse
     *         con los detalles de la excepción y un estado NOT_FOUND
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public final ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(LocalDateTime.now(), ex.getMessage(),
                Arrays.asList(request.getDescription(false)));

        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FactureNotFound.class)
    public final ResponseEntity<Object> handleUserNotFoundException(FactureNotFound ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(LocalDateTime.now(), ex.getMessage(),
                Arrays.asList(request.getDescription(false)));

        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja la excepción MethodArgumentNotValidException. Devuelve un ResponseEntity
     * con un estado BAD_REQUEST y un ExceptionResponse que contiene
     * la descripción de la excepción (Validation Failed) y los mensajes de error
     * de las validaciones que fallaron.
     *
     * @param ex      la excepción que se lanzó
     * @param headers los headers de la respuesta
     * @param status  el estado de la respuesta
     * @param request la solicitud web actual
     * @return un ResponseEntity que contiene un ExceptionResponse
     *         con los detalles de la excepción y un estado BAD_REQUEST
     */
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<String> errors = new ArrayList<String>();
        ex.getBindingResult().getAllErrors().stream().forEach(error -> {
            errors.add(error.getDefaultMessage());
        });

        ExceptionResponse exceptionResponse = new ExceptionResponse(LocalDateTime.now(), "Validation Failed", errors);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
}
