package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.EnterpriseType;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseTypeEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Interfaz de mapeo para convertir entre entidades de tipo de empresa y su representación en el dominio.
 * Utiliza MapStruct para generar automáticamente las implementaciones de mapeo.
 */
@Mapper
public interface IEnterpriseTypeMapper {
     
    /**
     * Convierte un objeto de dominio EnterpriseType a su entidad correspondiente.
     *
     * @param enterpriseType el objeto de dominio a convertir
     * @return la entidad EnterpriseTypeEntity correspondiente
     */
    EnterpriseTypeEntity toEntity(EnterpriseType enterpriseType);

    /**
     * Convierte una lista de entidades EnterpriseTypeEntity a una lista de objetos de dominio.
     *
     * @param enterpriseTypeEntities la lista de entidades a convertir
     * @return la lista de objetos de dominio EnterpriseType correspondiente
     */
    List<EnterpriseType> toModel(List<EnterpriseTypeEntity> enterpriseTypeEntities);
}