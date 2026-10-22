package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.paymentMethods;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class PaymentMethodsNotFoundException extends BaseBusinessException {

    public PaymentMethodsNotFoundException() {
        super(PaymentMethodsErrorCode.PAYMENT_METHOD_NOT_FOUND);
    }

    public PaymentMethodsNotFoundException(String customMessage) {
        super(PaymentMethodsErrorCode.PAYMENT_METHOD_NOT_FOUND, customMessage);
    }
}
