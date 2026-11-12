package co.unicauca.edu.co.contables.billing.application.ports.output;

import java.util.List;
import java.util.Optional;

/**
 * Catalogo de metodos de pago de la empresa, de solo lectura. Billing no procesa pagos: solo
 * verifica que el metodo elegido en la factura de venta exista, este activo y sea de la empresa, y
 * congela su nombre.
 */
public interface PaymentCatalogPort {

    /** Metodo de pago de la empresa, o vacio si no existe en ella. */
    Optional<PaymentMethodRef> findPaymentMethod(String enterpriseId, long paymentMethodId);

    /** Metodos de pago ACTIVOS de la empresa (para el selector de la factura). */
    List<PaymentMethodRef> listActivePaymentMethods(String enterpriseId);

    record PaymentMethodRef(long id, String name, boolean active) {
    }
}
