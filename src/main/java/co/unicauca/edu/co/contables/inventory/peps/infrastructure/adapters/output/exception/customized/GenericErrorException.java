package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.exception.customized;

/**
 * @brief Exception for unhandled or generic system errors
 * 
 * Used as a fallback when a more specific exception type is not applicable.
 */
public class GenericErrorException extends BaseException {
    /**
     * @brief Constructor for GenericErrorException
     * @param status HTTP status code
     * @param message Error description
     */
    public GenericErrorException(Integer status,String message){
        super(status,message);
    }
}
