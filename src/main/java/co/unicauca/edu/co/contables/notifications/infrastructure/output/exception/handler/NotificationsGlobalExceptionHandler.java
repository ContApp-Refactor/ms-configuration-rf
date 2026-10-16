package co.unicauca.edu.co.contables.notifications.infrastructure.output.exception.handler;

import co.unicauca.edu.co.contables.notifications.domain.exception.MessageProcessingErrorNotFoundException;
import co.unicauca.edu.co.contables.notifications.infrastructure.input.rest.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

/**
 * Manejador global de excepciones específico del módulo de notificaciones.
 * Captura las excepciones lanzadas por los controladores REST de este módulo
 * y las convierte en el formato estandarizado {@link ApiResponse}.
 * Está limitado a {@code basePackages} para no interferir con el
 * {@code GlobalExceptionHandler} global de commons.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "co.unicauca.edu.co.contables.notifications")
@Slf4j

public class NotificationsGlobalExceptionHandler {

        // Método helper para obtener la URI
        private String getRequestPath(WebRequest request) {
                return ((ServletWebRequest) request).getRequest().getRequestURI();
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(
                        MethodArgumentNotValidException ex,
                        WebRequest request) {

                // Tomamos el PRIMER error de validación
                FieldError fieldError = ex.getBindingResult().getFieldErrors().get(0);

                String message = fieldError.getDefaultMessage();

                HttpStatus status = HttpStatus.BAD_REQUEST;

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                message,
                                "VALIDATION_ERROR",
                                status,
                                getRequestPath(request));

                return new ResponseEntity<>(apiResponse, status);
        }

        // --- MANEJADORES PARA EXCEPCIONES "NOT FOUND" (404) ---

        @ExceptionHandler(MessageProcessingErrorNotFoundException.class)
        public ResponseEntity<ApiResponse<Object>> handleMessageProcessingErrorNotFoundException(
                        MessageProcessingErrorNotFoundException ex, WebRequest request) {

                log.info("MessageProcessingErrorNotFoundException: {} at path {}", ex.getMessage(),
                                getRequestPath(request));
                HttpStatus status = HttpStatus.NOT_FOUND;
                ApiResponse<Object> apiResponse = ApiResponse.error(ex.getMessage(), "MESSAGE_ERROR_NOT_FOUND", status,
                                getRequestPath(request));

                return new ResponseEntity<>(apiResponse, status);
        }

        // --- MANEJADORES PARA ERRORES DE LÓGICA DE NEGOCIO ---

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ApiResponse<Object>> handleIllegalStateException(IllegalStateException ex,
                        WebRequest request) {
                log.warn("IllegalStateException: {} at path {}", ex.getMessage(), getRequestPath(request));
                HttpStatus status = HttpStatus.CONFLICT;
                ApiResponse<Object> apiResponse = ApiResponse.error(ex.getMessage(), "INVALID_STATE_OPERATION", status,
                                getRequestPath(request));
                return new ResponseEntity<>(apiResponse, status);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex,
                        WebRequest request) {
                log.warn("IllegalArgumentException: {} at path {}", ex.getMessage(), getRequestPath(request));
                HttpStatus status = HttpStatus.BAD_REQUEST;
                ApiResponse<Object> apiResponse = ApiResponse.error(ex.getMessage(), "INVALID_ARGUMENT", status,
                                getRequestPath(request));
                return new ResponseEntity<>(apiResponse, status);
        }

        // --- MANEJADOR GENÉRICO DE SEGURIDAD (500) ---

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex, WebRequest request) {
                log.error("An unexpected error occurred at path {}: ", getRequestPath(request), ex);
                HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "Ocurrió un error inesperado en el servidor. Por favor, contacte a soporte.",
                                "INTERNAL_SERVER_ERROR",
                                status,
                                getRequestPath(request));
                return new ResponseEntity<>(apiResponse, status);
        }
}
