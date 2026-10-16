package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter;

import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output.IEnterpriseTypeOutputPort;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.EnterpriseType;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IEnterpriseTypeMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IEnterpriseTypeRepository;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador para la gestión de tipos de empresa usando JPA.
 * Implementa la interfaz IEnterpriseTypeOutputPort.
 */
@Component
@Data
public class EnterpriseTypeJpaAdapter implements IEnterpriseTypeOutputPort {

    private final IEnterpriseTypeRepository enterpriseTypeRepository;
    private final IEnterpriseTypeMapper enterpriseTypeMapper;

    /**
     * Obtiene todos los tipos de empresa.
     *
     * @return una lista de todos los tipos de empresa en el modelo de dominio
     */
    @Override
    public List<EnterpriseType> getAllEnterpriseTypes() {
        return enterpriseTypeMapper.toModel(enterpriseTypeRepository.findAll());  
    }  
}