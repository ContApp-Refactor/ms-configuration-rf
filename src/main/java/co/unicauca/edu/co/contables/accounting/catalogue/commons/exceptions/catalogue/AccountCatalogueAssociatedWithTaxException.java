package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class AccountCatalogueAssociatedWithTaxException extends BaseBusinessException {
    
    public AccountCatalogueAssociatedWithTaxException() {
        super(AccountCatalogueErrorCode.ACCOUNT_ASSOCIATED_WITH_TAX);
    }
    
    public AccountCatalogueAssociatedWithTaxException(String customMessage) {
        super(AccountCatalogueErrorCode.ACCOUNT_ASSOCIATED_WITH_TAX, customMessage);
    }
    
    public AccountCatalogueAssociatedWithTaxException(String customMessage, Throwable cause) {
        super(AccountCatalogueErrorCode.ACCOUNT_ASSOCIATED_WITH_TAX, customMessage, cause);
    }
}
