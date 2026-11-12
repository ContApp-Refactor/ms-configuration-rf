package co.unicauca.edu.co.contables.billing.domain.model;

import java.util.Objects;

/**
 * Retencion de una factura (valor inmutable): clase, impuesto de la empresa elegido y su tarifa. El
 * monto no se almacena: es {@code round(subtotal * tarifa / 100)} sobre el SUBTOTAL COMPLETO del
 * documento (no sobre la base del impuesto), con el mismo redondeo que el IVA. {@code taxName} y
 * {@code rate} son la tasa vigente mientras es borrador y quedan congelados al emitir.
 */
public record Withholding(WithholdingCode code, long taxId, String taxName, TaxRate rate) {

    public Withholding {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(rate, "rate");
    }

    /** Monto que resta esta retencion de un subtotal dado. */
    public Money amountOn(Money subtotal) {
        return rate.applyTo(subtotal);
    }
}
