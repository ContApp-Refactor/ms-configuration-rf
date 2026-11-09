package co.unicauca.edu.co.contables.billing.application.ports.input;

import co.unicauca.edu.co.contables.billing.application.ports.output.PaymentCatalogPort.PaymentMethodRef;
import java.util.List;

/** Selectores de pago de la factura de venta: metodos de pago ACTIVOS de la empresa. */
public interface ListPaymentCatalogUseCase {

    List<PaymentMethodRef> paymentMethods(String enterpriseId);
}
