package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class TaxNotFoundException extends BaseBusinessException {

    public TaxNotFoundException(String message) {
        super(TaxesErrorCode.TAX_NOT_FOUND, message);
    }

    public TaxNotFoundException(String message, Throwable cause) {
        super(TaxesErrorCode.TAX_NOT_FOUND, message, cause);
    }
}
