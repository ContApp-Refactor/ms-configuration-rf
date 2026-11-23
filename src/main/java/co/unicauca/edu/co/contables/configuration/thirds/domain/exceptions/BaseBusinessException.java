package co.unicauca.edu.co.contables.configuration.thirds.domain.exceptions;

/**
 * @brief Excepción base para todas las excepciones de negocio del dominio
 *
 * Proporciona funcionalidad común para el manejo consistente de códigos de error
 * y mensajes en todas las excepciones de negocio de la aplicación.
 */
public abstract class BaseBusinessException extends RuntimeException {

    private final ErrorCodeDefinition errorCode;

    /**
     * @brief Constructor con código de error
     *
     * Crea una instancia de la excepción utilizando el mensaje por defecto
     * del código de error proporcionado.
     * @param errorCode código de error que identifica el tipo de problema
     */
    protected BaseBusinessException(ErrorCodeDefinition errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * @brief Constructor con código de error y mensaje personalizado
     *
     * Crea una instancia de la excepción con un mensaje personalizado
     * que reemplaza al mensaje por defecto del código de error.
     * @param errorCode código de error que identifica el tipo de problema
     * @param customMessage mensaje personalizado que describe el error específico
     */
    protected BaseBusinessException(ErrorCodeDefinition errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }

    /**
     * @brief Constructor con código de error, mensaje personalizado y causa
     *
     * Crea una instancia de la excepción con mensaje personalizado y causa raíz,
     * manteniendo el código de error para categorización.
     * @param errorCode código de error que identifica el tipo de problema
     * @param customMessage mensaje personalizado que describe el error específico
     * @param cause excepción original que causó este error
     */
    protected BaseBusinessException(ErrorCodeDefinition errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.errorCode = errorCode;
    }

    /**
     * @brief Obtiene el código de error asociado a esta excepción
     * @return código de error que identifica el tipo de problema de negocio
     */
    public ErrorCodeDefinition getErrorCode() {
        return errorCode;
    }
}