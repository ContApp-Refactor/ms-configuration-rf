package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.bankAccounts;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

/**
 * Excepción lanzada cuando se intenta modificar o eliminar una cuenta bancaria
 * que tiene movimientos registrados (usageCount > 0).
 */
public class BankAccountInUseException extends BaseBusinessException {

    /**
     * Constructor que permite especificar si es una operación de edición o eliminación
     * @param accountNumber número de la cuenta bancaria
     * @param isEditOperation true si es edición, false si es eliminación
     */
    public BankAccountInUseException(String accountNumber, boolean isEditOperation) {
        super(BankAccountErrorCode.BANK_ACCOUNT_IN_USE,
              isEditOperation ?
              String.format("No se puede editar la cuenta bancaria %s porque tiene movimientos contables", accountNumber) :
              String.format("No se puede eliminar la cuenta bancaria %s porque tiene movimientos contables", accountNumber));
    }
}
