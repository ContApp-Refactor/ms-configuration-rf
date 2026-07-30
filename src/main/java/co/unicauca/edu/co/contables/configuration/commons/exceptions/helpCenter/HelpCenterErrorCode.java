package co.unicauca.edu.co.contables.configuration.commons.exceptions.helpCenter;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.ErrorCodeDefinition;
import lombok.Getter;

/**
 * @brief Códigos de error para excepciones del centro de ayuda
 *
 * Enumeración que define los códigos de error y mensajes asociados
 * para las excepciones relacionadas con operaciones del centro de ayuda.
 */
@Getter
public enum HelpCenterErrorCode implements ErrorCodeDefinition {
    HELP_CENTER_NOT_FOUND("HELP_CENTER_NOT_FOUND", "Registro de ayuda no encontrado"),
    HELP_CENTER_ALREADY_EXISTS("HELP_CENTER_ALREADY_EXISTS", "Registro de ayuda ya existe"),
    INVALID_MODULE("INVALID_MODULE", "Módulo inválido");

    private final String code;
    private final String message;

    HelpCenterErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
