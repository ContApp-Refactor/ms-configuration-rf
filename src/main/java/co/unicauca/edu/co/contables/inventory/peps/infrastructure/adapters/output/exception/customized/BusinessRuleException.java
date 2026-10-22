package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.exception.customized;

import lombok.Getter;
import lombok.Setter;

/**
 * @brief Exception thrown when a business rule is violated
 * 
 * Used to indicate that an operation cannot proceed due to domain logic constraints.
 */
@Getter
@Setter
public class BusinessRuleException extends BaseException{
    /**
     * @brief Constructor for BusinessRuleException
     * @param status HTTP status code
     * @param message Error description
     */
    public BusinessRuleException(Integer status, String message){
        super(status,message);

    }
    
}
