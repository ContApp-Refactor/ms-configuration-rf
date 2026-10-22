package co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.DTO.TaxDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;

/**
 * @brief Puerto de entrada para operaciones de actualización de impuestos
 *
 * Define el contrato para actualizar la información de impuestos existentes.
 */
public interface ITaxUpdateInputPort {
    /**
     * @brief Actualiza un impuesto existente
     * @param taxDTO datos actualizados del impuesto
     * @param id ID del impuesto a actualizar
     * @return impuesto actualizado
     */
    Tax update(TaxDTO taxDTO,long id);
}
