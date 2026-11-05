package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.mapper;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.City;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Country;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.State;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response.CityResponse;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response.CountryResponse;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.rest.dto.response.StateResponse;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @brief Mapper para conversión entre entidades geográficas y DTOs de respuesta
 */
@Mapper(componentModel = "spring")
public interface GeographyRestMapper {

    CountryResponse toCountryResponse(Country country);

    List<CountryResponse> toCountryResponseList(List<Country> countries);

    StateResponse toStateResponse(State state);

    List<StateResponse> toStateResponseList(List<State> states);

    CityResponse toCityResponse(City city);

    List<CityResponse> toCityResponseList(List<City> cities);
}
