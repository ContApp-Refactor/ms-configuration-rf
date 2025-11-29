package co.unicauca.edu.co.contables.configuration.commons.exceptions.documentTypes;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

/**
 * @brief Excepción para tipo de documento en uso
 *
 * Se lanza cuando se intenta modificar o eliminar un tipo de documento
 * que tiene movimientos registrados (usageCount > 0).
 */
public class DocumentTypeInUseException extends BaseBusinessException {

    public DocumentTypeInUseException(String prefix, boolean isEditOperation) {
        super(DocumentTypesErrorCode.DOCUMENT_TYPE_IN_USE,
              isEditOperation ?
              String.format("No se puede editar el tipo de documento %s porque tiene movimientos contables", prefix) :
              String.format("No se puede eliminar el tipo de documento %s porque tiene movimientos contables", prefix));
    }
}
