package co.unicauca.edu.co.contables.configuration.commons.exceptions;

/**
 * @brief Excepción base para excepciones de negocio
 *
 * Excepción base para todas las excepciones de negocio del dominio.
 * Proporciona funcionalidad común para manejo de códigos de error y mensajes.
 */
public abstract class BaseBusinessException extends RuntimeException {
    
    private final ErrorCodeDefinition errorCode;
    
    protected BaseBusinessException(ErrorCodeDefinition errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    protected BaseBusinessException(ErrorCodeDefinition errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
    
    protected BaseBusinessException(ErrorCodeDefinition errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCodeDefinition getErrorCode() {
        return errorCode;
    }
}