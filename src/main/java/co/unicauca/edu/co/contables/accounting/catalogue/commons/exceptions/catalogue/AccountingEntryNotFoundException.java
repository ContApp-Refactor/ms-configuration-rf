package co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.catalogue;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AccountingEntryNotFoundException extends RuntimeException {
    
    public AccountingEntryNotFoundException(String message) {
        super(message);
    }
    
}
