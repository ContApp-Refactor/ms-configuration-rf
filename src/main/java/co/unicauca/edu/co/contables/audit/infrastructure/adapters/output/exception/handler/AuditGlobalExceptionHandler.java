package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.handler;

import co.unicauca.edu.co.contables.audit.domain.exceptions.AuditDomainException;
import co.unicauca.edu.co.contables.audit.domain.exceptions.ExportAuditException;
import co.unicauca.edu.co.contables.audit.domain.exceptions.InvalidAuditEventException;

import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.dto.ErrorResponseDto;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.security.AuditMappingException;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.security.AuthenticationRequiredException;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.security.DuplicateResourceException;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.security.EntityNotFoundException;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.security.InvalidJwtAuthenticationException;
import co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.exception.security.MissingHeaderException;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

/**
 * @brief Manejador global de excepciones del módulo audit.
 *
 *        Equivale al {@code GlobalExceptionHandler} del origen, recortado a los
 *        tipos que {@code commons.exceptions.GlobalExceptionHandler} NO cubre:
 *        las excepciones propias de audit y aquellos tipos sin manejador en
 *        commons. Los que commons ya cubre se descartan para no duplicar
 *        comportamiento ({@code MethodArgumentNotValidException},
 *        {@code ConstraintViolationException},
 *        {@code HttpMessageNotReadableException},
 *        {@code MethodArgumentTypeMismatchException},
 *        {@code IllegalArgumentException},
 *        {@code AccessDeniedException},
 *        {@code AuthorizationDeniedException} y el catch-all
 *        {@code Exception.class}).
 *
 *        Se limita a {@code basePackages} y queda por delante del advice global
 *        para preservar los códigos HTTP y el DTO de respuesta del origen.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "co.unicauca.edu.co.contables.audit")
public class AuditGlobalExceptionHandler {

        @ExceptionHandler(InvalidAuditEventException.class)
        public ResponseEntity<ErrorResponseDto> handleInvalidAuditEventException(
                        InvalidAuditEventException ex,
                        WebRequest request) {

                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.BAD_REQUEST,
                                "Invalid Audit Event",
                                ex.getMessage(),
                                getPath(request));
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(error);
        }

        // commons no cubre BindException: sin este manejador el @Valid sobre
        // @ModelAttribute caería en el catch-all de commons (500 en vez de 400).
        // MethodArgumentNotValidException extiende BindException, así que este
        // manejador también cubre el @Valid sobre @RequestBody, como en el origen.
        @ExceptionHandler(BindException.class)
        public ResponseEntity<ErrorResponseDto> handleBindException(
                        BindException ex,
                        WebRequest request) {

                Map<String, String> validationErrors = new HashMap<>();

                ex.getBindingResult().getFieldErrors()
                                .forEach(error -> validationErrors.put(error.getField(), error.getDefaultMessage()));

                ErrorResponseDto response = ErrorResponseFactory.buildValidation(
                                "Invalid request parameters",
                                getPath(request),
                                validationErrors);

                return ResponseEntity.badRequest().body(response);
        }

        @ExceptionHandler(AuditDomainException.class)
        public ResponseEntity<ErrorResponseDto> handleAuditDomainException(
                        AuditDomainException ex,
                        WebRequest request) {
                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.UNPROCESSABLE_ENTITY,
                                "Business Rule Violation",
                                ex.getMessage(),
                                getPath(request));
                return ResponseEntity
                                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                                .body(error);
        }

        private String getPath(WebRequest request) {
                return request.getDescription(false).replace("uri=", "");
        }

        @ExceptionHandler(MissingHeaderException.class)
        public ResponseEntity<ErrorResponseDto> handleMissingHeaderException(MissingHeaderException ex,
                        WebRequest request) {
                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.BAD_REQUEST,
                                "Bad Request",
                                ex.getMessage(),
                                getPath(request));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        @ExceptionHandler(AuthenticationRequiredException.class)
        public ResponseEntity<ErrorResponseDto> handleAuthenticationRequired(
                        AuthenticationRequiredException ex,
                        WebRequest request) {

                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.UNAUTHORIZED,
                                "Authentication Required",
                                ex.getMessage(),
                                getPath(request));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<ErrorResponseDto> handleResponseStatusException(
                        ResponseStatusException ex,
                        WebRequest request) {

                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.valueOf(ex.getStatusCode().value()),
                                HttpStatus.valueOf(ex.getStatusCode().value()).getReasonPhrase(),
                                ex.getReason(),
                                getPath(request));

                return ResponseEntity
                                .status(ex.getStatusCode())
                                .body(error);
        }

        @ExceptionHandler(InvalidJwtAuthenticationException.class)
        public ResponseEntity<ErrorResponseDto> handleInvalidJwt(
                        InvalidJwtAuthenticationException ex,
                        WebRequest request) {

                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.UNAUTHORIZED,
                                "Unauthorized",
                                ex.getMessage(),
                                getPath(request));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        @ExceptionHandler(ExportAuditException.class)
        public ResponseEntity<ErrorResponseDto> handleExportException(
                        ExportAuditException ex, WebRequest request) {
                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Export Error",
                                "Error generating export file. Please try again.",
                                getPath(request));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<ErrorResponseDto> handleEntityNotFound(
                        EntityNotFoundException ex,
                        WebRequest request) {

                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.NOT_FOUND,
                                "Resource Not Found",
                                ex.getMessage(),
                                getPath(request));

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ErrorResponseDto> handleDuplicateResource(
                        DuplicateResourceException ex,
                        WebRequest request) {
                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.CONFLICT,
                                "Resource Conflict",
                                ex.getMessage(),
                                getPath(request));
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ErrorResponseDto> handleMethodNotSupported(
                        HttpRequestMethodNotSupportedException ex,
                        WebRequest request) {

                String message = String.format(
                                "HTTP method '%s' is not supported for this endpoint",
                                ex.getMethod());

                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.METHOD_NOT_ALLOWED,
                                "Method Not Allowed",
                                message,
                                getPath(request));

                return ResponseEntity
                                .status(HttpStatus.METHOD_NOT_ALLOWED)
                                .body(error);
        }

        @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
        public ResponseEntity<ErrorResponseDto> handleMediaTypeNotSupported(
                        HttpMediaTypeNotSupportedException ex,
                        WebRequest request) {
                String message = String.format(
                                "Content type '%s' is not supported",
                                ex.getContentType());
                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                                "Unsupported Media Type",
                                message,
                                getPath(request));
                return ResponseEntity
                                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                .body(error);
        }

        @ExceptionHandler(ResourceAccessException.class)
        public ResponseEntity<ErrorResponseDto> handleResourceAccess(
                        ResourceAccessException ex,
                        WebRequest request) {
                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.SERVICE_UNAVAILABLE,
                                "Service Unavailable",
                                "External service is temporarily unavailable",
                                getPath(request));
                return ResponseEntity
                                .status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(error);
        }

        @ExceptionHandler(AuditMappingException.class)
        public ResponseEntity<ErrorResponseDto> handleAuditMappingException(
                        AuditMappingException ex,
                        WebRequest request) {
                ErrorResponseDto error = ErrorResponseFactory.build(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Audit Mapping Error",
                                ex.getMessage(),
                                getPath(request));

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(error);
        }
}
