package co.unicauca.edu.co.contables.billing.domain.model;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Cantidad estrictamente positiva con escala maxima 3 (almacenada con escala 3). */
public record Quantity(BigDecimal value) {

    private static final int SCALE = 3;

    public Quantity {
        if (value == null) {
            throw new BillingException(BillingErrorCode.BILLING_INVALID_LINE, "La cantidad es obligatoria");
        }
        if (value.signum() <= 0) {
            throw new BillingException(BillingErrorCode.BILLING_INVALID_LINE, "La cantidad debe ser mayor que cero");
        }
        if (value.stripTrailingZeros().scale() > SCALE) {
            throw new BillingException(BillingErrorCode.BILLING_INVALID_LINE,
                    "La cantidad admite maximo 3 decimales");
        }
        value = value.setScale(SCALE, RoundingMode.UNNECESSARY);
    }

    public static Quantity of(BigDecimal quantity) {
        return new Quantity(quantity);
    }
}
