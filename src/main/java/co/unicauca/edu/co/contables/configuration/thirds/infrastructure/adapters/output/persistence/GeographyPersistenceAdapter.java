package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.GeographyOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.City;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Country;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.State;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.mapper.GeographyPersistenceMapper;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.CityRepository;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.CountryRepository;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @brief Adaptador de persistencia para consultas geográficas optimizadas
 *
 * Implementa GeographyOutputPort para acceso eficiente a datos geográficos.
 * Utiliza consultas especializadas de los repositorios geográficos para obtener
 * países, estados y ciudades con ordenamiento.
 */
@Component
@RequiredArgsConstructor
public class GeographyPersistenceAdapter implements GeographyOutputPort {

    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;
    private final GeographyPersistenceMapper geographyMapper;

    @Override
    public List<Country> getAllActiveCountries() {
        return geographyMapper.toCountryList(countryRepository.findAllCountries());
    }

    @Override
    public List<State> getStatesByCountry(String countryCode) {
        return geographyMapper.toStateList(stateRepository.findByCountryCodeOrderByStateName(countryCode));
    }

    @Override
    public List<City> getCitiesByState(String stateCode, String countryCode) {
        return geographyMapper.toCityList(cityRepository.findByStateCodeAndCountryCodeOrderByCityName(stateCode, countryCode));
    }

    @Override
    public boolean existsActiveCountry(String countryCode) {
        return countryRepository.existsByCountryCode(countryCode);
    }

    @Override
    public boolean existsActiveState(String stateCode, String countryCode) {
        return stateRepository.existsByStateCodeAndCountryCode(stateCode, countryCode);
    }

    @Override
    public boolean existsActiveCity(String cityCode, String stateCode, String countryCode) {
        return cityRepository.existsByCityCodeAndStateCodeAndCountryCode(cityCode, stateCode, countryCode);
    }

    @Override
    public List<State> getAllActiveStates() {
        return geographyMapper.toStateList(stateRepository.findAllStates());
    }

    @Override
    public List<City> getAllActiveCities() {
        return geographyMapper.toCityList(cityRepository.findAllCities());
    }
}
