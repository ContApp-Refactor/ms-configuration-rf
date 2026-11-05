package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.bankAccounts;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class InvalidAccountNumberException extends BaseBusinessException {
    
    public InvalidAccountNumberException() {
        super(BankAccountErrorCode.INVALID_ACCOUNT_NUMBER);
    }
    
    public InvalidAccountNumberException(String message) {
        super(BankAccountErrorCode.INVALID_ACCOUNT_NUMBER, message);
    }
}
