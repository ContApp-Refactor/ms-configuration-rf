package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class AccountCatalogueInactiveException extends BaseBusinessException {

    public AccountCatalogueInactiveException() {
        super(AccountCatalogueErrorCode.ACCOUNT_INACTIVE);
    }

    public AccountCatalogueInactiveException(String customMessage) {
        super(AccountCatalogueErrorCode.ACCOUNT_INACTIVE, customMessage);
    }

    public AccountCatalogueInactiveException(String customMessage, Throwable cause) {
        super(AccountCatalogueErrorCode.ACCOUNT_INACTIVE, customMessage, cause);
    }
}