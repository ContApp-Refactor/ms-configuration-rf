package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.PersonTypeEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.PersonType;

/**
 * @brief Mapeador de datos para el tipo de persona
 */
@Mapper(componentModel = "spring")
public interface PersonTypeDataMapper {
    PersonTypeEntity toEntity(PersonType domain);

    PersonType toDomain(PersonTypeEntity entity);
}
