package co.unicauca.edu.co.contables.configuration.commons.exceptions.documentClasses;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.ErrorCodeDefinition;
import lombok.Getter;

/**
 * @brief Códigos de error para excepciones de clases de documento
 *
 * Enumeración que define los códigos de error y mensajes asociados
 * para las excepciones relacionadas con operaciones de clases de documento.
 */
@Getter
public enum DocumentClassesErrorCode implements ErrorCodeDefinition {

    DOCUMENT_CLASS_NOT_FOUND("DOCUMENT_CLASS_NOT_FOUND", "Clase de documento no encontrada"),
    DOCUMENT_CLASS_ALREADY_EXISTS("DOCUMENT_CLASS_ALREADY_EXISTS", "Clase de documento ya existe"),
    DOCUMENT_CLASS_IN_USE("DOCUMENT_CLASS_IN_USE", "No se puede eliminar una clase de documento que está siendo utilizada"),
    DOCUMENT_CLASS_INACTIVE("DOCUMENT_CLASS_INACTIVE", "No se puede usar una clase de documento inactiva");

    private final String code;
    private final String message;

    DocumentClassesErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}


