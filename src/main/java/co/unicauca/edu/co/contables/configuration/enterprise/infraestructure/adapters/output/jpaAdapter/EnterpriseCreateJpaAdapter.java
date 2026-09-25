package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter;

import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output.IEnterpriseCreateOutputPort;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IEnterpriseCreateMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IEnterpriseRepository;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * Adaptador para la creación de entidades Enterprise usando JPA.
 * Implementa la interfaz IEnterpriseCreateOutputPort.
 */
@Component
@Data
public class EnterpriseCreateJpaAdapter implements IEnterpriseCreateOutputPort {

    // Repositorio para realizar operaciones CRUD sobre entidades Enterprise
    private final IEnterpriseRepository enterpriseRepository;
    
    // Mapper para convertir entre modelos de dominio y entidades JPA
    private final IEnterpriseCreateMapper createMapper;

    /**
     * Crea una nueva entidad Enterprise.
     *
     * @param enterprise el modelo de dominio de la Empresa a crear
     * @return el modelo de dominio de la Empresa creada, o null si la conversión a entidad falla
     */
    @Override
    public Enterprise create(Enterprise enterprise) {
        // Convierte el modelo de dominio a una entidad JPA
        EnterpriseEntity enterpriseEntity = createMapper.toEntity(enterprise);
        
        if (enterpriseEntity == null) {
            return null;          
        }
        
        // Guarda la entidad en el repositorio (base de datos)
        enterpriseEntity = enterpriseRepository.save(enterpriseEntity);
        
        // Convierte la entidad guardada de vuelta al modelo de dominio y la devuelve
        return createMapper.toModel(enterpriseEntity);
    } 
}
