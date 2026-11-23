package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.exception.customized;

import lombok.Getter;
import lombok.Setter;

/**
 * @brief Abstract base class for custom application exceptions
 * 
 * Provides a common structure for exceptions including an HTTP status code
 * and a descriptive message.
 */
@Getter
@Setter
public abstract class BaseException extends RuntimeException {
    private Integer status;
    private String message;

    /**
     * @brief Constructor for BaseException
     * @param status HTTP status code associated with the error
     * @param message Error description
     */
    public BaseException(Integer status,String message){
        super(message);
        this.status=status;
        this.message=message;
    }
}
