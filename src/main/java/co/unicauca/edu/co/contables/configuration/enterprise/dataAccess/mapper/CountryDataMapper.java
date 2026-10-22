package co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.mapper;

import org.mapstruct.Mapper;

import co.unicauca.edu.co.contables.configuration.enterprise.dataAccess.entity.CountryEntity;
import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Country;

import java.util.List;

/**
 * @brief Mapeador de datos para países
 */
@Mapper(componentModel = "spring")
public interface CountryDataMapper {
    CountryEntity toEntity(Country domain);

    Country toDomain(CountryEntity entity);

    List<Country> toDomainList(List<CountryEntity> entities);
}
