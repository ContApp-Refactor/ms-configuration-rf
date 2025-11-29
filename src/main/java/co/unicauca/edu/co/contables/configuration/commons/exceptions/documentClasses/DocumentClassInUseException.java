package co.unicauca.edu.co.contables.configuration.commons.exceptions.documentClasses;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

/**
 * @brief Excepción para clase de documento en uso
 *
 * Se lanza cuando se intenta eliminar o editar una clase de documento
 * que está siendo utilizada por tipos de documentos.
 */
public class DocumentClassInUseException extends BaseBusinessException {

    public DocumentClassInUseException() {
        super(DocumentClassesErrorCode.DOCUMENT_CLASS_IN_USE);
    }

    public DocumentClassInUseException(String className) {
        super(
            DocumentClassesErrorCode.DOCUMENT_CLASS_IN_USE,
            String.format("No se puede eliminar la clase de documento %s porque está siendo utilizada por tipos de documentos", className)
        );
    }

    public DocumentClassInUseException(Long id, String className) {
        super(
            DocumentClassesErrorCode.DOCUMENT_CLASS_IN_USE,
            String.format("No se puede eliminar la clase de documento con ID %d ('%s') porque está siendo utilizada por tipos de documentos", id, className)
        );
    }

    /**
     * Constructor para el caso de edición cuando hay tipos con registros contables
     * @param className nombre de la clase de documento
     * @param isEditOperation true si es operación de edición, false si es eliminación
     */
    public DocumentClassInUseException(String className, boolean isEditOperation) {
        super(
            DocumentClassesErrorCode.DOCUMENT_CLASS_IN_USE,
            isEditOperation ?
                String.format("No se puede editar la clase de documento %s porque tiene tipos de documento con movimientos contables", className) :
                String.format("No se puede eliminar la clase de documento %s porque está siendo utilizada por tipos de documentos activos", className)
        );
    }
}
