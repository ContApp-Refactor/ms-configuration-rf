package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @brief Configuración centralizada para carga automática de datos geográficos
 *
 * Contiene constantes y rutas para el descubrimiento automático de archivos SQL
 * de países, estados y ciudades. Facilita la configuración de carga inicial
 * de datos maestros geográficos sin hardcodear valores.
 */
@Component
public class GeographyDataConfig {
    public static final String BASE_DIRECTORY = "data/geography";
    public static final String COUNTRIES_FILE = "01_countries.sql";
    public static final String STATES_FILE = "02_states.sql";
    public static final String CITIES_PATTERN = "**/*cities*.sql";
    public static final List<String> COUNTRY_DIRECTORIES = List.of(
        "colombia"
    );
    
    public String getBaseDirectory() {
        return BASE_DIRECTORY;
    }
    
    public String getCountriesFile() {
        return COUNTRIES_FILE;
    }
    
    public String getStatesFile() {
        return STATES_FILE;
    }
    
    public String getCitiesPattern() {
        return CITIES_PATTERN;
    }
    
    public List<String> getCountryDirectories() {
        return COUNTRY_DIRECTORIES;
    }
}
