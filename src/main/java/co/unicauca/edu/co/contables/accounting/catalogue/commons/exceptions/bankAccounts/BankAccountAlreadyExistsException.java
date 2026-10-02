package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.bankAccounts;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class BankAccountAlreadyExistsException extends BaseBusinessException {

    public BankAccountAlreadyExistsException(String field, String value, String enterpriseId) {
        super(BankAccountErrorCode.BANK_ACCOUNT_ALREADY_EXISTS,
              String.format("Ya existe una cuenta bancaria con %s '%s'", field, value));
    }
}
