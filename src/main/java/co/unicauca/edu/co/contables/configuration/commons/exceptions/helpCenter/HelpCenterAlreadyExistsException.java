package co.unicauca.edu.co.contables.configuration.commons.exceptions.helpCenter;

import co.unicauca.edu.co.contables.configuration.commons.exceptions.BaseBusinessException;

public class HelpCenterAlreadyExistsException extends BaseBusinessException {
    
    public HelpCenterAlreadyExistsException(String name) {
        super(HelpCenterErrorCode.HELP_CENTER_ALREADY_EXISTS,
                String.format("Ya existe un registro de ayuda con el nombre '%s'", name));
    }
    
}
