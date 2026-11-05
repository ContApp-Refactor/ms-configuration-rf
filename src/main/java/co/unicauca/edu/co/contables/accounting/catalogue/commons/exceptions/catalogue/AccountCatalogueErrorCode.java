package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue;

import co.unicauca.edu.co.contables.commons.exceptions.ErrorCodeDefinition;
import lombok.Getter;

/**
 * Códigos de error específicos del dominio de Catálogo de Cuentas.
 */
@Getter
public enum AccountCatalogueErrorCode implements ErrorCodeDefinition {

    ACCOUNT_NOT_FOUND("ACCOUNT_NOT_FOUND", "Cuenta no encontrada"),
    ACCOUNT_INACTIVE("ACCOUNT_INACTIVE", "La cuenta está inactiva"),
    ACCOUNT_ALREADY_EXISTS("ACCOUNT_ALREADY_EXISTS", "La cuenta ya existe"),
    ACCOUNT_DESCRIPTION_ALREADY_EXISTS("ACCOUNT_DESCRIPTION_ALREADY_EXISTS", "Ya existe una cuenta con esta descripción"),
    INVALID_ACCOUNT_CODE("INVALID_ACCOUNT_CODE", "Código de cuenta inválido"),
    ACCOUNT_HAS_CHILDREN("ACCOUNT_HAS_CHILDREN", "La cuenta tiene subcuentas asociadas"),
    ACCOUNT_IN_USE("ACCOUNT_IN_USE", "No se puede modificar o eliminar una cuenta contable que tiene movimientos registrados"),
    PARENT_ACCOUNT_NOT_FOUND("PARENT_ACCOUNT_NOT_FOUND", "Cuenta padre no encontrada"),
    ACCOUNT_ASSOCIATED_WITH_TAX("ACCOUNT_ASSOCIATED_WITH_TAX", "La cuenta está asociada a uno o más impuestos"),
    ACCOUNT_ASSOCIATED_WITH_BANK_ACCOUNT("ACCOUNT_ASSOCIATED_WITH_BANK_ACCOUNT", "La cuenta está asociada a una o más cuentas bancarias"),
    ACCOUNT_ASSOCIATED_WITH_PAYMENT_METHOD("ACCOUNT_ASSOCIATED_WITH_PAYMENT_METHOD", "La cuenta está asociada a uno o más métodos de pago"),
    INVALID_ACCOUNT_CODE_LENGTH("INVALID_ACCOUNT_CODE_LENGTH", "El código de cuenta debe tener exactamente 1, 2, 4, 6 u 8 dígitos"),
    EXCEL_VALIDATION_ERROR("EXCEL_VALIDATION_ERROR", "Error en la validación de datos del archivo Excel"),
    EXCEL_CODE_VALIDATION_ERROR("EXCEL_CODE_VALIDATION_ERROR", "Error aplicando validación de código en columna"),
    EXCEL_DROPDOWN_VALIDATION_ERROR("EXCEL_DROPDOWN_VALIDATION_ERROR", "Error aplicando validación de lista desplegable en columna"),
    EXCEL_CONDITIONAL_VALIDATION_ERROR("EXCEL_CONDITIONAL_VALIDATION_ERROR", "Error aplicando validación condicional en columna"),
    ACCOUNT_IMPORT_ERROR("ACCOUNT_IMPORT_ERROR", "Error durante la importación de catálogo de cuentas"),
    ACCOUNT_IMPORT_NO_DATA("ACCOUNT_IMPORT_NO_DATA", "No hay datos válidos para importar en el archivo"),
    ACCOUNT_EXPORT_NO_DATA("ACCOUNT_EXPORT_NO_DATA", "No hay cuentas contables para exportar"),
    ACCOUNT_HIERARCHY_ERROR("ACCOUNT_HIERARCHY_ERROR", "Error en la jerarquía de cuentas"),
    FILE_SIZE_EXCEEDED("FILE_SIZE_EXCEEDED", "El archivo excede el tamaño máximo permitido");

    private final String code;
    private final String message;

    AccountCatalogueErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
