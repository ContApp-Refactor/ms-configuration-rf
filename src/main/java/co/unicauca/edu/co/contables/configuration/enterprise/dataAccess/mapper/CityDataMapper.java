package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.CityEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.City;

/**
 * @brief Mapeador de datos para ciudades
 */
@Mapper(componentModel = "spring", uses = DepartmentDataMapper.class)
public interface CityDataMapper {
    CityEntity toEntity(City domain);

    City toDomain(CityEntity entity);
}
