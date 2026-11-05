package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.paymentMethods;

import co.unicauca.edu.co.contables.commons.exceptions.ErrorCodeDefinition;

import lombok.Getter;

/**
 * Códigos de error específicos del dominio de Métodos de Pago.
 */
@Getter
public enum PaymentMethodsErrorCode implements ErrorCodeDefinition {
    PAYMENT_METHOD_NOT_FOUND("PAYMENT_METHOD_NOT_FOUND", "Método de pago no encontrado"),
    PAYMENT_METHOD_ALREADY_EXISTS("PAYMENT_METHOD_ALREADY_EXISTS", "El método de pago ya existe"),
    INVALID_ACCOUNTING_ACCOUNT("INVALID_ACCOUNTING_ACCOUNT", "La cuenta contable no es válida"),
    ACCOUNTING_ACCOUNT_IMMUTABLE("ACCOUNTING_ACCOUNT_IMMUTABLE", "La cuenta contable no puede ser modificada una vez creado el método de pago"),
    PAYMENT_METHOD_IN_USE("PAYMENT_METHOD_IN_USE", "No se puede modificar o eliminar un método de pago que tiene movimientos contables registrados");

    private final String code;
    private final String message;

    PaymentMethodsErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
