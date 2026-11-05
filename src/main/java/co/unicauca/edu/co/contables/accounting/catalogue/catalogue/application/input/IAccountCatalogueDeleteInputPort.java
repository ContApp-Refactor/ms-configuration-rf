package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input;

/**
 * @brief Puerto de entrada para operaciones de eliminación de cuentas contables
 *
 * Define el contrato para eliminar cuentas del catálogo.
 */
public interface IAccountCatalogueDeleteInputPort {
    /**
     * @brief Elimina cuenta contable por ID y empresa
     * @param id ID de la cuenta a eliminar
     * @param idEnterprise ID de la empresa
     */
    void deleteById(Long id, String idEnterprise);
}
