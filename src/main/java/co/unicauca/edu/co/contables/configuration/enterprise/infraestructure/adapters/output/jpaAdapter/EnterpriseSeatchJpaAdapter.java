package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter;

import co.unicauca.edu.co.contables.configuration.enterprise.application.ports.output.IEnterpriseSearchOutputPort;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.dto.EnterpriseInfoDto;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Enterprise;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IEnterpriseSearchMapper;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.projection.IEnterpriseInfoProjection;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IEnterpriseRepository;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Adaptador para la búsqueda de entidades Enterprise usando JPA.
 * Implementa la interfaz IEnterpriseSearchOutputPort.
 */
@Component
@Data
public class EnterpriseSeatchJpaAdapter implements IEnterpriseSearchOutputPort{
    
    private final IEnterpriseRepository enterpriseRepository;
    private final IEnterpriseSearchMapper enterpriseMapper;
    
    /**
     * Obtiene todas las empresas.
     *
     * @return una lista de DTOs con la información de todas las empresas
     */
    @Override
    public List<EnterpriseInfoDto> getAllEnterprises() {
        List<IEnterpriseInfoProjection> enterpriseInfo = enterpriseRepository.findEnterpriseInfo();
        return enterpriseMapper.toEnterpriseInfoDtoList(enterpriseInfo);
    }

    /**
     * Obtiene todas las empresas inactivas.
     *
     * @return una lista de DTOs con la información de todas las empresas inactivas
     */
    @Override
    public List<EnterpriseInfoDto> getAllEnterprisesInactive() {
        List<IEnterpriseInfoProjection> enterpriseInfo = enterpriseRepository.findEnterpriseInfoInactive();
        return enterpriseMapper.toEnterpriseInfoDtoList(enterpriseInfo);
    }

    /**
     * Obtiene una empresa por su ID.
     *
     * @param id el UUID de la empresa a buscar
     * @return el modelo de dominio de la empresa, o null si no se encuentra
     */
    @Override
    public Enterprise getEnterpriseById(UUID id) {
        EnterpriseEntity enterpriseEntity = enterpriseRepository.findById(id).orElse(null);
        return enterpriseMapper.toEnterprise(enterpriseEntity);
    }
    

}
