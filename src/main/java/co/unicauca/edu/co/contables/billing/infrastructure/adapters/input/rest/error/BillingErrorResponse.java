package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.error;

import java.time.Instant;
import java.util.Map;

/** Cuerpo de error del modulo billing (ERR-2). */
public record BillingErrorResponse(
        String code,
        String message,
        int status,
        String path,
        Instant timestamp,
        Map<String, Object> details) {
}
