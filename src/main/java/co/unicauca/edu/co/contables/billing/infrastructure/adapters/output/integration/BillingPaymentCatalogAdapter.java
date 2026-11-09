package co.unicauca.edu.co.contables.billing.infrastructure.adapters.output.integration;

import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.paymentMethods.PaymentMethodsNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.model.PaymentMethod;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.services.IPaymentMethodService;
import co.unicauca.edu.co.contables.billing.application.ports.output.PaymentCatalogPort;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Metodos de pago del modulo de contabilidad, resueltos <strong>en el mismo
 * proceso</strong> mediante su servicio de dominio ({@link IPaymentMethodService}), nunca
 * repositorios ni entidades. {@code findById} busca por id Y
 * empresa, asi que un registro de otra empresa se comporta como inexistente. Un registro
 * inexistente se traduce a vacio; cualquier otro error se propaga. Estado nulo se trata como
 * inactivo.
 */
@Component("billingPaymentCatalogAdapter")
public class BillingPaymentCatalogAdapter implements PaymentCatalogPort {

    /** Tope de registros al listar (los selectores no paginan). */
    static final int LIST_LIMIT = 500;

    private final IPaymentMethodService paymentMethods;

    public BillingPaymentCatalogAdapter(IPaymentMethodService paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    @Override
    public Optional<PaymentMethodRef> findPaymentMethod(String enterpriseId, long paymentMethodId) {
        PaymentMethod method;
        try {
            method = paymentMethods.findById(paymentMethodId, enterpriseId);
        } catch (PaymentMethodsNotFoundException ex) {
            return Optional.empty();
        }
        return method == null ? Optional.empty() : Optional.of(toRef(method, paymentMethodId));
    }

    @Override
    public List<PaymentMethodRef> listActivePaymentMethods(String enterpriseId) {
        return paymentMethods.findAllActiveByEnterprise(enterpriseId, Optional.of(0), Optional.of(LIST_LIMIT))
                .getContent().stream()
                .filter(m -> m.getId() != null)
                .map(m -> toRef(m, m.getId()))
                .filter(PaymentMethodRef::active)
                .toList();
    }

    private static PaymentMethodRef toRef(PaymentMethod method, long id) {
        return new PaymentMethodRef(id, method.getName(), Boolean.TRUE.equals(method.getStatus()));
    }
}
