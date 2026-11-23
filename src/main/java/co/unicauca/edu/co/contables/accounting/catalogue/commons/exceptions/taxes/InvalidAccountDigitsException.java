package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class InvalidAccountDigitsException extends BaseBusinessException {

    public InvalidAccountDigitsException() {
        super(TaxesErrorCode.INVALID_ACCOUNT_DIGITS);
    }

    public InvalidAccountDigitsException(String message) {
        super(TaxesErrorCode.INVALID_ACCOUNT_DIGITS, message);
    }

    public InvalidAccountDigitsException(String message, Throwable cause) {
        super(TaxesErrorCode.INVALID_ACCOUNT_DIGITS, message, cause);
    }
}
