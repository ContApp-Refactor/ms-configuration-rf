package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized;

public class EntityAlreadyExists extends BaseException{

    public EntityAlreadyExists(Integer errorCode, String message) {
        super(errorCode, message);
    }
    
}
