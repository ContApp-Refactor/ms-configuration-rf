package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.bankAccounts;

import co.unicauca.edu.co.contables.commons.exceptions.ErrorCodeDefinition;
import lombok.Getter;

/**
 * Códigos de error específicos del dominio de Cuentas Bancarias.
 */
@Getter
public enum BankAccountErrorCode implements ErrorCodeDefinition {
    BANK_ACCOUNT_NOT_FOUND("BANK_ACCOUNT_NOT_FOUND", "Cuenta bancaria no encontrada"),
    BANK_ACCOUNT_ALREADY_EXISTS("BANK_ACCOUNT_ALREADY_EXISTS", "La cuenta bancaria ya existe"),
    BANK_NOT_FOUND_FOR_ACCOUNT("BANK_NOT_FOUND_FOR_ACCOUNT", "El banco especificado no existe o no está disponible"),
    INVALID_ACCOUNT_NUMBER("INVALID_ACCOUNT_NUMBER", "El número de cuenta no es válido"),
    INVALID_ACCOUNTING_ACCOUNT_FOR_BANK_ACCOUNT("INVALID_ACCOUNTING_ACCOUNT_FOR_BANK_ACCOUNT", "La cuenta contable no es válida para la cuenta bancaria"),
    BANK_ACCOUNT_IN_USE("BANK_ACCOUNT_IN_USE", "No se puede modificar o eliminar una cuenta bancaria que tiene movimientos registrados");

    private final String code;
    private final String message;

    BankAccountErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
