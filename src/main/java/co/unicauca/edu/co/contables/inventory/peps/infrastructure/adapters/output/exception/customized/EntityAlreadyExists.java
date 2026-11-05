package co.unicauca.edu.co.contables.inventory.peps.infrastructure.adapters.output.exception.customized;

/**
 * @brief Exception thrown when attempting to create an entity that already exists
 * 
 * Typically maps to a 409 Conflict HTTP status.
 */
public class EntityAlreadyExists extends BaseException {
    /**
     * @brief Constructor for EntityAlreadyExists
     * @param errorCode HTTP status code
     * @param message Error description
     */
    public EntityAlreadyExists(Integer errorCode,String message){
        super(errorCode,message);
    }
}
