package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.banks;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class InvalidBankCodeException extends BaseBusinessException {
    
    public InvalidBankCodeException() {
        super(BankErrorCode.INVALID_BANK_CODE);
    }
    
    public InvalidBankCodeException(String message) {
        super(BankErrorCode.INVALID_BANK_CODE, message);
    }
}
