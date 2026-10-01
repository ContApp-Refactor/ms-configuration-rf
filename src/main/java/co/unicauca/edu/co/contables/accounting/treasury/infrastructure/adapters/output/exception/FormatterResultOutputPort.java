package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.exception;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.accounting.treasury.domain.port.IFormatterResultOutputPort;
import co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.exception.handler.BusinessRuleException;


@Service
public class FormatterResultOutputPort implements IFormatterResultOutputPort {

    @Override
    public void returnResponseError(int status, String message) {
        throw new  BusinessRuleException(status, message);
    }

}
