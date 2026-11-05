package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.paymentMethods;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

/**
 * Excepción lanzada cuando se intenta modificar o eliminar un método de pago
 * que tiene movimientos contables registrados (usageCount > 0).
 */
public class PaymentMethodInUseException extends BaseBusinessException {

    

    /**
     * Constructor que permite especificar si es una operación de edición o eliminación
     * @param paymentMethodName nombre del método de pago
     * @param isEditOperation true si es edición, false si es eliminación
     */
    public PaymentMethodInUseException(String paymentMethodName, boolean isEditOperation) {
        super(PaymentMethodsErrorCode.PAYMENT_METHOD_IN_USE,
              isEditOperation ?
              String.format("No se puede editar el método de pago %s porque tiene movimientos contables", paymentMethodName) :
              String.format("No se puede eliminar el método de pago %s porque tiene movimientos contables", paymentMethodName));
    }
}
