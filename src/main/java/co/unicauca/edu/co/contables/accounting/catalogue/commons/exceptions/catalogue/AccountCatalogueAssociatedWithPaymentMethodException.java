package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class AccountCatalogueAssociatedWithPaymentMethodException extends BaseBusinessException {

    public AccountCatalogueAssociatedWithPaymentMethodException() {
        super(AccountCatalogueErrorCode.ACCOUNT_ASSOCIATED_WITH_PAYMENT_METHOD);
    }

    public AccountCatalogueAssociatedWithPaymentMethodException(String customMessage) {
        super(AccountCatalogueErrorCode.ACCOUNT_ASSOCIATED_WITH_PAYMENT_METHOD, customMessage);
    }

    public AccountCatalogueAssociatedWithPaymentMethodException(String customMessage, Throwable cause) {
        super(AccountCatalogueErrorCode.ACCOUNT_ASSOCIATED_WITH_PAYMENT_METHOD, customMessage, cause);
    }
}