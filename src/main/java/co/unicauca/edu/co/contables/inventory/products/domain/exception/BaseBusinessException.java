package co.unicauca.edu.co.contables.inventory.products.domain.exception;

/**
 * @brief Excepción base para errores de negocio del dominio
 *
 * Proporciona funcionalidad común para todas las excepciones de negocio,
 * incluyendo manejo de códigos de error estandarizados y mensajes personalizables.
 */
public abstract class BaseBusinessException extends RuntimeException {
    
    private final ErrorCode errorCode;
    
    protected BaseBusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    protected BaseBusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
    
    protected BaseBusinessException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}