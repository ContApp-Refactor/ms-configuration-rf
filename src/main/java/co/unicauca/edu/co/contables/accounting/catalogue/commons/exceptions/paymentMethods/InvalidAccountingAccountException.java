package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.paymentMethods;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class InvalidAccountingAccountException extends BaseBusinessException {
    
    public InvalidAccountingAccountException() {
        super(PaymentMethodsErrorCode.INVALID_ACCOUNTING_ACCOUNT);
    }
    
    public InvalidAccountingAccountException(String customMessage) {
        super(PaymentMethodsErrorCode.INVALID_ACCOUNTING_ACCOUNT, customMessage);
    }
    
    public InvalidAccountingAccountException(String customMessage, Throwable cause) {
        super(PaymentMethodsErrorCode.INVALID_ACCOUNTING_ACCOUNT, customMessage, cause);
    }
}
