package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.paymentMethods;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class AccountingAccountImmutableException extends BaseBusinessException {
    
    public AccountingAccountImmutableException() {
        super(PaymentMethodsErrorCode.ACCOUNTING_ACCOUNT_IMMUTABLE);
    }
    
    public AccountingAccountImmutableException(String message) {
        super(PaymentMethodsErrorCode.ACCOUNTING_ACCOUNT_IMMUTABLE, message);
    }
}
