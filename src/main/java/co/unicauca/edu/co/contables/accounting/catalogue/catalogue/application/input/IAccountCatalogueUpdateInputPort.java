package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;

/**
 * @brief Puerto de entrada para operaciones de actualización de cuentas contables
 *
 * Define el contrato para modificar cuentas existentes del catálogo
 * con validaciones de jerarquía y unicidad.
 */
public interface IAccountCatalogueUpdateInputPort {
    /**
     * @brief Actualiza cuenta contable existente
     * @param id ID de la cuenta a actualizar
     * @param accountCatalogue datos actualizados de la cuenta
     * @return cuenta actualizada
     */
    AccountCatalogue updateAccountCatalogue(long id,AccountCatalogue accountCatalogue);
}
