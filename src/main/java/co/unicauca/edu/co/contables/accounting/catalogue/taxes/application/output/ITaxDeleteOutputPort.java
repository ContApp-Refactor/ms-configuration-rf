package co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output;

/**
 * @brief Puerto de salida para operaciones de eliminación de impuestos
 *
 * Define el contrato para realizar eliminación
 * de impuestos en el repositorio de datos.
 */
public interface ITaxDeleteOutputPort {

    /**
     * @brief Realiza la eliminación de un impuesto por su ID y empresa
     * @param id el ID del impuesto
     * @param idEnterprise el ID de la empresa
     * @return true si se eliminó con éxito, false de lo contrario
     */
    boolean deleteByCode(long id, String idEnterprise);
}
