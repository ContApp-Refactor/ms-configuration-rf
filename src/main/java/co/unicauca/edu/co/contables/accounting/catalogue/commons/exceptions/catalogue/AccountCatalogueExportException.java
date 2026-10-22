package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

/**
 * Excepción específica para errores durante la exportación de catálogo de cuentas.
 * Extiende BaseBusinessException para mantener consistencia con el manejo de errores del dominio.
 */
public class AccountCatalogueExportException extends BaseBusinessException {

    /**
     * Constructor con mensaje y código de error.
     *
     * @param errorCode código específico del error
     * @param message mensaje descriptivo del error
     */
    public AccountCatalogueExportException(AccountCatalogueErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Constructor con mensaje, código de error y causa.
     *
     * @param errorCode código específico del error
     * @param message mensaje descriptivo del error
     * @param cause causa raíz del error
     */
    public AccountCatalogueExportException(AccountCatalogueErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    /**
     * Constructor de conveniencia para exportación sin datos.
     *
     * @param status filtro de estado aplicado (true=activos, false=inactivos, null=todos)
     * @return nueva instancia de AccountCatalogueExportException
     */
    public static AccountCatalogueExportException forNoData(Boolean status) {
        String message;
        if (status == null) {
            message = "No hay cuentas para exportar";
        } else if (status) {
            message = "No hay cuentas activas para exportar";
        } else {
            message = "No hay cuentas inactivas para exportar";
        }

        return new AccountCatalogueExportException(
            AccountCatalogueErrorCode.ACCOUNT_EXPORT_NO_DATA,
            message
        );
    }
}
