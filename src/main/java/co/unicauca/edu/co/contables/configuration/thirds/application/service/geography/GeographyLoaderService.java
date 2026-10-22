package co.unicauca.edu.co.contables.configuration.thirds.application.service.geography;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.GeographyOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.City;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Country;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.State;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @brief Servicio para carga de datos geográficos completos desde códigos
 *
 * Utilizado por la capa de persistencia para reconstruir objetos geográficos
 * completos a partir de cadenas de texto almacenadas en base de datos.
 */
@Service
@RequiredArgsConstructor
public class GeographyLoaderService {

    private final GeographyOutputPort geographyOutputPort;

    /**
     * @brief Carga objeto Country completo desde código de país
     * @param countryCode el código del país
     * @return objeto Country completo o null si no se encuentra
     */
    public Country loadCountryByCode(String countryCode) {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            return null;
        }

        try {
            List<Country> countries = geographyOutputPort.getAllActiveCountries();
            return countries.stream()
                    .filter(country -> country.getCountryCode().equals(countryCode.trim().toUpperCase()))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            // If geography service fails, return basic object with code only
            return Country.builder()
                    .countryCode(countryCode.trim().toUpperCase())
                    .countryName("")
                    .build();
        }
    }

    /**
     * @brief Carga objeto State completo desde códigos de estado y país
     * @param stateCode el código del estado
     * @param countryCode el código del país
     * @return objeto State completo o null si no se encuentra
     */
    public State loadStateByCode(String stateCode, String countryCode) {
        if (stateCode == null || stateCode.trim().isEmpty() ||
                countryCode == null || countryCode.trim().isEmpty()) {
            return null;
        }

        try {
            // Load the country first
            Country country = loadCountryByCode(countryCode);

            // Load states for the country
            List<State> states = geographyOutputPort.getStatesByCountry(countryCode.trim().toUpperCase());
            State state = states.stream()
                    .filter(s -> s.getStateCode().equals(stateCode.trim()))
                    .findFirst()
                    .orElse(null);

            if (state != null && country != null) {
                // Set the country reference in the state
                return State.builder()
                        .stateCode(state.getStateCode())
                        .stateName(state.getStateName())
                        .countryCode(state.getCountryCode())
                        .country(country)
                        .build();
            }

            return null;
        } catch (Exception e) {
            // If geography service fails, return basic object with codes only
            return State.builder()
                    .stateCode(stateCode.trim())
                    .stateName("")
                    .countryCode(countryCode.trim().toUpperCase())
                    .country(loadCountryByCode(countryCode))
                    .build();
        }
    }

    /**
     * @brief Carga objeto City completo desde códigos de ciudad, estado y país
     * @param cityCode el código de la ciudad
     * @param stateCode el código del estado
     * @param countryCode el código del país
     * @return objeto City completo o null si no se encuentra
     */
    public City loadCityByCode(String cityCode, String stateCode, String countryCode) {
        if (cityCode == null || cityCode.trim().isEmpty() ||
                stateCode == null || stateCode.trim().isEmpty() ||
                countryCode == null || countryCode.trim().isEmpty()) {
            return null;
        }

        try {
            // Load the state first (which includes country)
            State state = loadStateByCode(stateCode, countryCode);

            // Load cities for the state
            List<City> cities = geographyOutputPort.getCitiesByState(stateCode.trim(),
                    countryCode.trim().toUpperCase());
            City city = cities.stream()
                    .filter(c -> c.getCityCode().equals(cityCode.trim()))
                    .findFirst()
                    .orElse(null);

            if (city != null && state != null) {
                // Set the state reference in the city
                return City.builder()
                        .cityCode(city.getCityCode())
                        .cityName(city.getCityName())
                        .stateCode(city.getStateCode())
                        .countryCode(city.getCountryCode())
                        .state(state)
                        .build();
            }

            return null;
        } catch (Exception e) {
            // If geography service fails, return basic object with codes only
            return City.builder()
                    .cityCode(cityCode.trim())
                    .cityName("")
                    .stateCode(stateCode.trim())
                    .countryCode(countryCode.trim().toUpperCase())
                    .state(loadStateByCode(stateCode, countryCode))
                    .build();
        }
    }
}
