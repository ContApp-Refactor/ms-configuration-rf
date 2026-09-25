package co.unicauca.edu.co.contables.configuration.enterprise.application.ports.services;

import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.input.ITaxLiabilityManagerPort;
import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output.ITaxLiabilityOutputPort;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxLiability;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio que implementa las operaciones de gestión de responsabilidades fiscales.
 * Gestiona la lógica de negocio para la consulta y recuperación de información
 * sobre las responsabilidades tributarias en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Service
@AllArgsConstructor
public class TaxLiabilityService implements ITaxLiabilityManagerPort {

    /**
     * Puerto de salida para operaciones de gestión de responsabilidades fiscales.
     */
    private final ITaxLiabilityOutputPort taxLiabilityOutputPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TaxLiability> getAllTaxLiability() {
        return taxLiabilityOutputPort.getAllTaxLiability();     
    } 
}
