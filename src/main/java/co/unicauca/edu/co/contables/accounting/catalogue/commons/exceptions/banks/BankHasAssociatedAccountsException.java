package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.banks;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

public class BankHasAssociatedAccountsException extends BaseBusinessException {

    public BankHasAssociatedAccountsException(String bankName) {
        super(BankErrorCode.BANK_HAS_ASSOCIATED_ACCOUNTS,
              String.format("No se puede eliminar '%s' porque tiene una o más cuentas bancarias", bankName));
    }
}
