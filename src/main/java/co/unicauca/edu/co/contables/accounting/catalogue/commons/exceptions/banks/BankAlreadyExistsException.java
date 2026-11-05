package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.banks;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class BankAlreadyExistsException extends BaseBusinessException {

    public BankAlreadyExistsException(String field, String value, String enterpriseId) {
        super(BankErrorCode.BANK_ALREADY_EXISTS,
              String.format("Ya existe un banco con %s '%s'", field, value));
    }
}