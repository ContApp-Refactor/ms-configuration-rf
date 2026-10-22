package co.unicauca.edu.co.contables.configuration.thirds.application.ports.output;

import co.unicauca.edu.co.contables.configuration.thirds.domain.model.City;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.Country;
import co.unicauca.edu.co.contables.configuration.thirds.domain.model.State;

import java.util.List;

/**
 * @brief Puerto de salida para operaciones geográficas
 *
 * Define el contrato para acceder a datos geográficos
 * de países, estados y ciudades con validación de jerarquía.
 */
public interface GeographyOutputPort {

    /**
     * @brief Obtiene todos los países activos
     * @return Lista de países activos ordenados por nombre
     */
    List<Country> getAllActiveCountries();

    /**
     * @brief Obtiene estados activos de un país específico
     * @param countryCode Código del país
     * @return Lista de estados activos del país ordenados por nombre
     */
    List<State> getStatesByCountry(String countryCode);

    /**
     * @brief Obtiene ciudades activas de un estado específico
     * @param stateCode Código del estado
     * @param countryCode Código del país
     * @return Lista de ciudades activas del estado ordenadas por nombre
     */
    List<City> getCitiesByState(String stateCode, String countryCode);

    /**
     * @brief Verifica existencia de país activo
     * @param countryCode Código del país
     * @return true si el país existe y está activo, false en caso contrario
     */
    boolean existsActiveCountry(String countryCode);

    /**
     * @brief Verifica existencia de estado activo en un país
     * @param stateCode Código del estado
     * @param countryCode Código del país
     * @return true si el estado existe y está activo, false en caso contrario
     */
    boolean existsActiveState(String stateCode, String countryCode);

    /**
     * @brief Verifica existencia de ciudad activa en un estado
     * @param cityCode Código de la ciudad
     * @param stateCode Código del estado
     * @param countryCode Código del país
     * @return true si la ciudad existe y está activa, false en caso contrario
     */
    boolean existsActiveCity(String cityCode, String stateCode, String countryCode);

    /**
     * @brief Obtiene todos los estados activos (sin filtrar por país)
     * @details Optimizado para carga batch en exportaciones masivas
     * @return Lista de todos los estados activos
     */
    List<State> getAllActiveStates();

    /**
     * @brief Obtiene todas las ciudades activas (sin filtrar por estado)
     * @details Optimizado para carga batch en exportaciones masivas
     * @return Lista de todas las ciudades activas
     */
    List<City> getAllActiveCities();
}
