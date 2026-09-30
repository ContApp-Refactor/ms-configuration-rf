package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;

/**
 * @brief Puerto de entrada para operaciones de creación de cuentas contables
 *
 * Define el contrato para crear nuevas cuentas en el catálogo
 * con validaciones de negocio y jerarquía.
 */
public interface IAccountCatalogueCreateInputPort {
    /**
     * @brief Crea nueva cuenta contable
     * @param accountCatalogue datos de la cuenta a crear
     * @return cuenta creada con ID generado
     */
    AccountCatalogue createAccountCatalogue(AccountCatalogue accountCatalogue);
}
