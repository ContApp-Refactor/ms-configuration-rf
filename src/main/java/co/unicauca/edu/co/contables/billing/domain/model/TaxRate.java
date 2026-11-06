package co.unicauca.edu.co.contables.billing.domain.model;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Porcentaje de IVA entre 0 y 100 con escala maxima 2 (DOC-7). */
public record TaxRate(BigDecimal value) {

    private static final int SCALE = 2;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public TaxRate {
        if (value == null) {
            throw new BillingException(BillingErrorCode.BILLING_INVALID_LINE, "La tasa de IVA es obligatoria");
        }
        if (value.signum() < 0 || value.compareTo(HUNDRED) > 0) {
            throw new BillingException(BillingErrorCode.BILLING_INVALID_LINE,
                    "La tasa de IVA debe estar entre 0 y 100");
        }
        if (value.stripTrailingZeros().scale() > SCALE) {
            throw new BillingException(BillingErrorCode.BILLING_INVALID_LINE,
                    "La tasa de IVA admite maximo 2 decimales");
        }
        value = value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static TaxRate of(BigDecimal rate) {
        return new TaxRate(rate);
    }

    public static TaxRate zero() {
        return new TaxRate(BigDecimal.ZERO);
    }

    /** IVA de una base: round(base * rate / 100), escala 2 HALF_UP. */
    public Money applyTo(Money base) {
        return Money.of(base.value().multiply(value).divide(HUNDRED, SCALE, RoundingMode.HALF_UP));
    }
}
