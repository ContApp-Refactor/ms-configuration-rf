package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.typeId;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.ErrorCodeDefinition;
import lombok.Getter;

/**
 * @brief Códigos de error específicos del dominio de Tipos de Identificación
 * 
 */
@Getter
public enum TypeIdErrorCode implements ErrorCodeDefinition {

    TYPE_ID_NOT_FOUND("TYPE_ID_NOT_FOUND", "Tipo de identificación no encontrado"),
    TYPE_ID_ALREADY_EXISTS("TYPE_ID_ALREADY_EXISTS", "El tipo de identificación ya existe"),
    TYPE_ID_NAME_ALREADY_EXISTS("TYPE_ID_NAME_ALREADY_EXISTS", "Ya existe un tipo de identificación con ese nombre"),
    TYPE_ID_INVALID_DATA("TYPE_ID_INVALID_DATA", "Datos del tipo de identificación inválidos"),
    TYPE_ID_IN_USE("TYPE_ID_IN_USE", "El tipo de identificación está siendo utilizado y no puede ser eliminado"),
    TYPE_ID_FOREIGN_KEY_VIOLATION("TYPE_ID_FOREIGN_KEY_VIOLATION", "Tipo de identificación no válido o no encontrado"),
    TYPE_ID_INVALID_CLASSIFICATION("TYPE_ID_INVALID_CLASSIFICATION", "Clasificación de persona inválida o no encontrada");

    private final String code;
    private final String message;

    /**
     * @brief Constructor del enum
     * @param code código único del error utilizado internamente
     * @param message mensaje descriptivo del error para usuarios
     */
    TypeIdErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
