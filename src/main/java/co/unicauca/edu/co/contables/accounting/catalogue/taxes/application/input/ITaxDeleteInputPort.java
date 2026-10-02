package co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input;

/**
 * @brief Puerto de entrada para operaciones de eliminación de impuestos
 *
 * Define el contrato para eliminar impuestos.
 */
public interface ITaxDeleteInputPort {

    /**
     * @brief Elimina un impuesto por su ID y empresa
     * @param id el ID del impuesto
     * @param idEnterprise el ID de la empresa
     * @return true si se eliminó con éxito, false de lo contrario
     */
    boolean deleteByCode(long id, String idEnterprise);
}
