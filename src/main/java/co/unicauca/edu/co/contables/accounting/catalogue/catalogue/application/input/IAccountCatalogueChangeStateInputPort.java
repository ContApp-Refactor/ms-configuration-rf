package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;

/**
 * @brief Puerto de entrada para operaciones de cambio de estado de cuentas contables
 *
 * Define el contrato para activar o desactivar cuentas del catálogo
 * con validaciones de existencia y permisos.
 */
public interface IAccountCatalogueChangeStateInputPort {

    /**
     * @brief Cambia estado de cuenta contable
     * @param id ID de la cuenta
     * @param idEnterprise ID de la empresa
     * @param status nuevo estado (true=activo, false=inactivo)
     * @return cuenta con estado actualizado
     */
    AccountCatalogue changeState(Long id, String idEnterprise, Boolean status);
}
