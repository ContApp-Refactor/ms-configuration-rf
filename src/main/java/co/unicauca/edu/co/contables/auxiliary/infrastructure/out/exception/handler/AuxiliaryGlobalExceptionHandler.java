package co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.handler;

import java.util.HashMap;
import java.util.Map;

import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.customized.*;
import co.unicauca.edu.co.contables.auxiliary.infrastructure.out.exception.dto.ErrorResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;


/**
 * @brief Manejador global de excepciones para toda la aplicación.
 *
 * Intercepta las excepciones lanzadas desde los controladores y las
 * convierte a respuestas HTTP consistentes usando {@link ErrorResponseDTO}.
 * Maneja excepciones personalizadas ({@link BaseException}), errores de
 * validación, parámetros faltantes, tipos incorrectos y cualquier
 * {@link RuntimeException} no contemplada.
 *
 * @author JulianRuano
 */

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "co.unicauca.edu.co.contables.auxiliary")
public class AuxiliaryGlobalExceptionHandler {

    /**
     * @brief Maneja las excepciones de validación de argumentos.
     * Construye un mapa con los errores por campo y lo retorna con HTTP 400.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName;
            if (error instanceof FieldError) {
                fieldName = ((FieldError) error).getField();
            } else {
                fieldName = error.getObjectName();
            }
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest().body(errors);
    }


    /**
     * @brief Maneja las excepciones personalizadas con estructura común.
     * Usa el código y mensaje de la excepción para construir la respuesta.
     */
    @ExceptionHandler({
            BusinessRuleException.class,
            EntityAlreadyExists.class,
            EntityDoesNotExistException.class,
            GenericErrorException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleCustomExceptions(final HttpServletRequest req, BaseException e) {
        return buildErrorResponse(e.getStatus(), e.getMessage(), req);
    }

    /**
     * @brief Maneja diversas excepciones de petición inválida.
     * Retorna HTTP 400 (o 404 si el recurso no se encontró) con un
     * mensaje adaptado al tipo de excepción.
     */
    @ExceptionHandler({
            NoResourceFoundException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class,
            EntityNotFoundException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleBadRequestExceptions(final HttpServletRequest req,Exception ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = ex.getMessage();

        if (ex instanceof NoResourceFoundException) {
            status = HttpStatus.NOT_FOUND;
            message = "Resource not found";
        } else if (ex instanceof MissingServletRequestParameterException) {
            message = "Missing parameter: " + ((MissingServletRequestParameterException) ex).getParameterName();
        } else if (ex instanceof MethodArgumentTypeMismatchException) {
            message = "Method argument type mismatch for '" + ((MethodArgumentTypeMismatchException) ex).getName() + "'";
        }

        return buildErrorResponse(status.value(), message, req);
    }

    /**
     * @brief Maneja RuntimeException no contempladas específicamente.
     * Retorna HTTP 500 con un mensaje genérico de error interno.
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponseDTO> handleRuntimeException(final HttpServletRequest req, RuntimeException ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error", req);
    }

    /**
     * Helper method to build a consistent error response.
     */
    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(int status, String message, HttpServletRequest req) {
        return ErrorResponseDTO.builder()
                .status(status)
                .message(message)
                .url(req.getRequestURI())
                .method(req.getMethod())
                .build()
                .of();
    }
}
