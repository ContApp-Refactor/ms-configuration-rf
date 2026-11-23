package co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.inventory.stock.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized.BusinessRuleException;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized.EntityAlreadyExists;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized.EntityDoesNotExistException;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized.ErrorCode;
import co.unicauca.edu.co.contables.inventory.stock.infrastructure.adapters.output.exception.customized.GenericErrorException;

@Service
public class FormatterResultOutputPort implements IFormatterResultOutputPort {

    @Override
    public void returnBusinessRuleErrorResponse(int status, String message) {
        throw new  BusinessRuleException(status, ErrorCode.BUSINESS_RULE_VIOLATION.getDescription()  + message);
    }

    @Override
    public void returnEntityAlreadyExistsErrorResponse(int status, String message) {
        throw new EntityAlreadyExists(status, ErrorCode.ENTITY_ALREADY_EXISTS.getDescription() + message);
    }

    @Override
    public void returnEntityDoesNotExistErrorResponse(int status, String message) {
        throw new EntityDoesNotExistException(status, ErrorCode.ENTITY_NOT_FOUND.getDescription() + message);
    }

    @Override
    public void returnErrorGenericResponse(int status, String message) {
        throw new GenericErrorException(status, ErrorCode.GENERIC_ERROR.getDescription() + message);
    }
    
}
