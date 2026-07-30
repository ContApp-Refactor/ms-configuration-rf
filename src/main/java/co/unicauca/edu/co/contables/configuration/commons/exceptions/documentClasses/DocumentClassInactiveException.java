package co.unicauca.edu.co.contables.configuration.commons.exceptions.documentClasses;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

/**
 * @brief Excepción para clase de documento inactiva
 *
 * Se lanza cuando se intenta usar una clase de documento inactiva
 * para crear o editar un tipo de documento.
 */
public class DocumentClassInactiveException extends BaseBusinessException {

    public DocumentClassInactiveException() {
        super(DocumentClassesErrorCode.DOCUMENT_CLASS_INACTIVE);
    }

    public DocumentClassInactiveException(String className) {
        super(
            DocumentClassesErrorCode.DOCUMENT_CLASS_INACTIVE,
            String.format("No se puede usar la clase de documento '%s' porque está inactiva", className)
        );
    }

    public DocumentClassInactiveException(Long id, String className) {
        super(
            DocumentClassesErrorCode.DOCUMENT_CLASS_INACTIVE,
            String.format("No se puede usar la clase de documento con ID %d ('%s') porque está inactiva", id, className)
        );
    }
}
