package co.unicauca.edu.co.contables.configuration.commons.exceptions.noCommercialTags;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;


public class NoCommercialAlreadyExistsException extends BaseBusinessException {

    public NoCommercialAlreadyExistsException() {
        super(NoCommercialTagsErrorCode.NO_COMMERCIAL_TAGS_ALREADY_EXISTS);
        
    }

    public NoCommercialAlreadyExistsException(String name, String idEnterprise) {
        super(NoCommercialTagsErrorCode.NO_COMMERCIAL_TAGS_ALREADY_EXISTS,
        String.format("Ya existe una etiqueta no comercial con el nombre '%s' para la empresa %s", name, idEnterprise));
    }

    
}
