package co.unicauca.edu.co.contables.configuration.thirds.application.service.geography;

import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config.GeographyDataConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @brief Servicio para descubrimiento automático de archivos SQL de geografía
 *
 * Aplica el principio de responsabilidad única para localizar y ordenar
 * automáticamente archivos SQL de datos geográficos.
 */
@Service
@Slf4j
public class GeographyFileDiscoveryService {

    private final GeographyDataConfig config;
    private final ResourcePatternResolver resourceResolver;

    @Autowired
    public GeographyFileDiscoveryService(GeographyDataConfig config) {
        this(config, new PathMatchingResourcePatternResolver());
    }

    public GeographyFileDiscoveryService(GeographyDataConfig config, ResourcePatternResolver resourceResolver) {
        this.config = config;
        this.resourceResolver = resourceResolver != null ? resourceResolver : new PathMatchingResourcePatternResolver();
    }

    /**
     * @brief Descubre y ordena todos los archivos SQL de geografía automáticamente
     * @return Lista ordenada de rutas de archivos SQL de geografía
     */
    public List<String> discoverGeographyFiles() {
        List<String> allFiles = new ArrayList<>();

        try {
            // 1. Archivos de países (fase 1)
            allFiles.addAll(discoverCountryFiles());

            // 2. Archivos de estados (fase 2)
            allFiles.addAll(discoverStateFiles());

            // 3. Archivos de ciudades (fase 3) - ordenados automáticamente
            allFiles.addAll(discoverCityFiles());

            return allFiles;

        } catch (IOException e) {
            throw new RuntimeException("Error en descubrimiento de archivos", e);
        }
    }

    /**
     * @brief Descubre archivos de países (fase 1 de carga)
     * @return Lista con la ruta del archivo de países si existe
     * @throws IOException si ocurre error al verificar existencia del archivo
     */
    private List<String> discoverCountryFiles() throws IOException {
        String countriesFilePath = config.getBaseDirectory() + "/" + config.getCountriesFile();
        return resourceExists(countriesFilePath) ? List.of(countriesFilePath) : List.of();
    }

    /**
     * @brief Descubre archivos de estados por cada directorio de país (fase 2 de carga)
     * @return Lista ordenada de rutas de archivos de estados encontrados
     * @throws IOException si ocurre error al verificar existencia de archivos
     */
    private List<String> discoverStateFiles() throws IOException {
        List<String> stateFiles = new ArrayList<>();

        for (String countryDir : config.getCountryDirectories()) {
            String fullPath = config.getBaseDirectory() + "/" + countryDir + "/" + config.getStatesFile();
            if (resourceExists(fullPath)) {
                stateFiles.add(fullPath);
            }
        }

        return stateFiles;
    }

    /**
     * @brief Descubre archivos de ciudades por cada directorio de país (fase 3 de carga)
     * @return Lista ordenada alfabéticamente de rutas de archivos de ciudades encontrados
     * @throws IOException si ocurre error al buscar archivos con patrón
     */
    private List<String> discoverCityFiles() throws IOException {
        List<String> cityFiles = new ArrayList<>();

        for (String countryDir : config.getCountryDirectories()) {
            String pattern = config.getBaseDirectory() + "/" + countryDir + "/" + config.getCitiesPattern();

            Resource[] resources = resourceResolver.getResources("classpath:" + pattern);

            // Ordenar archivos por nombre para mantener consistencia
            Arrays.stream(resources)
                    .map(resource -> extractRelativePath(resource, config.getBaseDirectory()))
                    .filter(Objects::nonNull)
                    .sorted()
                    .forEach(cityFiles::add);
        }

        return cityFiles;
    }

    /**
     * @brief Verifica si un recurso existe en el classpath
     * @param path Ruta del recurso a verificar
     * @return true si el recurso existe, false en caso contrario
     */
    private boolean resourceExists(String path) {
        try {
            Resource resource = resourceResolver.getResource("classpath:" + path);
            return resource.exists();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @brief Extrae la ruta relativa de un recurso respecto al directorio base
     * @param resource Recurso del cual extraer la ruta
     * @param baseDir Directorio base para calcular la ruta relativa
     * @return Ruta relativa del recurso o null si no se puede extraer
     */
    private String extractRelativePath(Resource resource, String baseDir) {
        try {
            String fullPath = resource.getURI().toString();
            int baseIndex = fullPath.indexOf(baseDir);
            return baseIndex >= 0 ? fullPath.substring(baseIndex) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
