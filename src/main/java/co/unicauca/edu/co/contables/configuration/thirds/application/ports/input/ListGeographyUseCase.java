package co.unicauca.edu.co.contables.configuration.thirds.application.ports.input;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.City;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Country;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.State;

import java.util.List;

/**
 * @brief Caso de uso para consulta de información geográfica
 *
 * Proporciona acceso jerárquico a datos geográficos
 * con validación de relaciones entre países, estados y ciudades.
 */
public interface ListGeographyUseCase {

    /**
     * @brief Obtiene todos los países activos
     * @return Lista de países activos ordenados por nombre
     */
    List<Country> getAllCountries();

    /**
     * @brief Obtiene estados activos de un país específico
     * @param countryCode Código del país
     * @return Lista de estados activos del país ordenados por nombre
     * @throws IllegalArgumentException si el país no existe o no está activo
     */
    List<State> getStatesByCountry(String countryCode);

    /**
     * @brief Obtiene ciudades activas de un estado específico
     * @param stateCode Código del estado
     * @param countryCode Código del país
     * @return Lista de ciudades activas del estado ordenadas por nombre
     * @throws IllegalArgumentException si el estado no existe, no está activo o no pertenece al país
     */
    List<City> getCitiesByState(String stateCode, String countryCode);
}
