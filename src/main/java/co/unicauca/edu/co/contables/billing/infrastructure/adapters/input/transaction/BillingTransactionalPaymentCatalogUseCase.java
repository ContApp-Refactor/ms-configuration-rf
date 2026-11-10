package co.unicauca.edu.co.contables.billing.infrastructure.adapters.input.transaction;

import co.unicauca.edu.co.contables.billing.application.ports.input.ListPaymentCatalogUseCase;
import co.unicauca.edu.co.contables.billing.application.ports.output.PaymentCatalogPort;
import co.unicauca.edu.co.contables.billing.application.ports.output.PaymentCatalogPort.PaymentMethodRef;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Consulta de solo lectura de los catalogos de pago (metodos de pago). */
@Component("billingTransactionalPaymentCatalogUseCase")
public class BillingTransactionalPaymentCatalogUseCase implements ListPaymentCatalogUseCase {

    private final PaymentCatalogPort catalog;

    public BillingTransactionalPaymentCatalogUseCase(PaymentCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodRef> paymentMethods(String enterpriseId) {
        return catalog.listActivePaymentMethods(enterpriseId);
    }
}
