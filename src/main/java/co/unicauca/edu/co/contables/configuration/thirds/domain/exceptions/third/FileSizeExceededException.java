package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.third;

import co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions.BaseBusinessException;

/**
 * @brief Excepción lanzada cuando un archivo excede el tamaño máximo permitido
 *
 * Se utiliza en validaciones de archivos para prevenir el procesamiento
 * de archivos demasiado grandes que puedan afectar el rendimiento del sistema.
 */
public class FileSizeExceededException extends BaseBusinessException {

    /**
     * @brief Constructor con tamaño máximo permitido
     *
     * Crea una excepción específica cuando se detecta que un archivo supera
     * el límite de tamaño establecido, generando automáticamente un mensaje
     * con el tamaño máximo formateado de manera legible.
     * @param maxSize tamaño máximo permitido en bytes
     */
    public FileSizeExceededException(long maxSize) {
        super(
            ThirdsErrorCode.FILE_SIZE_EXCEEDED,
            String.format("El archivo excede el tamaño máximo permitido de %s.",
                formatFileSize(maxSize))
        );
    }

    /**
     * @brief Formatea el tamaño del archivo en unidades legibles (KB, MB)
     *
     * Convierte un tamaño en bytes a una representación más legible para usuarios,
     * mostrando KB para archivos pequeños y MB para archivos más grandes.
     * @param sizeInBytes tamaño del archivo en bytes
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
