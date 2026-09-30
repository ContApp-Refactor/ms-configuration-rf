package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;

import java.util.List;

/**
 * @brief Puerto de salida para operaciones de creación de cuentas contables
 *
 * Define el contrato para persistir nuevas cuentas contables
 * en el repositorio de datos.
 */
public interface IAccountCatalogueCreateOutputPort {
    /**
     * @brief Crea nueva cuenta contable en base de datos
     * @param accountCatalogue cuenta a persistir
     * @return cuenta creada con ID generado
     */
    AccountCatalogue createAccountCatalogue(AccountCatalogue accountCatalogue);

    /**
     * @brief Crea múltiples cuentas contables en batch para mejor rendimiento
     * @details Usa saveAll de JPA para reducir round-trips a la BD
     * @param accountCatalogues lista de cuentas a persistir
     * @return lista de cuentas creadas con IDs generados
     */
    List<AccountCatalogue> createAllAccountCatalogues(List<AccountCatalogue> accountCatalogues);
}
