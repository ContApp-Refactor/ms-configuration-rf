package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.output.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @brief Ejecutor de scripts SQL desde archivos del classpath
 *
 * Componente utilitario para ejecutar scripts SQL almacenados como recursos.
 * Utilizado principalmente para inicialización de datos geográficos y maestros.
 * Maneja parsing de sentencias SQL con separación inteligente por punto y coma.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SqlScriptRunner {
    
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * @brief Ejecuta script SQL completo desde archivo del classpath
     * @details Lee el archivo SQL, valida su existencia, parsea las sentencias
     * individualmente (manejando strings correctamente) y ejecuta cada una.
     * @param sqlFilePath ruta del archivo SQL en el classpath (ej: "data/countries.sql")
     * @throws RuntimeException si el archivo no existe o hay errores de ejecución
     */
    public void executeSqlFile(String sqlFilePath) {
        try {
            
            ClassPathResource resource = new ClassPathResource(sqlFilePath);
            validateResource(resource, sqlFilePath);
            
            String sqlContent = readSqlContent(resource);
            executeSqlStatements(sqlContent);
            
            
        } catch (Exception e) {
            throw new RuntimeException("Error cargando datos desde archivo: " + sqlFilePath, e);
        }
    }
    
    /**
     * @brief Valida existencia del recurso SQL
     * @details Verifica que el archivo especificado exista en el classpath antes de intentar leerlo.
     * @param resource recurso ClassPathResource a validar
     * @param sqlFilePath ruta original del archivo para mensaje de error
     * @throws IOException si el archivo no existe
     */
    private void validateResource(ClassPathResource resource, String sqlFilePath) throws IOException {
        if (!resource.exists()) {
            throw new IOException("Archivo SQL no encontrado: " + sqlFilePath);
        }
    }
    
    /**
     * @brief Lee y procesa contenido del archivo SQL
     * @details Lee línea por línea el archivo SQL, filtra comentarios y líneas vacías,
     * y construye el contenido SQL válido para ejecución.
     * @param resource recurso ClassPathResource del archivo SQL
     * @return contenido SQL limpio y listo para parsing
     * @throws IOException si hay errores de lectura del archivo
     */
    private String readSqlContent(ClassPathResource resource) throws IOException {
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            StringBuilder sqlContent = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (isValidSqlLine(line)) {
                    sqlContent.append(line).append("\n");
                }
            }

            return sqlContent.toString().trim();
        }
    }
    
    /**
     * @brief Valida si una línea contiene SQL ejecutable
     * @details Filtra líneas vacías y comentarios SQL (que empiezan con --).
     * @param line línea de texto a validar
     * @return true si la línea contiene SQL válido para ejecutar
     */
    private boolean isValidSqlLine(String line) {
        return !line.isEmpty() && !line.startsWith("--");
    }
    
    /**
     * @brief Ejecuta sentencias SQL individuales con parsing inteligente
     * @details Divide el contenido SQL por punto y coma usando regex que respeta
     * strings (no divide dentro de comillas simples). Ejecuta cada sentencia no vacía.
     * @param sqlContent contenido completo del archivo SQL
     */
    private void executeSqlStatements(String sqlContent) {
        if (sqlContent.isEmpty()) {
            return;
        }

        // Dividir por punto y coma, respetando strings
        String[] statements = sqlContent.split(";(?=([^']*'[^']*')*[^']*$)");

        for (String statement : statements) {
            String trimmedStatement = statement.trim();
            if (!trimmedStatement.isEmpty()) {
                jdbcTemplate.execute(trimmedStatement);
            }
        }
    }
}
