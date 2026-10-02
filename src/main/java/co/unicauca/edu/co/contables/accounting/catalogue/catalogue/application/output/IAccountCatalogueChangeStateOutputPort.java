package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;

/**
 * @brief Puerto de salida para operaciones de cambio de estado de cuentas contables
 *
 * Define el contrato para modificar el estado activo/inactivo de cuentas
 * contables en el repositorio de datos.
 */
public interface IAccountCatalogueChangeStateOutputPort {

    /**
     * @brief Cambia estado de cuenta contable en base de datos
     * @param id ID de la cuenta
     * @param status nuevo estado (true=activo, false=inactivo)
     * @return cuenta con estado actualizado
     */
    AccountCatalogue changeState(Long id, Boolean status);
}
