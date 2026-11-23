package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized;

import lombok.Getter;

@Getter
public class EntityDoesNotExistException extends BaseException {

    public EntityDoesNotExistException(Integer status, String message) {
        super(status, message);
    }

}
