package co.unicauca.edu.co.contables.commons.exceptions.noCommercialTags;

import co.unicauca.edu.co.contables.commons.exceptions.ErrorCodeDefinition;
import lombok.Getter;



@Getter
public enum NoCommercialTagsErrorCode implements ErrorCodeDefinition{  

    NO_COMMERCIAL_TAGS_NOT_FOUND("NO_COMMERCIAL_TAGS_NOT_FOUND", "No se encontró la etiqueta comercial"),
    NO_COMMERCIAL_TAGS_ALREADY_EXISTS("NO_COMMERCIAL_TAGS_ALREADY_EXISTS", "La etiqueta comercial ya existe");
    

    private final String code;
    private final String message;
    
    NoCommercialTagsErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
