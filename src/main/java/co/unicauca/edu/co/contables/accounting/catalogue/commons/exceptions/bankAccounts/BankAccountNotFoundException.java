package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.bankAccounts;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class BankAccountNotFoundException extends BaseBusinessException {
    
    public BankAccountNotFoundException() {
        super(BankAccountErrorCode.BANK_ACCOUNT_NOT_FOUND);
    }
    
    public BankAccountNotFoundException(String message) {
        super(BankAccountErrorCode.BANK_ACCOUNT_NOT_FOUND, message);
    }
}
