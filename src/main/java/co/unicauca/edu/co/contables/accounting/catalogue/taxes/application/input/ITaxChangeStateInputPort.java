package co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;

/**
 * @brief Puerto de entrada para operaciones de cambio de estado de impuestos
 *
 * Define el contrato para activar o desactivar impuestos.
 */
public interface ITaxChangeStateInputPort {

    /**
     * @brief Cambia el estado (activo/inactivo) de un impuesto
     * @param id el ID del impuesto
     * @param idEnterprise el ID de la empresa
     * @param status el nuevo estado (true = activo, false = inactivo)
     * @return el impuesto actualizado
     */
    Tax changeState(Long id, String idEnterprise, Boolean status);
}
