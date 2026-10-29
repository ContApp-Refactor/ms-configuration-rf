package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.error;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PessimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Advice acotado al paquete billing con maxima precedencia, para que ningun advice global
 * del monolito intercepte las excepciones del modulo (ERR-1, ERR-2).
 */
@RestControllerAdvice(basePackages = "co.unicauca.edu.co.contables.billing")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BillingRestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(BillingRestExceptionHandler.class);

    @ExceptionHandler(BillingException.class)
    public ResponseEntity<BillingErrorResponse> handleBilling(BillingException ex, HttpServletRequest request) {
        HttpStatus status = toStatus(ex.category());
        if (status.is5xxServerError()) {
            log.error("Billing error {}: {}", ex.errorCode(), ex.getMessage(), ex);
        } else {
            log.debug("Billing error {}: {}", ex.errorCode(), ex.getMessage());
        }
        return build(status, ex.errorCode(), ex.getMessage(), request, null);
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<BillingErrorResponse> handleOptimistic(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, BillingErrorCode.BILLING_CONCURRENT_MODIFICATION,
                "El documento fue modificado por otro usuario", request, null);
    }

    @ExceptionHandler({
            PessimisticLockingFailureException.class,
            QueryTimeoutException.class,
            LockTimeoutException.class,
            PessimisticLockException.class,
            jakarta.persistence.QueryTimeoutException.class})
    public ResponseEntity<BillingErrorResponse> handleLock(Exception ex, HttpServletRequest request) {
        log.warn("Billing lock/timeout failure: {}", ex.toString());
        return build(HttpStatus.SERVICE_UNAVAILABLE, BillingErrorCode.BILLING_SEQUENCE_BUSY,
                "El recurso esta ocupado, intente de nuevo", request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BillingErrorResponse> handleInvalidBody(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> details = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> details.putIfAbsent(fe.getField(), String.valueOf(fe.getDefaultMessage())));
        return build(HttpStatus.BAD_REQUEST, BillingErrorCode.BILLING_VALIDATION_ERROR,
                "Solicitud invalida", request, details);
    }

    @ExceptionHandler({
            HandlerMethodValidationException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            NumberFormatException.class})
    public ResponseEntity<BillingErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        log.debug("Billing bad request: {}", ex.toString());
        return build(HttpStatus.BAD_REQUEST, BillingErrorCode.BILLING_VALIDATION_ERROR,
                "Solicitud invalida", request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BillingErrorResponse> handleDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, BillingErrorCode.BILLING_FORBIDDEN,
                "No tiene permiso para esta operacion", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BillingErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected billing error at {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, BillingErrorCode.BILLING_INTERNAL_ERROR,
                "Error interno del servidor", request, null);
    }

    static HttpStatus toStatus(BillingErrorCode.ErrorCategory category) {
        return switch (category) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case UNPROCESSABLE -> HttpStatus.UNPROCESSABLE_ENTITY;
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case INTERNAL -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private ResponseEntity<BillingErrorResponse> build(
            HttpStatus status,
            BillingErrorCode code,
            String message,
            HttpServletRequest request,
            Map<String, Object> details) {
        BillingErrorResponse body = new BillingErrorResponse(
                code.code(),
                message,
                status.value(),
                request.getRequestURI(),
                Instant.now(),
                details == null ? Map.of() : details);
        return ResponseEntity.status(status).body(body);
    }
}
