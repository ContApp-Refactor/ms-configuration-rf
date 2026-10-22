package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class DuplicateTaxAccountsException extends BaseBusinessException {

    public DuplicateTaxAccountsException(String message) {
        super(TaxesErrorCode.DUPLICATE_TAX_ACCOUNTS, message);
    }

    public DuplicateTaxAccountsException(String message, Throwable cause) {
        super(TaxesErrorCode.DUPLICATE_TAX_ACCOUNTS, message, cause);
    }
}
