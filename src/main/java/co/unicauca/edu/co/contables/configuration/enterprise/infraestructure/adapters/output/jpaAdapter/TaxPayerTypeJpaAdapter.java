package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter;

import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output.ITaxPayerTypeOutputPort;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.TaxPayerType;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.ITaxPayerTypeMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository.ITaxPayerTypeRepository;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador para la gestión de tipo de contribuyentes usando JPA.
 * Implementa la interfaz ITaxPayerTypeOutputPort.
 */
@Component
@Data
public class TaxPayerTypeJpaAdapter implements ITaxPayerTypeOutputPort{

    private final ITaxPayerTypeRepository taxPayerTypeRepository;
    private final ITaxPayerTypeMapper taxPayerTypeMapper;

    /**
     * Obtiene todas los tipos de contribuyente.
     *
     * @return una lista de todas los tipos de contribuyente en el modelo de dominio
     */

    @Override
    public List<TaxPayerType> getAllTaxPayerTypes() {
        return taxPayerTypeMapper.toModel(taxPayerTypeRepository.findAll());
    }  
}