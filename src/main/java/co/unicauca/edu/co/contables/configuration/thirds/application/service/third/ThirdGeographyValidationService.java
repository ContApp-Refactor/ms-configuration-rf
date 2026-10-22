package co.unicauca.edu.co.contables.configuration.thirds.application.service.third;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.GeographyOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.geography.CountryNotFoundException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.geography.GeographyInvalidDataException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.geography.StateNotFoundException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.City;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Country;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.State;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @brief Servicio de validación geográfica para operaciones de terceros
 *
 * Valida la jerarquía geográfica: País → Estado/Departamento → Ciudad
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ThirdGeographyValidationService {

    private final GeographyOutputPort geographyOutputPort;

    /**
     * @brief Valida y obtiene la información geográfica completa basada en los códigos proporcionados
     * @param countryCode código del país (puede ser null)
     * @param stateCode código del departamento (puede ser null)
     * @param cityCode código de la ciudad (puede ser null)
     * @return array con [Country, State, City] validados (algunos pueden ser null)
     * @throws CountryNotFoundException si el país no existe
     * @throws StateNotFoundException si el departamento no existe
     * @throws GeographyInvalidDataException si la jerarquía es inválida
     */
    public Object[] validateAndGetGeography(String countryCode, String stateCode, String cityCode) {

        Country country = null;
        State state = null;
        City city = null;

        // Validar país si se proporciona
        if (countryCode != null && !countryCode.trim().isEmpty()) {
            if (!geographyOutputPort.existsActiveCountry(countryCode.trim().toUpperCase())) {
                throw new CountryNotFoundException(countryCode);
            }
            
            // Obtener país
            List<Country> countries = geographyOutputPort.getAllActiveCountries();
            country = countries.stream()
                    .filter(c -> c.getCountryCode().equals(countryCode.trim().toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new CountryNotFoundException(countryCode));
        }

        // Validar estado si se proporciona
        if (stateCode != null && !stateCode.trim().isEmpty()) {
            if (countryCode == null || countryCode.trim().isEmpty()) {
                throw new GeographyInvalidDataException("El código del país es obligatorio cuando se especifica estado");
            }
            
            if (!geographyOutputPort.existsActiveState(stateCode.trim(), countryCode.trim().toUpperCase())) {
                throw new StateNotFoundException(stateCode);
            }
            
            // Obtener estado
            List<State> states = geographyOutputPort.getStatesByCountry(countryCode.trim().toUpperCase());
            state = states.stream()
                    .filter(s -> s.getStateCode().equals(stateCode.trim()))
                    .findFirst()
                    .orElseThrow(() -> new StateNotFoundException(stateCode, countryCode));
        }

        // Validar ciudad si se proporciona
        if (cityCode != null && !cityCode.trim().isEmpty()) {
            if (stateCode == null || stateCode.trim().isEmpty()) {
                throw new GeographyInvalidDataException("El código del estado es obligatorio cuando se especifica ciudad");
            }
            
            if (countryCode == null || countryCode.trim().isEmpty()) {
                throw new GeographyInvalidDataException("El código del país es obligatorio cuando se especifica ciudad");
            }
            
            if (!geographyOutputPort.existsActiveCity(cityCode.trim(), stateCode.trim(), countryCode.trim().toUpperCase())) {
                throw new GeographyInvalidDataException(
                        String.format("La ciudad con código '%s' no existe en el estado '%s' del país '%s'",
                                cityCode, stateCode, countryCode));
            }
            
            // Obtener ciudad
            List<City> cities = geographyOutputPort.getCitiesByState(stateCode.trim(), countryCode.trim().toUpperCase());
            city = cities.stream()
                    .filter(c -> c.getCityCode().equals(cityCode.trim()))
                    .findFirst()
                    .orElseThrow(() -> new GeographyInvalidDataException(
                            String.format("La ciudad con código '%s' no existe en el estado '%s' del país '%s'",
                                    cityCode, stateCode, countryCode)));
        }

        // Validar jerarquía geográfica
        if (country != null && state != null && city != null) {
            validateGeographyHierarchy(country, state, city);
        } else if (country != null && state != null) {
            validateGeographyHierarchy(country, state, null);
        }

        return new Object[] { country, state, city };
    }

    /**
     * @brief Valida que la jerarquía geográfica sea consistente
     * @param country país
     * @param state departamento
     * @param city ciudad (puede ser null para validar solo país-departamento)
     * @throws GeographyInvalidDataException si la jerarquía es inconsistente
     */
    private void validateGeographyHierarchy(Country country, State state, City city) {
        // Validar que el estado pertenezca al país
        if (!country.getCountryCode().equals(state.getCountryCode())) {
            throw new GeographyInvalidDataException(
                    String.format("El estado '%s' no pertenece al país '%s'",
                            state.getStateName(), country.getCountryName()));
        }

        // Validar que la ciudad pertenezca al estado y país (solo si city no es null)
        if (city != null) {
            if (!state.getStateCode().equals(city.getStateCode()) ||
                    !country.getCountryCode().equals(city.getCountryCode())) {
                throw new GeographyInvalidDataException(
                        String.format("La ciudad '%s' no pertenece al estado '%s' del país '%s'",
                                city.getCityName(), state.getStateName(), country.getCountryName()));
            }
        }
    }
}
