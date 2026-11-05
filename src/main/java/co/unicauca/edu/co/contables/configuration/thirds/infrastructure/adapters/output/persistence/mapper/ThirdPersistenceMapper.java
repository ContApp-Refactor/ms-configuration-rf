package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.mapper;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.City;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Country;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.State;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Third;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.ThirdEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @brief Mapper para conversión entre ThirdEntity JPA y modelo de dominio Third
 */
@Mapper(componentModel = "spring", uses = {IdPersistenceMapper.class})
public interface ThirdPersistenceMapper {

    @Mapping(target = "typeId", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "country", expression = "java(mapCountryToString(third.getCountry()))")
    @Mapping(target = "province", expression = "java(mapStateToString(third.getProvince()))")
    @Mapping(target = "city", expression = "java(mapCityToString(third.getCity()))")
    ThirdEntity toThirdEntity(Third third);

    @Mapping(target = "typeId", source = "typeId")
    @Mapping(target = "thirdTypes", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "province", ignore = true)
    @Mapping(target = "city", ignore = true)
    Third toThird(ThirdEntity thirdEntity);

    default String mapCountryToString(Country country) {
        return country != null ? country.getCountryCode() : null;
    }

    default String mapStateToString(State state) {
        return state != null ? state.getStateCode() : null;
    }

    default String mapCityToString(City city) {
        return city != null ? city.getCityCode() : null;
    }
}