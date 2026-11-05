package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

/**
 * Excepción lanzada cuando se intenta modificar o eliminar una cuenta contable
 * que tiene movimientos registrados (usageCount > 0).
 */
public class AccountCatalogueInUseException extends BaseBusinessException {
   
    /**
     * Constructor que permite especificar si es una operación de edición o eliminación
     * @param accountCode código de la cuenta contable
     * @param isEditOperation true si es edición, false si es eliminación
     */
    public AccountCatalogueInUseException(String accountCode, boolean isEditOperation) {
        super(AccountCatalogueErrorCode.ACCOUNT_IN_USE,
              isEditOperation ?
              String.format("No se puede editar la cuenta contable %s porque tiene movimientos registrados", accountCode) :
              String.format("No se puede eliminar la cuenta contable %s porque tiene movimientos registrados", accountCode));
    }
}
