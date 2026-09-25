package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/**
 * @brief Propiedades de configuración para carga de archivos con validación
 *
 * Externaliza límites y restricciones de archivos en application.yml para facilitar
 * configuración sin recompilación. Soporta diferentes tipos de archivo con sus
 * extensiones y tipos MIME permitidos.
 */
@Data
@ConfigurationProperties(prefix = "file-upload")
public class FileUploadProperties {
    private Long maxSize;
    private Map<String, List<String>> allowedExtensions;
    private Map<String, List<String>> allowedMimeTypes;
}
