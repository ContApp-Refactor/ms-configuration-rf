package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class AccountCatalogueHasChildrenException extends BaseBusinessException {
    
    public AccountCatalogueHasChildrenException() {
        super(AccountCatalogueErrorCode.ACCOUNT_HAS_CHILDREN);
    }
    
    public AccountCatalogueHasChildrenException(String customMessage) {
        super(AccountCatalogueErrorCode.ACCOUNT_HAS_CHILDREN, customMessage);
    }
    
    public AccountCatalogueHasChildrenException(String customMessage, Throwable cause) {
        super(AccountCatalogueErrorCode.ACCOUNT_HAS_CHILDREN, customMessage, cause);
    }
}
