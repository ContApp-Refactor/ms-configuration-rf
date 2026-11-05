package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.mapper;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.City;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Country;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.State;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.CityEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.CountryEntity;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.entity.StateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * @brief Mapper para conversión entre entidades geográficas JPA y modelos del dominio
 */
@Mapper(componentModel = "spring")
public interface GeographyPersistenceMapper {

    CountryEntity toCountryEntity(Country country);

    Country toCountry(CountryEntity countryEntity);

    List<CountryEntity> toCountryEntityList(List<Country> countries);

    List<Country> toCountryList(List<CountryEntity> countryEntities);

    @Mapping(target = "country", ignore = true)
    StateEntity toStateEntity(State state);

    @Mapping(target = "country", ignore = true)
    State toState(StateEntity stateEntity);

    List<StateEntity> toStateEntityList(List<State> states);

    List<State> toStateList(List<StateEntity> stateEntities);

    @Mapping(target = "state", ignore = true)
    CityEntity toCityEntity(City city);

    @Mapping(target = "state", ignore = true)
    City toCity(CityEntity cityEntity);

    List<CityEntity> toCityEntityList(List<City> cities);

    List<City> toCityList(List<CityEntity> cityEntities);
}
