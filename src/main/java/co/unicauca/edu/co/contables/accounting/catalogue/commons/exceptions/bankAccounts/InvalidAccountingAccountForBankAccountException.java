package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.bankAccounts;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class InvalidAccountingAccountForBankAccountException extends BaseBusinessException {
    
    public InvalidAccountingAccountForBankAccountException() {
        super(BankAccountErrorCode.INVALID_ACCOUNTING_ACCOUNT_FOR_BANK_ACCOUNT);
    }
    
    public InvalidAccountingAccountForBankAccountException(String message) {
        super(BankAccountErrorCode.INVALID_ACCOUNTING_ACCOUNT_FOR_BANK_ACCOUNT, message);
    }
}
