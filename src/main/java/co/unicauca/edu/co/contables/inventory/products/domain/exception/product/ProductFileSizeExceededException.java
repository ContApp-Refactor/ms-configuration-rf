package co.unicauca.edu.co.contables.inventory.products.domain.exception.product;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.BaseBusinessException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.ErrorCode;

/**
 * @brief Excepción para archivo que excede tamaño máximo
 *
 * Se lanza cuando se intenta subir un archivo que supera el límite
 * de tamaño configurado para operaciones de importación.
 */
public class ProductFileSizeExceededException extends BaseBusinessException {

    
    public ProductFileSizeExceededException(long maxSize) {
        super(
            ErrorCode.FILE_VALIDATION_ERROR,
            String.format("El archivo excede el tamaño máximo permitido de %s.",
                formatFileSize(maxSize))
        );
    }

   /**
    * @brief Formatea el tamaño del archivo en unidades legibles (KB, MB)
    *
    * Convierte un tamaño en bytes a una representación más legible para usuarios,
    * mostrando KB para archivos pequeños y MB para archivos más grandes.
    * @param sizeInBytes
    * @return cadena formateada con el tamaño y unidad apropiada
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