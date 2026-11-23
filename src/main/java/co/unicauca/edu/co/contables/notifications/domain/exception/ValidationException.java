package co.unicauca.edu.co.contables.notifications.domain.exception;

/**
 * Exception that represents validation errors in the domain layer.
 * This exception extends RuntimeException and is used to indicate
 * errors related to data validation or business rules.
 */
public class ValidationException extends RuntimeException {

    /**
     * Constructor de la excepción de validación.
     * @param message Mensaje descriptivo del error de validación.
     */
    public ValidationException(String message) {
        super(message);
    }
    
}