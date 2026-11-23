package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class TaxAlreadyExistsException extends BaseBusinessException {

    public TaxAlreadyExistsException(String message) {
        super(TaxesErrorCode.TAX_ALREADY_EXISTS, message);
    }

    public TaxAlreadyExistsException(String message, Throwable cause) {
        super(TaxesErrorCode.TAX_ALREADY_EXISTS, message, cause);
    }
}
