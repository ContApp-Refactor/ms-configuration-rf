package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

/**
 * Excepción lanzada cuando se detectan problemas en la jerarquía de cuentas contables.
 * Ejemplos: cuentas hijas huérfanas, códigos padre inexistentes.
 */
public class AccountCatalogueHierarchyException extends BaseBusinessException {

    public AccountCatalogueHierarchyException(String message) {
        super(AccountCatalogueErrorCode.ACCOUNT_HIERARCHY_ERROR, message);
    }

    public AccountCatalogueHierarchyException(String message, Throwable cause) {
        super(AccountCatalogueErrorCode.ACCOUNT_HIERARCHY_ERROR, message, cause);
    }

    /**
     * Crea excepción para cuenta huérfana (sin padre).
     */
    public static AccountCatalogueHierarchyException forOrphanAccount(String code, String parentCode) {
        return new AccountCatalogueHierarchyException(
            String.format("La cuenta '%s' requiere una cuenta padre '%s' que no existe", code, parentCode)
        );
    }

    /**
     * Crea excepción para múltiples cuentas huérfanas.
     */
    public static AccountCatalogueHierarchyException forMultipleOrphans(int count) {
        return new AccountCatalogueHierarchyException(
            String.format("Se encontraron %d cuentas sin padre válido. Todas las cuentas deben tener su cuenta padre creada primero.", count)
        );
    }
}

