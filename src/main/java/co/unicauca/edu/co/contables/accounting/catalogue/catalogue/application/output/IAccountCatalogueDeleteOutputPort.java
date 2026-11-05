package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output;

/**
 * @brief Puerto de salida para operaciones de eliminación de cuentas contables
 *
 * Define el contrato para eliminar cuentas contables del repositorio
 * mediante operaciones de base de datos.
 */
public interface IAccountCatalogueDeleteOutputPort {
    /**
     * @brief Elimina cuenta contable por ID
     * @param id ID de la cuenta a eliminar
     */
    void deleteById(Long id);
}
