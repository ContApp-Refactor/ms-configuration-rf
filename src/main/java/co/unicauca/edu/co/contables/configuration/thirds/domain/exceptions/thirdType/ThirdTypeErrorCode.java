package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.thirdType;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.ErrorCodeDefinition;
import lombok.Getter;

/**
 * @brief Códigos de error específicos del dominio de Tipos de Tercero
 *
 */
@Getter
public enum ThirdTypeErrorCode implements ErrorCodeDefinition {

    THIRD_TYPE_NOT_FOUND("THIRD_TYPE_NOT_FOUND", "Tipo de tercero no encontrado"),
    THIRD_TYPE_ALREADY_EXISTS("THIRD_TYPE_ALREADY_EXISTS", "El tipo de tercero ya existe"),
    THIRD_TYPE_NAME_ALREADY_EXISTS("THIRD_TYPE_NAME_ALREADY_EXISTS", "Ya existe un tipo de tercero con ese nombre"),
    THIRD_TYPE_INVALID_DATA("THIRD_TYPE_INVALID_DATA", "Datos del tipo de tercero inválidos"),
    THIRD_TYPE_IN_USE("THIRD_TYPE_IN_USE", "El tipo de tercero está siendo utilizado y no puede ser eliminado"),
    THIRD_TYPE_FOREIGN_KEY_VIOLATION("THIRD_TYPE_FOREIGN_KEY_VIOLATION", "Tipo de tercero no válido o no encontrado");

    private final String code;
    private final String message;

    /**
     * @brief Constructor del enum
     * @param code código único del error utilizado internamente
     * @param message mensaje descriptivo del error para usuarios
     */
    ThirdTypeErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
