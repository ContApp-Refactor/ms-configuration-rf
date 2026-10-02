package co.unicauca.edu.co.contables.accounting.catalogue.taxes.application.output;

import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.DTO.TaxDTO;
import co.unicauca.edu.co.contables.accounting.catalogue.taxes.domain.models.Tax;

/**
 * @brief Puerto de salida para operaciones de creación de impuestos
 *
 * Define el contrato para persistir nuevos impuestos
 * en el repositorio de datos.
 */
public interface ITaxCreateOutputPort {
    /**
     * @brief Crea un nuevo impuesto en la base de datos
     * @param tax datos del impuesto a crear
     * @return el impuesto creado con ID generado
     */
    Tax createTax(TaxDTO tax);
}
