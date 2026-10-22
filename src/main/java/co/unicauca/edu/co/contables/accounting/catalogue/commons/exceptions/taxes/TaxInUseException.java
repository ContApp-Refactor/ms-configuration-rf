package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

/**
 * Excepción lanzada cuando se intenta modificar o eliminar un impuesto
 * que tiene movimientos registrados (usageCount > 0).
 */
public class TaxInUseException extends BaseBusinessException {

    /**
     * Constructor que permite especificar si es una operación de edición o eliminación
     * @param taxCode código del impuesto
     * @param isEditOperation true si es edición, false si es eliminación
     */
    public TaxInUseException(String taxCode, boolean isEditOperation) {
        super(TaxesErrorCode.TAX_IN_USE,
              isEditOperation ?
              String.format("No se puede editar el impuesto %s porque tiene movimientos contables", taxCode) :
              String.format("No se puede eliminar el impuesto %s porque tiene movimientos contables", taxCode));
    }
}
