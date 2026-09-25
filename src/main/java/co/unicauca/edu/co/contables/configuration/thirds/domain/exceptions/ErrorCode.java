package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions;

import lombok.Getter;

/**
 * @brief Catálogo de errores núcleo y comunes a toda la aplicación
 *
 * Define los códigos de error genéricos que pueden ser utilizados
 * en cualquier parte del sistema cuando no se requiere un código específico de dominio.
 */
@Getter
public enum ErrorCode implements ErrorCodeDefinition {

    GENERIC_ERROR("GENERIC_ERROR", "Ha ocurrido un error");

    private final String code;
    private final String message;

    /**
     * @brief Constructor del enum
     * @param code código único del error genérico
     * @param message mensaje descriptivo del error genérico
     */
    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
