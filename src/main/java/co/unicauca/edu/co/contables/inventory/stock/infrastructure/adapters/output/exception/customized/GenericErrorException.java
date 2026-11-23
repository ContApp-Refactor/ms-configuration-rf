package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized;

public class GenericErrorException extends BaseException {

    public GenericErrorException(Integer status, String message) {
        super(status, message);
    }

}
