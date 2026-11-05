package co.unicauca.edu.co.contables.inventory.products.domain.exception.product;

import co.unicauca.edu.co.contables.inventory.products.domain.exception.BaseBusinessException;
import co.unicauca.edu.co.contables.inventory.products.domain.exception.ErrorCode;

/**
 * @brief Excepción para errores durante exportación de productos
 *
 * Se lanza cuando ocurren problemas durante el proceso de exportación
 * de productos a archivos Excel, incluyendo casos sin datos disponibles.
 */
public class ProductExportException extends BaseBusinessException {

    /**
     * Constructor con código de error y mensaje.
     *
     * @param errorCode código específico del error
     * @param message mensaje descriptivo del error
     */
    public ProductExportException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Constructor con código de error, mensaje y causa.
     *
     * @param errorCode código específico del error
     * @param message mensaje descriptivo del error
     * @param cause causa raíz del error
     */
    public ProductExportException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    /**
     * Constructor de conveniencia para exportación sin datos.
     *
     * @param status filtro de estado aplicado (true=activos, false=inactivos, null=todos)
     * @return nueva instancia de ProductExportException
     */
    public static ProductExportException forNoData(Boolean status) {
        String message;
        if (status == null) {
            message = "No hay productos para exportar";
        } else if (status) {
            message = "No hay productos activos para exportar";
        } else {
            message = "No hay productos inactivos para exportar";
        }

        return new ProductExportException(
            ErrorCode.PRODUCT_EXPORT_NO_DATA,
            message
        );
    }
}
