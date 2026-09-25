package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxLiability;

import java.util.List;

/**
 * Puerto de salida para la consulta de responsabilidades fiscales.
 * Define las operaciones necesarias para recuperar información sobre
 * responsabilidades fiscales desde el sistema de almacenamiento.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface ITaxLiabilityOutputPort {
    
    /**
     * Recupera todas las responsabilidades fiscales registradas en el sistema.
     *
     * @return List<TaxLiability> Lista de todas las responsabilidades fiscales disponibles
     * @see TaxLiability
     */
    List<TaxLiability> getAllTaxLiability();
}
