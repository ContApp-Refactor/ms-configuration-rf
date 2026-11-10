package co.unicauca.edu.co.contables.billing.domain.model;

import co.unicauca.edu.co.contables.billing.domain.exception.BillingErrorCode;
import co.unicauca.edu.co.contables.billing.domain.exception.BillingException;

/**
 * Metodo de pago de una factura de VENTA (valor inmutable). Billing no procesa pagos: solo registra
 * el metodo elegido para que Cartera/Tesoreria/Contabilidad actuen despues. {@code paymentMethodName}
 * es el snapshot congelado al emitir.
 * <p>
 * El borrador puede estar incompleto (sin metodo todavia); el metodo se exige al emitir
 * ({@link BillingDocument#emit}).
 */
public record PaymentTerms(Long paymentMethodId, String paymentMethodName) {

    /** Normaliza los datos de un borrador: sin metodo no hay condiciones de pago ({@code null}). */
    public static PaymentTerms draft(Long paymentMethodId) {
        if (paymentMethodId == null) {
            return null;
        }
        return new PaymentTerms(paymentMethodId, null);
    }

    /** Congela el nombre del metodo de pago (snapshot). */
    public PaymentTerms withMethodName(String name) {
        return new PaymentTerms(paymentMethodId, name);
    }

    /** Copia sin el nombre congelado (para duplicar un borrador). */
    public PaymentTerms withoutSnapshot() {
        return new PaymentTerms(paymentMethodId, null);
    }

    /** Valida lo exigido para emitir: el metodo de pago. */
    void assertComplete() {
        if (paymentMethodId == null) {
            throw new BillingException(BillingErrorCode.BILLING_PAYMENT_METHOD_REQUIRED,
                    "La factura de venta requiere un metodo de pago");
        }
    }
}
