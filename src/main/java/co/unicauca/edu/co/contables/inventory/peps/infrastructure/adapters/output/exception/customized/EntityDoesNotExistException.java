package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.exception.customized;

/**
 * @brief Exception thrown when a requested entity is not found
 * 
 * Typically maps to a 404 Not Found HTTP status.
 */
public class EntityDoesNotExistException extends BaseException {
    /**
     * @brief Constructor for EntityDoesNotExistException
     * @param status HTTP status code
     * @param message Error description
     */
    public EntityDoesNotExistException(Integer status,String message){
        super(status,message);
    }
}
