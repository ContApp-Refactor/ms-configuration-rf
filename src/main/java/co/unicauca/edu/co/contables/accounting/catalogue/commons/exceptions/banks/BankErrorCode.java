package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.banks;

import co.unicauca.edu.co.contables.commons.exceptions.ErrorCodeDefinition;
import lombok.Getter;

/**
 * Códigos de error específicos del dominio de Bancos.
 */
@Getter
public enum BankErrorCode implements ErrorCodeDefinition {
    BANK_NOT_FOUND("BANK_NOT_FOUND", "Banco no encontrado"),
    BANK_ALREADY_EXISTS("BANK_ALREADY_EXISTS", "El banco ya existe"),
    INVALID_BANK_CODE("INVALID_BANK_CODE", "El código del banco no es válido"),
    BANK_HAS_ASSOCIATED_ACCOUNTS("BANK_HAS_ASSOCIATED_ACCOUNTS", "El banco tiene cuentas bancarias asociadas"),
    BANK_IN_USE("BANK_IN_USE", "No se puede modificar o eliminar un banco que tiene cuentas con movimientos registrados");

    private final String code;
    private final String message;

    BankErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
