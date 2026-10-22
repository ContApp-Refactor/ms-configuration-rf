package co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.DTO.TaxDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;

/**
 * @brief Puerto de salida para operaciones de actualización de impuestos
 *
 * Define el contrato para modificar impuestos existentes
 * en el repositorio de datos.
 */
public interface ITaxUpdateOutputPort {
    /**
     * @brief Actualiza un impuesto existente en la base de datos
     * @param taxDTO datos actualizados del impuesto
     * @param id ID del impuesto a actualizar
     * @return el impuesto actualizado
     */
    Tax update(TaxDTO taxDTO,long id);
}
