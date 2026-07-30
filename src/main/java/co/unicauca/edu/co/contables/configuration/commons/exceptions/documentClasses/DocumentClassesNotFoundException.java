package co.unicauca.edu.co.contables.configuration.commons.exceptions.documentClasses;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

/**
 * @brief Excepción para clase de documento no encontrada
 *
 * Se lanza cuando una clase de documento no es encontrada en las operaciones
 * del sistema, con soporte para identificación por ID.
 */
public class DocumentClassesNotFoundException extends BaseBusinessException {

    public DocumentClassesNotFoundException() {
        super(DocumentClassesErrorCode.DOCUMENT_CLASS_NOT_FOUND);
    }

    public DocumentClassesNotFoundException(Long id) {
        super(DocumentClassesErrorCode.DOCUMENT_CLASS_NOT_FOUND,
                String.format("La clase de documento con ID %d no fue encontrada", id));
    }
}


