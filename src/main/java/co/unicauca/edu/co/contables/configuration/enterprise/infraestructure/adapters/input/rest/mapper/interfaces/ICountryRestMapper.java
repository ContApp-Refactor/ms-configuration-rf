package co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.mapper.interfaces;

import co.unicauca.edu.co.contables.configuration.enterprise.domain.models.Country;
import co.unicauca.edu.co.contables.configuration.enterprise.infraestructure.adapters.input.rest.data.response.CountryResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface ICountryRestMapper {

    List<CountryResponse> toCountryResponseList(List<Country> countries);
}