package co.unicauca.edu.co.contables.configuration.thirds.infrastructure.adapters.input.validation;

import org.springframework.web.multipart.MultipartFile;

/**
 * @brief Interfaz para validadores de archivos usando patrón Strategy
 */
public interface FileValidator {

    /**
     * @brief Valida un archivo según reglas específicas del tipo
     * @param file archivo a validar
     */
    void validate(MultipartFile file);

    /**
     * @brief Obtiene los tipos MIME soportados por este validador
     * @return array de tipos MIME soportados
     */
    String[] getSupportedMimeTypes();

    /**
     * @brief Obtiene las extensiones soportadas por este validador
     * @return array de extensiones soportadas
     */
    String[] getSupportedExtensions();
}
