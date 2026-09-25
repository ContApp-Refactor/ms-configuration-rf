package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxLiability;

import java.util.List;

/**
 * Puerto de entrada para la gestión de responsabilidades fiscales.
 * Define las operaciones disponibles para la consulta y gestión de
 * obligaciones tributarias en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface ITaxLiabilityManagerPort {
    
    /**
     * Recupera todas las responsabilidades fiscales registradas en el sistema.
     *
     * @return List<TaxLiability> Lista de todas las responsabilidades fiscales disponibles
     * @see TaxLiability
     */
    List<TaxLiability> getAllTaxLiability();  
}
