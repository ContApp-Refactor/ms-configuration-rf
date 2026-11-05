package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue;

import co.unicauca.edu.co.contables.commons.exceptions.BaseBusinessException;

/**
 * Excepción lanzada cuando un archivo excede el tamaño máximo permitido.
 */
public class FileSizeExceededException extends BaseBusinessException {

    /**
     * Crea una excepción indicando el tamaño máximo permitido.
     * 
     * @param maxSize Tamaño máximo permitido en bytes
     */
    public FileSizeExceededException(long maxSize) {
        super(
            AccountCatalogueErrorCode.FILE_SIZE_EXCEEDED,
            String.format("El archivo excede el tamaño máximo permitido de %s.", 
                formatFileSize(maxSize))
        );
    }

    /**
     * Formatea el tamaño del archivo en unidades legibles (KB, MB).
     */
    private static String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + " bytes";
        } else if (sizeInBytes < 1024 * 1024) {
            return String.format("%.2f KB", sizeInBytes / 1024.0);
        } else {
            return String.format("%.2f MB", sizeInBytes / (1024.0 * 1024.0));
        }
    }
}

