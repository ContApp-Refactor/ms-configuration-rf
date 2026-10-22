package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

/**
 * Excepción específica para errores durante la importación de catálogo de cuentas.
 * Extiende BaseBusinessException para mantener consistencia con el manejo de errores del dominio.
 */
public class AccountCatalogueImportException extends BaseBusinessException {

    /**
     * Constructor con mensaje y código de error.
     * 
     * @param errorCode código específico del error
     * @param message mensaje descriptivo del error
     */
    public AccountCatalogueImportException(AccountCatalogueErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Constructor con mensaje, código de error y causa.
     * 
     * @param errorCode código específico del error
     * @param message mensaje descriptivo del error
     * @param cause causa raíz del error
     */
    public AccountCatalogueImportException(AccountCatalogueErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    /**
     * Constructor de conveniencia para errores de archivo Excel inválido.
     * 
     * @param fileName nombre del archivo que causó el error
     * @param details detalles específicos del error
     * @return nueva instancia de AccountCatalogueImportException
     */
    public static AccountCatalogueImportException forInvalidExcelFile(String fileName, String details) {
        return new AccountCatalogueImportException(
            AccountCatalogueErrorCode.EXCEL_VALIDATION_ERROR,
            String.format("El archivo Excel '%s' tiene formato inválido: %s", fileName, details)
        );
    }

    /**
     * Constructor de conveniencia para errores de procesamiento en lotes.
     * 
     * @param batchNumber número del lote que falló
     * @param details detalles específicos del error
     * @return nueva instancia de AccountCatalogueImportException
     */
    public static AccountCatalogueImportException forBatchProcessingError(int batchNumber, String details) {
        return new AccountCatalogueImportException(
            AccountCatalogueErrorCode.ACCOUNT_IMPORT_ERROR,
            String.format("Error procesando lote #%d: %s", batchNumber, details)
        );
    }

    /**
     * Constructor de conveniencia para errores de archivo vacío.
     * 
     * @param fileName nombre del archivo vacío
     * @return nueva instancia de AccountCatalogueImportException
     */
    public static AccountCatalogueImportException forEmptyFile(String fileName) {
        return new AccountCatalogueImportException(
            AccountCatalogueErrorCode.ACCOUNT_IMPORT_NO_DATA,
            String.format("El archivo '%s' está vacío o no contiene datos válidos para importar", fileName)
        );
    }

    /**
     * Constructor de conveniencia para errores de jerarquía.
     * 
     * @param details detalles específicos del error de jerarquía
     * @return nueva instancia de AccountCatalogueImportException
     */
    public static AccountCatalogueImportException forHierarchyError(String details) {
        return new AccountCatalogueImportException(
            AccountCatalogueErrorCode.ACCOUNT_HIERARCHY_ERROR,
            String.format("Error en la jerarquía de cuentas: %s", details)
        );
    }
}

