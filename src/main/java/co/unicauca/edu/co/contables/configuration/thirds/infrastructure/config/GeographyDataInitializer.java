package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config;

import co.unicauca.edu.co.contables.configuration.thirds.application.service.geography.GeographyFileDiscoveryService;
import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.geography.GeographyDataInitializationException;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.SqlScriptRunner;
import co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @brief Inicializador automático de datos geográficos al arranque de la aplicación
 *
 * Implementa CommandLineRunner para cargar datos maestros geográficos (países, estados, ciudades)
 * desde archivos SQL durante el startup. Aplica principios SOLID con composición sobre herencia
 * y delega responsabilidades específicas a servicios especializados.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GeographyDataInitializer implements CommandLineRunner {

    private final CountryRepository countryRepository;
    private final GeographyFileDiscoveryService fileDiscoveryService;
    private final SqlScriptRunner sqlScriptRunner;

    /**
     * @brief Punto de entrada de Spring Boot para inicialización automática
     * @param args argumentos de línea de comandos (no utilizados)
     * @throws Exception si falla la inicialización de datos geográficos
     */
    @Override
    public void run(String... args) throws Exception {
        if (shouldLoadGeographyData()) {
            loadGeographyData();
        }
    }

    /**
     * @brief Verifica si la base de datos necesita carga inicial de datos geográficos
     * @return true si no existen países en BD, false si ya están cargados
     */
    private boolean shouldLoadGeographyData() {
        return countryRepository.count() == 0;
    }

    /**
     * @brief Coordina la carga completa de datos geográficos con manejo de errores
     * @details Descubre archivos SQL, valida existencia y ejecuta carga secuencial.
     * Maneja excepciones específicas de inicialización geográfica.
     * @throws GeographyDataInitializationException si falla cualquier paso del proceso
     */
    private void loadGeographyData() {
        try {
            List<String> discoveredFiles = fileDiscoveryService.discoverGeographyFiles();

            if (discoveredFiles.isEmpty()) {
                return;
            }
            executeGeographyFiles(discoveredFiles);

        } catch (Exception e) {
            throw new GeographyDataInitializationException("Fallo en la inicialización de datos geográficos", e);
        }
    }

    /**
     * @brief Ejecuta secuencialmente todos los archivos SQL de datos geográficos
     * @details Procesa cada archivo SQL en orden, ejecutando todas las sentencias.
     * Si falla cualquier archivo, detiene el proceso y lanza excepción específica.
     * @param sqlFiles lista de rutas de archivos SQL a ejecutar
     * @throws GeographyDataInitializationException si falla la ejecución de algún archivo
     */
    private void executeGeographyFiles(List<String> sqlFiles) {
        for (String sqlFile : sqlFiles) {
            try {
                sqlScriptRunner.executeSqlFile(sqlFile);
            } catch (Exception e) {
                throw new GeographyDataInitializationException(
                        "Error al procesar archivo de datos geográficos: " + sqlFile, e);
            }
        }
    }
}
