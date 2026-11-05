package co.unicauca.edu.co.contables.keycloak.controller.exception;

import java.time.LocalDateTime;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Manejador global de excepciones del módulo keycloak (D-12.6).
 * <p>
 * Equivale al {@code GlobalExceptionHandler} del origen, recortado a los cinco
 * tipos que {@code commons.exceptions.GlobalExceptionHandler} NO cubre:
 * {@code RestClientException} (401), {@code UserException} (status propio),
 * {@code ConflictException} (409), {@code ResourceNotFoundException} (404) y
 * {@code IllegalStateException} (409). Los demás (validaciones,
 * {@code IllegalArgumentException}, {@code AccessDeniedException} y el
 * catch-all {@code Exception}) los absorbe commons, igual que en los módulos
 * audit y configuration.
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
