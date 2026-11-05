package co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.input;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.DTO.TaxDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;

/**
 * @brief Puerto de entrada para operaciones de creación de impuestos
 *
 * Define el contrato para crear nuevos impuestos en el sistema.
 */
public interface ITaxCreateInputPort {
    /**
     * @brief Crea un nuevo impuesto
     * @param tax datos del impuesto a crear
     * @return impuesto creado
     */
    Tax createTax(TaxDTO tax);
}
