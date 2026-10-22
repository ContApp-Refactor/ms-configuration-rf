package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.EnterpriseTypeEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.EnterpriseType;

import java.util.List;

/**
 * @brief Mapeador de datos para el tipo de empresa
 */
@Mapper(componentModel = "spring")
public interface EnterpriseTypeDataMapper {
    EnterpriseTypeEntity toEntity(EnterpriseType domain);

    EnterpriseType toDomain(EnterpriseTypeEntity entity);

    List<EnterpriseType> toDomainList(List<EnterpriseTypeEntity> entities);
}
