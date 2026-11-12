package co.unicauca.edu.co.contables.billing.domain.model;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;

/**
 * Clase de retencion de una factura. RETE_IVA, RETE_ICA y RETE_FUENTE aplican a venta y compra;
 * AUTO_RENTA solo a venta. Las devoluciones no llevan retenciones.
 */
public enum WithholdingCode {
    RETE_IVA,
    RETE_ICA,
    RETE_FUENTE,
    AUTO_RENTA;

    public boolean allowedFor(DocumentType type) {
        if (type == null || !type.isInvoice()) {
            return false;
        }
        return this != AUTO_RENTA || type == DocumentType.SALE_INVOICE;
    }

    /** Falla con BILLING_WITHHOLDING_INVALID si la retencion no aplica al tipo de documento. */
    public void requireAllowedFor(DocumentType type) {
        if (!allowedFor(type)) {
            throw new BillingException(BillingErrorCode.BILLING_WITHHOLDING_INVALID,
                    "La retencion " + name() + " no aplica a " + type);
        }
    }

    /** Interpreta el codigo recibido (sin distinguir mayusculas); desconocido = BILLING_WITHHOLDING_INVALID. */
    public static WithholdingCode parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BillingException(BillingErrorCode.BILLING_WITHHOLDING_INVALID,
                    "El codigo de la retencion es obligatorio");
        }
        try {
            return valueOf(raw.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BillingException(BillingErrorCode.BILLING_WITHHOLDING_INVALID,
                    "Codigo de retencion desconocido: " + raw);
        }
    }
}
