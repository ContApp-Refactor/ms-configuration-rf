package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class AccountCatalogueNotFoundException extends BaseBusinessException {
    
    public AccountCatalogueNotFoundException() {
        super(AccountCatalogueErrorCode.ACCOUNT_NOT_FOUND);
    }
    
    public AccountCatalogueNotFoundException(String customMessage) {
        super(AccountCatalogueErrorCode.ACCOUNT_NOT_FOUND, customMessage);
    }
    
    public AccountCatalogueNotFoundException(String customMessage, Throwable cause) {
        super(AccountCatalogueErrorCode.ACCOUNT_NOT_FOUND, customMessage, cause);
    }
}
