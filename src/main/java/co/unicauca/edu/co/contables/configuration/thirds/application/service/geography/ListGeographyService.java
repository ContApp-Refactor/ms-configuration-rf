package co.unicauca.edu.co.contables.configuration.thirds.application.service.geography;

import co.unicauca.edu.co.contables.configuration.thirds.application.ports.input.ListGeographyUseCase;
import co.unicauca.edu.co.contables.configuration.thirds.application.ports.output.GeographyOutputPort;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.geography.CountryNotFoundException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.geography.StateNotFoundException;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.City;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Country;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.State;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @brief Servicio que implementa casos de uso para listar información geográfica
 *
 * Proporciona validación de jerarquía geográfica y acceso seguro a datos
 * de países, estados y ciudades con manejo de excepciones personalizado.
 */
@Service
@RequiredArgsConstructor
public class ListGeographyService implements ListGeographyUseCase {

    private final GeographyOutputPort geographyOutputPort;

    /**
     * @brief Obtiene todos los países activos
     * @return Lista de países activos ordenados por nombre
     */
    @Override
    public List<Country> getAllCountries() {
        return geographyOutputPort.getAllActiveCountries();
    }

    /**
     * @brief Obtiene estados activos de un país específico
     * @param countryCode Código del país
     * @return Lista de estados activos del país ordenados por nombre
     * @throws CountryNotFoundException si el país no existe o no está activo
     */
    @Override
    public List<State> getStatesByCountry(String countryCode) {
        // Validar jerarquía geográfica
        if (!geographyOutputPort.existsActiveCountry(countryCode)) {
            throw new CountryNotFoundException("El país con código '" + countryCode + "' no existe");
        }

        return geographyOutputPort.getStatesByCountry(countryCode);
    }

    /**
     * @brief Obtiene ciudades activas de un estado específico
     * @param stateCode Código del estado
     * @param countryCode Código del país
     * @return Lista de ciudades activas del estado ordenadas por nombre
     * @throws CountryNotFoundException si el país no existe o no está activo
     * @throws StateNotFoundException si el estado no existe o no pertenece al país
     */
    @Override
    public List<City> getCitiesByState(String stateCode, String countryCode) {
        // Validar jerarquía geográfica
        if (!geographyOutputPort.existsActiveCountry(countryCode)) {
            throw new CountryNotFoundException("El país con código '" + countryCode + "' no existe");
        }

        if (!geographyOutputPort.existsActiveState(stateCode, countryCode)) {
            throw new StateNotFoundException(
                    "El estado con código '" + stateCode + "' no existe o no pertenece al país '" + countryCode + "'");
        }

        return geographyOutputPort.getCitiesByState(stateCode, countryCode);
    }
}
