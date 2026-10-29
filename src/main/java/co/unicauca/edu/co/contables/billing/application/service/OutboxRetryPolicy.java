package co.unicauca.edu.co.contables.billing.application.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Politica de reintentos del relay (EVT-4). Recibe el numero de intento ya incrementado: si alcanza
 * el maximo la fila queda FAILED (definitivo); si no, el proximo intento es
 * {@code ahora + min(base * 2^(n-1), tope)}. Con los valores por defecto: 5 s, 10 s, 20 s ... hasta
 * 10 min, y el intento 10 es el ultimo.
 */
public class OutboxRetryPolicy {

    private final int maxAttempts;
    private final Duration baseBackoff;
    private final Duration maxBackoff;

    public OutboxRetryPolicy(int maxAttempts, Duration baseBackoff, Duration maxBackoff) {
        Objects.requireNonNull(baseBackoff, "baseBackoff");
        Objects.requireNonNull(maxBackoff, "maxBackoff");
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts debe ser >= 1");
        }
        if (baseBackoff.isZero() || baseBackoff.isNegative()) {
            throw new IllegalArgumentException("baseBackoff debe ser positivo");
        }
        if (maxBackoff.compareTo(baseBackoff) < 0) {
            throw new IllegalArgumentException("maxBackoff no puede ser menor que baseBackoff");
        }
        this.maxAttempts = maxAttempts;
        this.baseBackoff = baseBackoff;
        this.maxBackoff = maxBackoff;
    }

    /** Resultado de un intento fallido: definitivo o proxima fecha de reintento. */
    public record Decision(boolean exhausted, Instant nextAttemptAt) {
    }

    /**
     * @param attemptsAfterIncrement numero de intentos fallidos incluyendo el que acaba de ocurrir (>= 1)
     * @param now instante actual
     */
    public Decision decide(int attemptsAfterIncrement, Instant now) {
        if (attemptsAfterIncrement < 1) {
            throw new IllegalArgumentException("El numero de intento debe ser >= 1");
        }
        Objects.requireNonNull(now, "now");
        if (attemptsAfterIncrement >= maxAttempts) {
            // La columna next_attempt_at es NOT NULL: un fallo definitivo conserva "ahora".
            return new Decision(true, now);
        }
        return new Decision(false, now.plus(backoffFor(attemptsAfterIncrement)));
    }

    /** base * 2^(n-1) con tope; se duplica de a un paso para no desbordar con n grandes. */
    private Duration backoffFor(int attempts) {
        Duration delay = baseBackoff;
        for (int i = 1; i < attempts; i++) {
            if (delay.compareTo(maxBackoff) >= 0) {
                return maxBackoff;
            }
            delay = delay.multipliedBy(2);
        }
        return delay.compareTo(maxBackoff) > 0 ? maxBackoff : delay;
    }
}
