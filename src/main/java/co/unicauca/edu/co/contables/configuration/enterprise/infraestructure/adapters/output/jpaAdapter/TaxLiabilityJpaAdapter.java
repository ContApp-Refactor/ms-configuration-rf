package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter;

import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output.ITaxLiabilityOutputPort;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxLiability;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.ITaxLiabilityMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository.ITaxLiabilityRepository;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador para la gestión de responsabilidades tributarias usando JPA.
 * Implementa la interfaz ITaxLiabilityOutputPort.
 */
@Component
@Data
public class TaxLiabilityJpaAdapter implements ITaxLiabilityOutputPort{

    private final ITaxLiabilityRepository taxLiabilityRepository;
    private final ITaxLiabilityMapper taxLiabilityMapper;

    /**
     * Obtiene todas las responsabilidades tributarias.
     *
     * @return una lista de todas las responsabilidades tributarias en el modelo de dominio
     */

    @Override
    public List<TaxLiability> getAllTaxLiability() {
        return taxLiabilityMapper.toModel(taxLiabilityRepository.findAll());
    }  
}
