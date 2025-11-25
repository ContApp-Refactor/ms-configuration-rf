package co.unicauca.edu.co.contables.configuration.commons.exceptions.documentTypes;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.ErrorCodeDefinition;
import lombok.Getter;

@Getter
public enum DocumentTypesErrorCode implements ErrorCodeDefinition {
    DOCUMENT_TYPE_NOT_FOUND("DOCUMENT_TYPE_NOT_FOUND", "Tipo de documento no encontrado"),
    DOCUMENT_TYPE_ALREADY_EXISTS("DOCUMENT_TYPE_ALREADY_EXISTS", "Tipo de documento ya existe"),
    INVALID_MODULE("INVALID_MODULE", "Módulo inválido"),
    DOCUMENT_TYPE_IN_USE("DOCUMENT_TYPE_IN_USE", "No se puede modificar o eliminar un tipo de documento que tiene movimientos registrados");

    private final String code;
    private final String message;

    DocumentTypesErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}


