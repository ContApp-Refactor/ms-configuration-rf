package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.CityEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.City;

/**
 * @brief Mapeador de datos para ciudades
 */
@Mapper(componentModel = "spring")
public interface CityDataMapper {
    @Mapping(target = "department", ignore = true)
    CityEntity toEntity(City domain);

    @Mapping(target = "department", ignore = true)
    City toDomain(CityEntity entity);
}
