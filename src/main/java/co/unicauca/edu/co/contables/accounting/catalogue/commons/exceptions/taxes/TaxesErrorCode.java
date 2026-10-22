package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.taxes;

import co.unicauca.edu.co.contables.commons.exceptions.ErrorCodeDefinition;

import lombok.Getter;

/**
 * Códigos de error específicos del dominio de impuestos.
 */
@Getter
public enum TaxesErrorCode implements ErrorCodeDefinition {

    TAX_NOT_FOUND("TAX_NOT_FOUND", "Impuesto no encontrado"),
    TAX_ALREADY_EXISTS("TAX_ALREADY_EXISTS", "El impuesto ya existe"),
    INVALID_TAX_CODE("INVALID_TAX_CODE", "Código de impuesto inválido"),
    TAX_DESCRIPTION_ALREADY_EXISTS("TAX_DESCRIPTION_ALREADY_EXISTS", "La descripción del impuesto ya existe"),
    INVALID_DEPOSIT_ACCOUNT("INVALID_DEPOSIT_ACCOUNT", "Cuenta inválida"),
    INVALID_REFUND_ACCOUNT("INVALID_REFUND_ACCOUNT", "Cuenta de devolución inválida"),
    INVALID_ACCOUNT_DIGITS("INVALID_ACCOUNT_DIGITS", "Las cuentas deben tener exactamente 8 dígitos"),
    DUPLICATE_TAX_ACCOUNTS("DUPLICATE_TAX_ACCOUNTS", "Las cuentas de impuesto de venta e impuesto de compra deben ser diferentes"),
    TAX_IN_USE("TAX_IN_USE", "No se puede modificar o eliminar un impuesto que tiene movimientos registrados");

    private final String code;
    private final String message;

    TaxesErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
