package co.unicauca.edu.co.contables.configuration.commons.exceptions.documentTypes;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

/**
 * @brief Excepción para tipo de documento ya existente
 *
 * Se lanza cuando un tipo de documento ya existe (por campo específico)
 * dentro de una empresa, evitando duplicados.
 */
public class DocumentTypesAlreadyExistsException extends BaseBusinessException {

    public DocumentTypesAlreadyExistsException() {
        super(DocumentTypesErrorCode.DOCUMENT_TYPE_ALREADY_EXISTS);
    }

    public DocumentTypesAlreadyExistsException(String field, String value, String idEnterprise) {
        super(DocumentTypesErrorCode.DOCUMENT_TYPE_ALREADY_EXISTS,
                String.format("Ya existe un tipo de documento con %s '%s' en la empresa %s", field, value, idEnterprise));
    }
}


