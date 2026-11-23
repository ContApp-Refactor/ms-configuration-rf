package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.LocationEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Location;

/**
 * @brief Mapeador de datos para la ubicación geográfica
 */
@Mapper(componentModel = "spring", uses = { CityDataMapper.class, CountryDataMapper.class, DepartmentDataMapper.class })
public interface LocationDataMapper {
    LocationEntity toEntity(Location domain);

    Location toDomain(LocationEntity entity);
}
