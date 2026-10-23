package co.unicauca.edu.co.contables.billing.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Valor monetario en pesos con escala 2 y redondeo HALF_UP (DOC-5). Nunca usa punto flotante.
 */
public record Money(BigDecimal value) {

    private static final int SCALE = 2;
    private static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money {
        Objects.requireNonNull(value, "value");
        value = value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount);
    }

    /** Conversion exacta desde centavos enteros (DOC-6). */
    public static Money ofCents(long cents) {
        return new Money(BigDecimal.valueOf(cents, SCALE));
    }

    public static Money zero() {
        return ZERO;
    }

    public Money plus(Money other) {
        return new Money(value.add(other.value));
    }

    /** Producto por cantidad, redondeado a escala 2 HALF_UP. */
    public Money times(Quantity quantity) {
        return new Money(value.multiply(quantity.value()));
    }

    public boolean isPositive() {
        return value.signum() > 0;
    }

    public boolean isNegative() {
        return value.signum() < 0;
    }

    /** Centavos exactos; lanza ArithmeticException si excede long (DOC-6). */
    public long toCents() {
        return value.movePointRight(SCALE).longValueExact();
    }
}
