package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.bankAccounts;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class BankNotFoundForAccountException extends BaseBusinessException {
    
    public BankNotFoundForAccountException() {
        super(BankAccountErrorCode.BANK_NOT_FOUND_FOR_ACCOUNT);
    }
    
    public BankNotFoundForAccountException(String message) {
        super(BankAccountErrorCode.BANK_NOT_FOUND_FOR_ACCOUNT, message);
    }
}
