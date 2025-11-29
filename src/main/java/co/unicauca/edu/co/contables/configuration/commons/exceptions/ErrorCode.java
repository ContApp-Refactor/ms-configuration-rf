package co.unicauca.edu.co.contables.configuration.commons.exceptions;

import lombok.Getter;

/**
 * @brief Catálogo de errores núcleo de la aplicación
 *
 * Catálogo de errores núcleo y comunes a toda la aplicación.
 */
@Getter
public enum ErrorCode implements ErrorCodeDefinition {

    GENERIC_ERROR("GENERIC_ERROR", "Ha ocurrido un error");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
