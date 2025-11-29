package co.unicauca.edu.co.contables.configuration.commons.exceptions.documentTypes;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

/**
 * @brief Excepción para tipo de documento no encontrado
 *
 * Se lanza cuando un tipo de documento no es encontrado en las operaciones
 * del sistema, con soporte para identificación por ID.
 */
public class DocumentTypesNotFoundException extends BaseBusinessException {

    public DocumentTypesNotFoundException() {
        super(DocumentTypesErrorCode.DOCUMENT_TYPE_NOT_FOUND);
    }

    public DocumentTypesNotFoundException(Long id) {
        super(DocumentTypesErrorCode.DOCUMENT_TYPE_NOT_FOUND,
                String.format("El tipo de documento con ID %d no fue encontrado", id));
    }
}


