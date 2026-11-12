package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.rest.mapper;

import co.unicauca.edu.co.contables.billing.domain.model.Money;
import java.math.BigDecimal;

/** Conversion exacta centavos enteros <-> pesos en el borde REST (D6, DOC-6). */
public final class BillingCents {

    private BillingCents() {
    }

    /** Entrada: {@code BigDecimal.valueOf(cents, 2)}; nulo = sin valor. */
    public static BigDecimal pesos(Long cents) {
        return cents == null ? null : BigDecimal.valueOf(cents, 2);
    }

    /** Salida: centavos exactos (lanza ArithmeticException si excede long). */
    public static long cents(Money money) {
        return money.toCents();
    }
}
