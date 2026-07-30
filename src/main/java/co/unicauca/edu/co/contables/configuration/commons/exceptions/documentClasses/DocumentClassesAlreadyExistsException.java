package co.unicauca.edu.co.contables.configuration.commons.exceptions.documentClasses;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

/**
 * @brief Excepción para clase de documento ya existente
 *
 * Se lanza cuando una clase de documento ya existe (por nombre)
 * dentro de una empresa, evitando duplicados.
 */
public class DocumentClassesAlreadyExistsException extends BaseBusinessException {

    public DocumentClassesAlreadyExistsException() {
        super(DocumentClassesErrorCode.DOCUMENT_CLASS_ALREADY_EXISTS);
    }

    public DocumentClassesAlreadyExistsException(String name, String idEnterprise) {
        super(
            DocumentClassesErrorCode.DOCUMENT_CLASS_ALREADY_EXISTS,
            String.format("Ya existe una clase de documento con nombre '%s' en la empresa %s", name, idEnterprise)
        );
    }
}


