package co.unicauca.edu.co.contables.inventory.products.infraestructure.input.validation;

import org.springframework.web.multipart.MultipartFile;

/**
 * @brief Interfaz contrato para validadores de archivos
 *
 * Define el contrato estándar para validadores de archivos específicos,
 * permitiendo validaciones de tamaño, extensión y tipo MIME.
 */
public interface FileValidator {

    /**
     * @brief Valida archivo según reglas específicas del tipo
     * @param file archivo a validar
     */
    void validate(MultipartFile file);

    /**
     * @brief Obtiene tipos MIME soportados por este validador
     * @return array de tipos MIME soportados
     */
    String[] getSupportedMimeTypes();

    /**
     * @brief Obtiene extensiones de archivo soportadas
     * @return array de extensiones soportadas
     */
    String[] getSupportedExtensions();
}