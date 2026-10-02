package co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;

/**
 * @brief Puerto de salida para operaciones de cambio de estado de impuestos
 *
 * Define el contrato para modificar el estado activo/inactivo de impuestos
 * en el repositorio de datos.
 */
public interface ITaxChangeStateOutputPort {

    /**
     * @brief Cambia el estado de un impuesto en la base de datos
     * @param id el ID del impuesto
     * @param idEnterprise el ID de la empresa
     * @param status el nuevo estado
     * @return el impuesto actualizado
     */
    Tax changeState(Long id, String idEnterprise, Boolean status);
}
