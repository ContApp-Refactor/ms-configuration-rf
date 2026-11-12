package co.unicauca.edu.co.contables.keycloak.controller.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Manejador global de excepciones del módulo keycloak (D-12.6).
 * <p>
 * Equivale al {@code GlobalExceptionHandler} del origen, recortado a los tipos
 * que {@code commons.exceptions.GlobalExceptionHandler} NO cubre o no forma:
 * {@code MethodArgumentNotValidException} (400 plano), {@code AccessDeniedException}
 * (403), {@code RestClientException} (401), {@code UserException} (status propio),
 * {@code ConflictException} (409), {@code ResourceNotFoundException} (404) e
 * {@code IllegalStateException} (409). Los demás ({@code IllegalArgumentException},
 * {@code ConstraintViolationException} y el catch-all {@code Exception}) los
 * absorbe commons, igual que en los módulos audit y configuration.
 * </p>
 * <p>
 * {@code AccessDeniedException} iba originalmente delegado a commons bajo la
 * suposición de que éste lo cubría; commons no lo hace y la excepción caía en
 * su {@code @ExceptionHandler(Exception.class)}, devolviendo un <strong>500</strong>
 * falso donde el origen devolvía <strong>403</strong>. Cubierto acá con el
 * mismo DTO ({@code statusCode}) que el origen.
 * </p>
 * <p>
 * {@code @Order(HIGHEST_PRECEDENCE)} + {@code basePackages} acotado: preserva
 * los códigos HTTP y el DTO de respuesta del origen solo para este paquete.
 * </p>
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "co.unicauca.edu.co.contables.keycloak")
public class KeycloakGlobalExceptionHandler {

    /**
     * Manejo de {@code AccessDeniedException}: 403 (Forbidden).
     * <p>
     * Se activa cuando un usuario intenta acceder a un recurso sin permisos
     * suficientes. Equivalente al manejador del origen; cubre también
     * {@code AuthorizationDeniedException}, que extiende esta excepción.
     * </p>
     *
     * @param ex      excepción de acceso denegado.
     * @param request solicitud HTTP que produjo el error.
     * @return respuesta con detalles del error de autorización.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("Acceso denegado en {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message("No autorizado para acceder a este recurso")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Manejo de {@code ResourceNotFoundException}: 404 (Not Found).
     *
     * @param ex      excepción de recurso no encontrado.
     * @param request solicitud HTTP que produjo el error.
     * @return respuesta con detalles del error de recurso inexistente.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage() != null ? ex.getMessage() : "No se encontró el perfil")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Manejo de {@code MethodArgumentNotValidException}: 400 (Bad Request)
     * con el DTO plano del origen.
     * <p>
     * Sin este manejador la validación cae en commons, que responde envuelto en
     * {@code {"error": ..., "fieldErrors": ...}} y sin {@code statusCode}.
     * </p>
     *
     * @param ex      excepción de validación del cuerpo de la petición.
     * @param request solicitud HTTP que produjo el error.
     * @return respuesta con los detalles de los campos inválidos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<String> details = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    if (error instanceof FieldError fe) {
                        return fe.getField() + ": " + fe.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.toList());

        String message = String.join(", ", details);

        log.warn("Validación fallida en {}: {}", request.getRequestURI(), message);

        ErrorResponse error = ErrorResponse.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Manejo de {@code ConflictException}: 409 (Conflict).
     *
     * @param ex      excepción de conflicto.
     * @param request solicitud HTTP que produjo el error.
     * @return respuesta con detalles del conflicto detectado.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .statusCode(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Manejo de {@code UserException}: usa el status que trae la excepción.
     *
     * @param ex      excepción de usuario.
     * @param request solicitud HTTP que produjo el error.
     * @return respuesta con detalles del error de usuario.
     */
    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleUserException(
            UserException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(ex.getStatus());

        log.warn("UserException: {} - Status: {}", ex.getMessage(), ex.getStatus());

        ErrorResponse error = ErrorResponse.builder()
                .statusCode(ex.getStatus())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(error);
    }

    /**
     * Manejo de {@code RestClientException}: 401 (Unauthorized), típico de
     * fallos de autenticación contra Keycloak.
     *
     * @param ex      excepción de cliente REST.
     * @param request solicitud HTTP que produjo el error.
     * @return respuesta con detalles del error.
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleRestClientError(
            RestClientException ex,
            HttpServletRequest request) {

        log.error("Error en llamada REST: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("Error de autenticación. Por favor verifica tus credenciales.")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Manejo de {@code IllegalStateException}: 409 (Conflict).
     *
     * @param ex      excepción de estado ilegal.
     * @param request solicitud HTTP que produjo el error.
     * @return respuesta con detalles del error.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request) {

        log.warn("Estado ilegal: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .statusCode(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
