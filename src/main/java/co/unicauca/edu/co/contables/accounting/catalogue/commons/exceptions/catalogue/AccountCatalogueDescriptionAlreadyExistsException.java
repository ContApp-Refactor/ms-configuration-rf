package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class AccountCatalogueDescriptionAlreadyExistsException extends BaseBusinessException {
    
    public AccountCatalogueDescriptionAlreadyExistsException() {
        super(AccountCatalogueErrorCode.ACCOUNT_DESCRIPTION_ALREADY_EXISTS);
    }
    
    public AccountCatalogueDescriptionAlreadyExistsException(String customMessage) {
        super(AccountCatalogueErrorCode.ACCOUNT_DESCRIPTION_ALREADY_EXISTS, customMessage);
    }
    
    public AccountCatalogueDescriptionAlreadyExistsException(String customMessage, Throwable cause) {
        super(AccountCatalogueErrorCode.ACCOUNT_DESCRIPTION_ALREADY_EXISTS, customMessage, cause);
    }
}
