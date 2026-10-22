package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.paymentMethods;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class PaymentMethodsAlreadyExistsException extends BaseBusinessException {

    public PaymentMethodsAlreadyExistsException(String field, String value, String enterpriseId) {
        super(PaymentMethodsErrorCode.PAYMENT_METHOD_ALREADY_EXISTS,
              String.format("Ya existe un método de pago con %s '%s'", field, value));
    }
}
