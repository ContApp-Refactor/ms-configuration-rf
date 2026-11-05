package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.banks;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class BankNotFoundException extends BaseBusinessException {
    
    public BankNotFoundException() {
        super(BankErrorCode.BANK_NOT_FOUND);
    }
    
    public BankNotFoundException(String message) {
        super(BankErrorCode.BANK_NOT_FOUND, message);
    }
}
