package co.unicauca.edu.co.contables.billing.support;

import co.unicauca.edu.co.contables.billing.application.ports.output.PaymentCatalogPort;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Catalogo de pago en memoria para pruebas: metodos de pago registrados a mano. */
public class FakePaymentCatalogPort implements PaymentCatalogPort {

    private final Map<Long, PaymentMethodRef> methods = new LinkedHashMap<>();

    /** Incluye el metodo 1 "Efectivo" (activo): el pago por defecto de las ventas de prueba. */
    public FakePaymentCatalogPort() {
        withMethod(1L, "Efectivo", true);
    }

    public FakePaymentCatalogPort withMethod(long id, String name, boolean active) {
        methods.put(id, new PaymentMethodRef(id, name, active));
        return this;
    }

    @Override
    public Optional<PaymentMethodRef> findPaymentMethod(String enterpriseId, long paymentMethodId) {
        return Optional.ofNullable(methods.get(paymentMethodId));
    }

    @Override
    public List<PaymentMethodRef> listActivePaymentMethods(String enterpriseId) {
        return new ArrayList<>(methods.values().stream().filter(PaymentMethodRef::active).toList());
    }
}
