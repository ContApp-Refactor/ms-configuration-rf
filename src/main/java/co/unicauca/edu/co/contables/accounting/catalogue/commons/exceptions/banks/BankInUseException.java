package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.banks;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

/**
 * Excepción lanzada cuando se intenta modificar o eliminar un banco
 * que tiene cuentas bancarias con movimientos registrados.
 */
public class BankInUseException extends BaseBusinessException {

    /**
     * Constructor que permite especificar si es una operación de edición o eliminación
     * @param bankName nombre del banco
     * @param isEditOperation true si es edición, false si es eliminación
     */
    public BankInUseException(String bankName, boolean isEditOperation) {
        super(BankErrorCode.BANK_IN_USE,
              isEditOperation ?
              String.format("No se puede editar el banco %s porque tiene cuentas con movimientos contables", bankName) :
              String.format("No se puede eliminar el banco %s porque tiene cuentas con movimientos contables", bankName));
    }
}
